package com.bestwo.dataplatform.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

public class ServletTraceLoggingFilter extends OncePerRequestFilter {

    private final BestwoLoggingProperties properties;
    private final HttpTraceLoggingSupport loggingSupport;

    public ServletTraceLoggingFilter(BestwoLoggingProperties properties, String applicationName) {
        this.properties = properties;
        this.loggingSupport = new HttpTraceLoggingSupport(properties, applicationName);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        long startedAtNanos = System.nanoTime();
        String requestPath = loggingSupport.resolveRequestPath(request.getRequestURI(), request.getQueryString());
        String traceId = loggingSupport.resolveOrGenerateId(request.getHeader(properties.getTraceIdHeader()));
        String requestId = loggingSupport.resolveOrGenerateId(request.getHeader(properties.getRequestIdHeader()));
        String userId = loggingSupport.resolveUserId(request.getHeader(properties.getUserIdHeader()));
        String clientIp = loggingSupport.resolveClientIp(
            request.getHeader("X-Forwarded-For"),
            request.getHeader("X-Real-Ip"),
            request.getRemoteAddr()
        );

        response.setHeader(properties.getTraceIdHeader(), traceId);
        response.setHeader(properties.getRequestIdHeader(), requestId);

        Map<String, String> previousContext = MDC.getCopyOfContextMap();
        LogContext.putRequestContext(
            traceId,
            requestId,
            userId,
            clientIp,
            request.getMethod(),
            requestPath
        );

        Throwable failure = null;
        try {
            filterChain.doFilter(request, response);
        } catch (Throwable throwable) {
            failure = throwable;
            throw throwable;
        } finally {
            long durationMs = (System.nanoTime() - startedAtNanos) / 1_000_000L;
            Principal principal = request.getUserPrincipal();
            String resolvedUserId = userId == null && principal != null ? principal.getName() : userId;
            int status = failure == null ? response.getStatus() : resolveStatus(response.getStatus());
            loggingSupport.logAccess(
                traceId,
                requestId,
                resolvedUserId,
                clientIp,
                request.getMethod(),
                requestPath,
                status,
                durationMs,
                request.getHeader("User-Agent"),
                request.getHeader("Referer"),
                failure
            );
            LogContext.restore(previousContext);
        }
    }

    private int resolveStatus(int currentStatus) {
        return currentStatus >= 400 ? currentStatus : 500;
    }
}
