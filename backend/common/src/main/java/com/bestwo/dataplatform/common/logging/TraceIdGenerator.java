package com.bestwo.dataplatform.common.logging;

import java.util.UUID;

public final class TraceIdGenerator {

    private TraceIdGenerator() {
    }

    public static String nextId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
