package com.bestwo.dataplatform.common.logging;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

class HttpTraceLoggingSupport {

    private static final Logger ACCESS_LOG = LoggerFactory.getLogger(LogConstants.ACCESS_LOGGER_NAME);

    private final BestwoLoggingProperties properties;
    private final String applicationName;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    HttpTraceLoggingSupport(BestwoLoggingProperties properties, String applicationName) {
        this.properties = properties;
        this.applicationName = applicationName;
    }

    String resolveOrGenerateId(String candidate) {
        return StringUtils.hasText(candidate) ? candidate.trim() : TraceIdGenerator.nextId();
    }

    String resolveUserId(String candidate) {
        return StringUtils.hasText(candidate) ? candidate.trim() : null;
    }

    String resolveRequestPath(String path, String rawQuery) {
        String normalizedPath = StringUtils.hasText(path) ? path : "/";
        if (!properties.isIncludeQueryString() || !StringUtils.hasText(rawQuery)) {
            return normalizedPath;
        }
        return normalizedPath + "?" + rawQuery;
    }

    String resolveClientIp(String forwardedFor, String realIp, String remoteAddress) {
        if (StringUtils.hasText(forwardedFor)) {
            String[] segments = forwardedFor.split(",");
            if (segments.length > 0 && StringUtils.hasText(segments[0])) {
                return segments[0].trim();
            }
        }
        if (StringUtils.hasText(realIp)) {
            return realIp.trim();
        }
        return StringUtils.hasText(remoteAddress) ? remoteAddress.trim() : "unknown";
    }

    void propagateHeaders(HttpHeaders headers, String traceId, String requestId, String clientIp, String userId) {
        if (!properties.isPropagationEnabled()) {
            return;
        }
        headers.set(properties.getTraceIdHeader(), traceId);
        headers.set(properties.getRequestIdHeader(), requestId);
        headers.set(properties.getSourceServiceHeader(), applicationName);
        if (StringUtils.hasText(clientIp)) {
            headers.set(properties.getClientIpHeader(), clientIp);
        }
        if (StringUtils.hasText(userId)) {
            headers.set(properties.getUserIdHeader(), userId);
        }
    }

    void addResponseHeaders(HttpHeaders headers, String traceId, String requestId) {
        if (!properties.isPropagationEnabled()) {
            return;
        }
        headers.set(properties.getTraceIdHeader(), traceId);
        headers.set(properties.getRequestIdHeader(), requestId);
    }

    boolean shouldIgnore(String path) {
        List<String> ignoredPaths = properties.getIgnoredPaths();
        if (ignoredPaths == null || ignoredPaths.isEmpty()) {
            return false;
        }
        for (String ignoredPath : ignoredPaths) {
            if (StringUtils.hasText(ignoredPath) && pathMatcher.match(ignoredPath.trim(), path)) {
                return true;
            }
        }
        return false;
    }

    void logAccess(
        String traceId,
        String requestId,
        String userId,
        String clientIp,
        String httpMethod,
        String httpPath,
        int httpStatus,
        long durationMs,
        String userAgent,
        String referer,
        Throwable error
    ) {
        if (!properties.isAccessLogEnabled() || shouldIgnore(httpPath)) {
            return;
        }

        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("event", "http_access");
        fields.put("traceId", traceId);
        fields.put("requestId", requestId);
        fields.put("clientIp", clientIp);
        fields.put("httpMethod", httpMethod);
        fields.put("httpPath", httpPath);
        fields.put("httpStatus", httpStatus);
        fields.put("durationMs", durationMs);
        if (StringUtils.hasText(userId)) {
            fields.put("userId", userId);
        }
        if (StringUtils.hasText(userAgent)) {
            fields.put("userAgent", truncate(userAgent, properties.getMaxUserAgentLength()));
        }
        if (StringUtils.hasText(referer)) {
            fields.put("referer", truncate(referer, properties.getMaxRefererLength()));
        }
        if (error != null) {
            fields.put("errorType", error.getClass().getSimpleName());
        }

        if (httpStatus >= 500 || durationMs >= properties.getSlowRequestThresholdMs()) {
            ACCESS_LOG.warn("http access completed {}", StructuredArguments.entries(fields));
            return;
        }
        ACCESS_LOG.info("http access completed {}", StructuredArguments.entries(fields));
    }

    private String truncate(String value, int maxLength) {
        if (value == null || maxLength <= 0 || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
