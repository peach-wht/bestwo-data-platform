package com.bestwo.dataplatform.order.payment.wechat;

import com.bestwo.dataplatform.common.exception.BusinessException;
import com.bestwo.dataplatform.order.domain.enums.PayPlatform;
import com.bestwo.dataplatform.order.domain.enums.PayTradeType;
import com.bestwo.dataplatform.order.domain.enums.PaymentOrderStatus;
import com.bestwo.dataplatform.order.payment.model.PayNotifyResult;
import com.bestwo.dataplatform.order.payment.model.PayPrepayCommand;
import com.bestwo.dataplatform.order.payment.model.PayPrepayResult;
import com.bestwo.dataplatform.order.payment.model.PayQueryResult;
import com.bestwo.dataplatform.order.payment.spi.PayClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(prefix = "bestwo.pay", name = "provider", havingValue = "WECHAT")
public class WeChatPayClient implements PayClient {

    private final WeChatPayProperties properties;
    private final WeChatPayRequestSigner requestSigner;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public WeChatPayClient(
        WeChatPayProperties properties,
        WeChatPayRequestSigner requestSigner,
        ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.requestSigner = requestSigner;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
            .baseUrl(properties.getBaseUrl())
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    @Override
    public PayPlatform platform() {
        return PayPlatform.WECHAT_PAY;
    }

    @Override
    public boolean supports(PayTradeType tradeType) {
        return tradeType == PayTradeType.NATIVE;
    }

    @Override
    public PayPrepayResult prepay(PayPrepayCommand command) {
        assertSupports(command.platform(), command.tradeType());
        String path = "/v3/pay/transactions/native";
        String requestBody = buildNativePrepayRequest(command);
        String responseBody = restClient.post()
            .uri(path)
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, requestSigner.buildAuthorization(properties, HttpMethod.POST, path, requestBody))
            .header("Wechatpay-Serial", properties.getMerchantSerialNo())
            .body(requestBody)
            .retrieve()
            .onStatus(status -> status.isError(), this::handleError)
            .body(String.class);

        JsonNode json = readJson(responseBody);
        return new PayPrepayResult(
            PayPlatform.WECHAT_PAY,
            command.tradeType(),
            PaymentOrderStatus.WAIT_PAY,
            command.merchantOrderNo(),
            command.paymentOrderNo(),
            text(json, "transaction_id"),
            text(json, "code_url"),
            text(json, "prepay_id"),
            command.expireAt(),
            responseBody
        );
    }

    @Override
    public PayQueryResult query(String merchantOrderNo, String paymentOrderNo) {
        if (merchantOrderNo == null || merchantOrderNo.isBlank()) {
            throw new BusinessException("merchantOrderNo must not be blank");
        }
        String path = "/v3/pay/transactions/out-trade-no/" + merchantOrderNo + "?mchid=" + properties.getMerchantId();
        String responseBody = restClient.get()
            .uri(path)
            .header(HttpHeaders.AUTHORIZATION, requestSigner.buildAuthorization(properties, HttpMethod.GET, path, ""))
            .header("Wechatpay-Serial", properties.getMerchantSerialNo())
            .retrieve()
            .onStatus(status -> status.isError(), this::handleError)
            .body(String.class);

        JsonNode json = readJson(responseBody);
        return new PayQueryResult(
            PayPlatform.WECHAT_PAY,
            merchantOrderNo,
            paymentOrderNo,
            text(json, "transaction_id"),
            mapTradeState(text(json, "trade_state")),
            amountFromJson(json.path("amount").path("payer_total")),
            parseTime(text(json, "success_time")),
            responseBody
        );
    }

    @Override
    public PayNotifyResult parseNotify(String requestBody, Map<String, String> headers) {
        if (requestBody == null || requestBody.isBlank()) {
            throw new BusinessException("wechat pay notify body must not be blank");
        }

        JsonNode envelope = readJson(requestBody);
        JsonNode payload = resolveNotifyPayload(envelope);
        String notifyId = text(envelope, "id");
        if (notifyId == null || notifyId.isBlank()) {
            notifyId = header(headers, "Wechatpay-Request-Id");
        }

        return new PayNotifyResult(
            PayPlatform.WECHAT_PAY,
            text(payload, "out_trade_no"),
            text(payload, "attach"),
            text(payload, "transaction_id"),
            mapTradeState(text(payload, "trade_state")),
            amountFromJson(payload.path("amount").path("payer_total")),
            parseTime(text(payload, "success_time")),
            notifyId,
            payload.toString(),
            text(payload, "trade_state_desc")
        );
    }

    private String buildNativePrepayRequest(PayPrepayCommand command) {
        ObjectNode body = objectMapper.createObjectNode()
            .put("appid", properties.getAppId())
            .put("mchid", properties.getMerchantId())
            .put("description", command.description() == null ? command.subject() : command.description())
            .put("out_trade_no", command.merchantOrderNo())
            .put("attach", command.paymentOrderNo())
            .put("notify_url", command.notifyUrl() == null ? properties.getNotifyUrl() : command.notifyUrl())
            .set("amount", objectMapper.createObjectNode()
                .put("total", command.amountFen())
                .put("currency", command.currency() == null ? "CNY" : command.currency()));
        if (command.expireAt() != null) {
            body.put("time_expire", command.expireAt().toString());
        }
        try {
            return objectMapper.writeValueAsString(body);
        } catch (IOException exception) {
            throw new BusinessException("failed to build wechat pay request body");
        }
    }

    private void handleError(org.springframework.http.HttpRequest request, ClientHttpResponse response) throws IOException {
        String responseBody = new String(response.getBody().readAllBytes());
        throw new BusinessException("wechat pay request failed: " + response.getStatusCode() + " " + responseBody);
    }

    private JsonNode readJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (IOException exception) {
            throw new BusinessException("failed to parse wechat pay response");
        }
    }

    private JsonNode resolveNotifyPayload(JsonNode envelope) {
        JsonNode resource = envelope.path("resource");
        if (resource.isMissingNode() || resource.isNull() || resource.path("ciphertext").isMissingNode()) {
            return envelope;
        }
        return readJson(decryptResource(resource));
    }

    private String decryptResource(JsonNode resource) {
        String apiV3Key = properties.getApiV3Key();
        if (apiV3Key == null || apiV3Key.isBlank()) {
            throw new BusinessException("api v3 key must be configured to decrypt wechat pay notify");
        }

        String ciphertext = text(resource, "ciphertext");
        String nonce = text(resource, "nonce");
        String associatedData = text(resource, "associated_data");
        if (ciphertext == null || nonce == null) {
            throw new BusinessException("wechat pay notify resource is incomplete");
        }

        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(apiV3Key.getBytes(StandardCharsets.UTF_8), "AES");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(128, nonce.getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, parameterSpec);
            if (associatedData != null && !associatedData.isBlank()) {
                cipher.updateAAD(associatedData.getBytes(StandardCharsets.UTF_8));
            }
            byte[] plainBytes = cipher.doFinal(Base64.getDecoder().decode(ciphertext));
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException exception) {
            throw new BusinessException("failed to decrypt wechat pay notify resource");
        }
    }

    private String text(JsonNode node, String fieldName) {
        JsonNode field = node.path(fieldName);
        return field.isMissingNode() || field.isNull() ? null : field.asText();
    }

    private Long amountFromJson(JsonNode node) {
        return node == null || node.isMissingNode() || node.isNull() ? null : node.asLong();
    }

    private Instant parseTime(String value) {
        return value == null || value.isBlank() ? null : Instant.parse(value);
    }

    private String header(Map<String, String> headers, String key) {
        if (headers == null || headers.isEmpty()) {
            return null;
        }
        return headers.entrySet().stream()
            .filter(entry -> entry.getKey() != null && entry.getKey().equalsIgnoreCase(key))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(null);
    }

    private PaymentOrderStatus mapTradeState(String tradeState) {
        if (tradeState == null || tradeState.isBlank()) {
            return PaymentOrderStatus.WAIT_PAY;
        }
        return switch (tradeState.toUpperCase()) {
            case "SUCCESS" -> PaymentOrderStatus.SUCCESS;
            case "CLOSED", "REVOKED" -> PaymentOrderStatus.CLOSED;
            case "PAYERROR" -> PaymentOrderStatus.FAILED;
            case "NOTPAY", "USERPAYING" -> PaymentOrderStatus.WAIT_PAY;
            default -> PaymentOrderStatus.WAIT_PAY;
        };
    }
}
