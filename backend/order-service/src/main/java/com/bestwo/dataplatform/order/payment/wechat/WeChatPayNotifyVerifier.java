package com.bestwo.dataplatform.order.payment.wechat;

import com.bestwo.dataplatform.common.exception.BusinessException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class WeChatPayNotifyVerifier {

    private final WeChatPayProperties properties;

    public WeChatPayNotifyVerifier(WeChatPayProperties properties) {
        this.properties = properties;
    }

    public boolean isVerificationConfigured() {
        return StringUtils.hasText(properties.getPlatformPublicKeyPem());
    }

    public void verifyIfConfigured(String requestBody, Map<String, String> headers) {
        if (!isVerificationConfigured()) {
            return;
        }

        String timestamp = header(headers, "Wechatpay-Timestamp");
        String nonce = header(headers, "Wechatpay-Nonce");
        String signature = header(headers, "Wechatpay-Signature");
        if (!StringUtils.hasText(timestamp) || !StringUtils.hasText(nonce) || !StringUtils.hasText(signature)) {
            throw new BusinessException("missing wechat pay signature headers");
        }

        String content = timestamp + "\n" + nonce + "\n" + (requestBody == null ? "" : requestBody) + "\n";
        try {
            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(parsePublicKey(properties.getPlatformPublicKeyPem()));
            verifier.update(content.getBytes(StandardCharsets.UTF_8));
            boolean verified = verifier.verify(Base64.getDecoder().decode(signature));
            if (!verified) {
                throw new BusinessException("wechat pay notify signature verification failed");
            }
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException("failed to verify wechat pay notify signature");
        }
    }

    private PublicKey parsePublicKey(String pem) throws Exception {
        String normalized = pem
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(normalized);
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
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
}
