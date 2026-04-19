package com.bestwo.dataplatform.common.logging;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.server.WebFilter;

@AutoConfiguration
@ConditionalOnProperty(prefix = "bestwo.logging", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnWebApplication(type = Type.REACTIVE)
@ConditionalOnClass(WebFilter.class)
public class BestwoReactiveLoggingAutoConfiguration {

    @Bean
    public ReactiveTraceLoggingWebFilter reactiveTraceLoggingWebFilter(
        BestwoLoggingProperties properties,
        Environment environment
    ) {
        return new ReactiveTraceLoggingWebFilter(
            properties,
            environment.getProperty("spring.application.name", "unknown-service")
        );
    }
}
