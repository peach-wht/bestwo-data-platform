package com.bestwo.dataplatform.common.logging;

public final class LogConstants {

    public static final String ACCESS_LOGGER_NAME = "bestwo.access";

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String CLIENT_IP_HEADER = "X-Client-Ip";
    public static final String SOURCE_SERVICE_HEADER = "X-Source-Service";

    public static final String TRACE_ID_MDC_KEY = "traceId";
    public static final String REQUEST_ID_MDC_KEY = "requestId";
    public static final String USER_ID_MDC_KEY = "userId";
    public static final String CLIENT_IP_MDC_KEY = "clientIp";
    public static final String HTTP_METHOD_MDC_KEY = "httpMethod";
    public static final String HTTP_PATH_MDC_KEY = "httpPath";

    private LogConstants() {
    }
}
