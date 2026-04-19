package com.bestwo.dataplatform.common.logging;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.WebFilter;

@AutoConfiguration
@EnableConfigurationProperties(BestwoLoggingProperties.class)
@ConditionalOnProperty(prefix = "bestwo.logging", name = "enabled", havingValue = "true", matchIfMissing = true)
public class BestwoLoggingAutoConfiguration {

    @Bean
    @ConditionalOnWebApplication(type = Type.SERVLET)
    @ConditionalOnClass(OncePerRequestFilter.class)
    public ServletTraceLoggingFilter servletTraceLoggingFilter(
        BestwoLoggingProperties properties,
        Environment environment
    ) {
        return new ServletTraceLoggingFilter(properties, resolveApplicationName(environment));
    }

    @Bean
    @ConditionalOnWebApplication(type = Type.REACTIVE)
    @ConditionalOnClass(WebFilter.class)
    public ReactiveTraceLoggingWebFilter reactiveTraceLoggingWebFilter(
        BestwoLoggingProperties properties,
        Environment environment
    ) {
        return new ReactiveTraceLoggingWebFilter(properties, resolveApplicationName(environment));
    }

    @Bean
    @ConditionalOnClass(name = {
        "org.springframework.boot.web.client.RestClientCustomizer",
        "org.springframework.web.client.RestClient"
    })
    public RestClientCustomizer bestwoRestClientCustomizer(
        BestwoLoggingProperties properties,
        Environment environment
    ) {
        String applicationName = resolveApplicationName(environment);
        return builder -> builder.requestInterceptor(new CurrentTraceClientHttpRequestInterceptor(properties, applicationName));
    }

    @Bean
    @ConditionalOnWebApplication(type = Type.SERVLET)
    @ConditionalOnBean(ServletTraceLoggingFilter.class)
    public FilterRegistrationBean<ServletTraceLoggingFilter> servletTraceLoggingFilterRegistration(
        ServletTraceLoggingFilter filter
    ) {
        FilterRegistrationBean<ServletTraceLoggingFilter> registrationBean = new FilterRegistrationBean<>(filter);
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registrationBean;
    }

    private String resolveApplicationName(Environment environment) {
        return environment.getProperty("spring.application.name", "unknown-service");
    }
}
