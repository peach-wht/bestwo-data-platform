package com.bestwo.dataplatform.common.logging;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bestwo.logging")
public class BestwoLoggingProperties {

    private boolean enabled = true;
    private boolean accessLogEnabled = true;
    private boolean propagationEnabled = true;
    private boolean includeQueryString = false;
    private long slowRequestThresholdMs = 1000L;
    private int maxUserAgentLength = 160;
    private int maxRefererLength = 160;
    private List<String> ignoredPaths = new ArrayList<>(List.of(
        "/health",
        "/api/health",
        "/actuator/**",
        "/favicon.ico"
    ));
    private String traceIdHeader = LogConstants.TRACE_ID_HEADER;
    private String requestIdHeader = LogConstants.REQUEST_ID_HEADER;
    private String userIdHeader = LogConstants.USER_ID_HEADER;
    private String clientIpHeader = LogConstants.CLIENT_IP_HEADER;
    private String sourceServiceHeader = LogConstants.SOURCE_SERVICE_HEADER;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isAccessLogEnabled() {
        return accessLogEnabled;
    }

    public void setAccessLogEnabled(boolean accessLogEnabled) {
        this.accessLogEnabled = accessLogEnabled;
    }

    public boolean isPropagationEnabled() {
        return propagationEnabled;
    }

    public void setPropagationEnabled(boolean propagationEnabled) {
        this.propagationEnabled = propagationEnabled;
    }

    public boolean isIncludeQueryString() {
        return includeQueryString;
    }

    public void setIncludeQueryString(boolean includeQueryString) {
        this.includeQueryString = includeQueryString;
    }

    public long getSlowRequestThresholdMs() {
        return slowRequestThresholdMs;
    }

    public void setSlowRequestThresholdMs(long slowRequestThresholdMs) {
        this.slowRequestThresholdMs = slowRequestThresholdMs;
    }

    public int getMaxUserAgentLength() {
        return maxUserAgentLength;
    }

    public void setMaxUserAgentLength(int maxUserAgentLength) {
        this.maxUserAgentLength = maxUserAgentLength;
    }

    public int getMaxRefererLength() {
        return maxRefererLength;
    }

    public void setMaxRefererLength(int maxRefererLength) {
        this.maxRefererLength = maxRefererLength;
    }

    public List<String> getIgnoredPaths() {
        return ignoredPaths;
    }

    public void setIgnoredPaths(List<String> ignoredPaths) {
        this.ignoredPaths = ignoredPaths;
    }

    public String getTraceIdHeader() {
        return traceIdHeader;
    }

    public void setTraceIdHeader(String traceIdHeader) {
        this.traceIdHeader = traceIdHeader;
    }

    public String getRequestIdHeader() {
        return requestIdHeader;
    }

    public void setRequestIdHeader(String requestIdHeader) {
        this.requestIdHeader = requestIdHeader;
    }

    public String getUserIdHeader() {
        return userIdHeader;
    }

    public void setUserIdHeader(String userIdHeader) {
        this.userIdHeader = userIdHeader;
    }

    public String getClientIpHeader() {
        return clientIpHeader;
    }

    public void setClientIpHeader(String clientIpHeader) {
        this.clientIpHeader = clientIpHeader;
    }

    public String getSourceServiceHeader() {
        return sourceServiceHeader;
    }

    public void setSourceServiceHeader(String sourceServiceHeader) {
        this.sourceServiceHeader = sourceServiceHeader;
    }
}
