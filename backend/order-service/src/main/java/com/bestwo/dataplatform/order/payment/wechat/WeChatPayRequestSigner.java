package com.bestwo.dataplatform.order.payment.wechat;

import com.bestwo.dataplatform.common.exception.BusinessException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class WeChatPayRequestSigner {

    public String buildAuthorization(
        WeChatPayProperties properties,
        HttpMethod method,
        String canonicalUrl,
        String body
    ) {
        validateConfig(properties);
        String nonceStr = UUID.randomUUID().toString().replace("-", "");
        long timestamp = System.currentTimeMillis() / 1000;
        String requestBody = body == null ? "" : body;
        String message = method.name() + "\n"
            + canonicalUrl + "\n"
            + timestamp + "\n"
            + nonceStr + "\n"
            + requestBody + "\n";
        String signature = sign(message, properties.getPrivateKeyPem());
        return "WECHATPAY2-SHA256-RSA2048 "
            + "mchid=\"" + properties.getMerchantId() + "\","
            + "nonce_str=\"" + nonceStr + "\","
            + "timestamp=\"" + timestamp + "\","
            + "serial_no=\"" + properties.getMerchantSerialNo() + "\","
            + "signature=\"" + signature + "\"";
    }

    private void validateConfig(WeChatPayProperties properties) {
        if (!StringUtils.hasText(properties.getMerchantId())
            || !StringUtils.hasText(properties.getMerchantSerialNo())
            || !StringUtils.hasText(properties.getPrivateKeyPem())) {
            throw new BusinessException("wechat pay merchant config is incomplete");
        }
    }

    private String sign(String message, String privateKeyPem) {
        try {
            String normalizedPem = privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
            byte[] keyBytes = Base64.getDecoder().decode(normalizedPem);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception exception) {
            throw new BusinessException("failed to sign wechat pay request: " + exception.getMessage());
        }
    }
}
