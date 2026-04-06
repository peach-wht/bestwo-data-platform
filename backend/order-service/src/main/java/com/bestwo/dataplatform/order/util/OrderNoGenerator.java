package com.bestwo.dataplatform.order.util;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class OrderNoGenerator {

    private static final DateTimeFormatter ORDER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

    private OrderNoGenerator() {
    }

    public static String generateOrderId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String generateOrderNo() {
        return "WX" + LocalDateTime.now(ZONE_ID).format(ORDER_NO_FORMATTER) + nextDigits(6);
    }

    private static String nextDigits(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(RANDOM.nextInt(10));
        }
        return builder.toString();
    }
}
