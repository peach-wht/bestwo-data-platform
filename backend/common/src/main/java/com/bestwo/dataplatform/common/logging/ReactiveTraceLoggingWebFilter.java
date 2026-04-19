package com.bestwo.dataplatform.common.logging;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class ReactiveTraceLoggingWebFilter implements WebFilter {

    private final BestwoLoggingProperties properties;
    private final HttpTraceLoggingSupport loggingSupport;

    public ReactiveTraceLoggingWebFilter(BestwoLoggingProperties properties, String applicationName) {
        this.properties = properties;
        this.loggingSupport = new HttpTraceLoggingSupport(properties, applicationName);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long startedAtNanos = System.nanoTime();
        ServerHttpRequest originalRequest = exchange.getRequest();
        String requestPath = loggingSupport.resolveRequestPath(
            originalRequest.getURI().getRawPath(),
            originalRequest.getURI().getRawQuery()
        );
        String traceId = loggingSupport.resolveOrGenerateId(originalRequest.getHeaders().getFirst(properties.getTraceIdHeader()));
        String requestId = loggingSupport.resolveOrGenerateId(
            originalRequest.getHeaders().getFirst(properties.getRequestIdHeader())
        );
        String userId = loggingSupport.resolveUserId(originalRequest.getHeaders().getFirst(properties.getUserIdHeader()));
        String clientIp = loggingSupport.resolveClientIp(
            originalRequest.getHeaders().getFirst("X-Forwarded-For"),
            originalRequest.getHeaders().getFirst("X-Real-Ip"),
            resolveRemoteAddress(originalRequest)
        );

        ServerHttpRequest mutatedRequest = originalRequest.mutate()
            .headers(headers -> loggingSupport.propagateHeaders(headers, traceId, requestId, clientIp, userId))
            .build();
        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
        loggingSupport.addResponseHeaders(mutatedExchange.getResponse().getHeaders(), traceId, requestId);

        AtomicReference<Throwable> errorRef = new AtomicReference<>();
        return chain.filter(mutatedExchange)
            .doOnError(errorRef::set)
            .doFinally(signalType -> {
                long durationMs = (System.nanoTime() - startedAtNanos) / 1_000_000L;
                loggingSupport.logAccess(
                    traceId,
                    requestId,
                    userId,
                    clientIp,
                    mutatedRequest.getMethod() == null ? "UNKNOWN" : mutatedRequest.getMethod().name(),
                    requestPath,
                    resolveStatus(mutatedExchange, errorRef.get()),
                    durationMs,
                    originalRequest.getHeaders().getFirst("User-Agent"),
                    originalRequest.getHeaders().getFirst("Referer"),
                    errorRef.get()
                );
            });
    }

    private String resolveRemoteAddress(ServerHttpRequest request) {
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        if (remoteAddress == null) {
            return null;
        }
        if (remoteAddress.getAddress() != null) {
            return remoteAddress.getAddress().getHostAddress();
        }
        return remoteAddress.getHostString();
    }

    private int resolveStatus(ServerWebExchange exchange, Throwable error) {
        HttpStatusCode statusCode = exchange.getResponse().getStatusCode();
        if (statusCode != null) {
            return statusCode.value();
        }
        return error == null ? 200 : 500;
    }
}
