package com.bestwo.dataplatform.order.payment.mock;

public final class MockPaymentConstants {

    public static final String PROVIDER = "MOCK";
    public static final String MERCHANT_CODE = "MOCK_WECHAT";
    public static final String EVENT_TYPE = "MOCK_PAYMENT";
    public static final String NOTIFY_URL = "/pay/mock/notify";

    private MockPaymentConstants() {
    }

    public static String buildMockPayUrl(String paymentOrderNo, String mockPayToken) {
        return "/orders?paymentOrderNo=" + paymentOrderNo + "&mockPayToken=" + mockPayToken;
    }
}
