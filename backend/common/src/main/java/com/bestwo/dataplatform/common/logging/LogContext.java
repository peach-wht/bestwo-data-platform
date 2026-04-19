package com.bestwo.dataplatform.common.logging;

import java.util.Map;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

public final class LogContext {

    private LogContext() {
    }

    public static void put(String key, String value) {
        if (StringUtils.hasText(value)) {
            MDC.put(key, value.trim());
            return;
        }
        MDC.remove(key);
    }

    public static void putRequestContext(
        String traceId,
        String requestId,
        String userId,
        String clientIp,
        String httpMethod,
        String httpPath
    ) {
        put(LogConstants.TRACE_ID_MDC_KEY, traceId);
        put(LogConstants.REQUEST_ID_MDC_KEY, requestId);
        put(LogConstants.USER_ID_MDC_KEY, userId);
        put(LogConstants.CLIENT_IP_MDC_KEY, clientIp);
        put(LogConstants.HTTP_METHOD_MDC_KEY, httpMethod);
        put(LogConstants.HTTP_PATH_MDC_KEY, httpPath);
    }

    public static String getTraceId() {
        return MDC.get(LogConstants.TRACE_ID_MDC_KEY);
    }

    public static String getRequestId() {
        return MDC.get(LogConstants.REQUEST_ID_MDC_KEY);
    }

    public static String getUserId() {
        return MDC.get(LogConstants.USER_ID_MDC_KEY);
    }

    public static String getClientIp() {
        return MDC.get(LogConstants.CLIENT_IP_MDC_KEY);
    }

    public static void restore(Map<String, String> contextMap) {
        if (contextMap == null || contextMap.isEmpty()) {
            MDC.clear();
            return;
        }
        MDC.setContextMap(contextMap);
    }
}
