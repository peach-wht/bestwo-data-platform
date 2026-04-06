package com.bestwo.dataplatform.order.config;

import com.bestwo.dataplatform.order.payment.wechat.WeChatPayProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({PayProperties.class, WeChatPayProperties.class})
public class PayClientConfig {
}
