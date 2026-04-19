package com.bestwo.dataplatform.common.logging;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(BestwoLoggingProperties.class)
@ConditionalOnProperty(prefix = "bestwo.logging", name = "enabled", havingValue = "true", matchIfMissing = true)
public class BestwoLoggingAutoConfiguration {

    @Bean
    @ConditionalOnClass(name = {
        "org.springframework.boot.web.client.RestClientCustomizer",
        "org.springframework.web.client.RestClient"
    })
    public RestClientCustomizer bestwoRestClientCustomizer(
        BestwoLoggingProperties properties,
        Environment environment
    ) {
        String applicationName = environment.getProperty("spring.application.name", "unknown-service");
        return builder -> builder.requestInterceptor(new CurrentTraceClientHttpRequestInterceptor(properties, applicationName));
    }
}
