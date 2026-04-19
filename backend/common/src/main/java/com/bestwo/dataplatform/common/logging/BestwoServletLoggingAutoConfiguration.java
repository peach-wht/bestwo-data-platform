package com.bestwo.dataplatform.common.logging;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.web.filter.OncePerRequestFilter;

@AutoConfiguration
@ConditionalOnProperty(prefix = "bestwo.logging", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnClass(name = {
    "jakarta.servlet.Filter",
    "org.springframework.web.filter.OncePerRequestFilter",
    "org.springframework.boot.web.servlet.FilterRegistrationBean"
})
public class BestwoServletLoggingAutoConfiguration {

    @Bean
    @ConditionalOnClass(OncePerRequestFilter.class)
    public ServletTraceLoggingFilter servletTraceLoggingFilter(
        BestwoLoggingProperties properties,
        Environment environment
    ) {
        return new ServletTraceLoggingFilter(
            properties,
            environment.getProperty("spring.application.name", "unknown-service")
        );
    }

    @Bean
    @ConditionalOnBean(ServletTraceLoggingFilter.class)
    public FilterRegistrationBean<ServletTraceLoggingFilter> servletTraceLoggingFilterRegistration(
        ServletTraceLoggingFilter filter
    ) {
        FilterRegistrationBean<ServletTraceLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(filter);
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registrationBean;
    }
}
