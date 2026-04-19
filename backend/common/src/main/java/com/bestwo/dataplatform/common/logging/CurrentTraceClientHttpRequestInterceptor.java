package com.bestwo.dataplatform.common.logging;

import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;

public class CurrentTraceClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private final BestwoLoggingProperties properties;
    private final String applicationName;

    public CurrentTraceClientHttpRequestInterceptor(BestwoLoggingProperties properties, String applicationName) {
        this.properties = properties;
        this.applicationName = applicationName;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
        throws IOException {
        HttpHeaders headers = request.getHeaders();
        putIfAbsent(headers, properties.getTraceIdHeader(), LogContext.getTraceId());
        putIfAbsent(headers, properties.getRequestIdHeader(), LogContext.getRequestId());
        putIfAbsent(headers, properties.getUserIdHeader(), LogContext.getUserId());
        putIfAbsent(headers, properties.getClientIpHeader(), LogContext.getClientIp());
        headers.set(properties.getSourceServiceHeader(), applicationName);
        return execution.execute(request, body);
    }

    private void putIfAbsent(HttpHeaders headers, String headerName, String value) {
        if (!StringUtils.hasText(value) || headers.containsKey(headerName)) {
            return;
        }
        headers.set(headerName, value);
    }
}
