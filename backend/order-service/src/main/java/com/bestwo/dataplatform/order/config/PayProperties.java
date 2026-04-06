package com.bestwo.dataplatform.order.config;

import com.bestwo.dataplatform.common.exception.BusinessException;
import java.util.Locale;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bestwo.pay")
public class PayProperties {

    private static final String MOCK = "MOCK";
    private static final String WECHAT = "WECHAT";

    private String provider = MOCK;

    public String getProvider() {
        return provider == null ? MOCK : provider.trim().toUpperCase(Locale.ROOT);
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public boolean isMockProvider() {
        return MOCK.equals(getProvider());
    }

    public boolean isWechatProvider() {
        return WECHAT.equals(getProvider());
    }

    public void assertMockProvider() {
        if (!isMockProvider()) {
            throw new BusinessException("mock payment provider is disabled");
        }
    }

    public void assertWechatProvider() {
        if (!isWechatProvider()) {
            throw new BusinessException("wechat payment provider is disabled");
        }
    }
}
