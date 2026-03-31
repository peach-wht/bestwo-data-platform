package com.bestwo.dataplatform.order.domain.support;

import com.bestwo.dataplatform.common.exception.BusinessException;
import com.bestwo.dataplatform.order.domain.enums.PaymentOrderStatus;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class PaymentOrderStatusFlow {

    private static final Map<PaymentOrderStatus, Set<PaymentOrderStatus>> ALLOWED_TRANSITIONS =
        new EnumMap<>(PaymentOrderStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(
            PaymentOrderStatus.INIT,
            EnumSet.of(PaymentOrderStatus.PREPAYING, PaymentOrderStatus.CLOSED, PaymentOrderStatus.FAILED)
        );
        ALLOWED_TRANSITIONS.put(
            PaymentOrderStatus.PREPAYING,
            EnumSet.of(PaymentOrderStatus.WAIT_PAY, PaymentOrderStatus.CLOSED, PaymentOrderStatus.FAILED)
        );
        ALLOWED_TRANSITIONS.put(
            PaymentOrderStatus.WAIT_PAY,
            EnumSet.of(PaymentOrderStatus.SUCCESS, PaymentOrderStatus.CLOSED, PaymentOrderStatus.FAILED)
        );
        ALLOWED_TRANSITIONS.put(
            PaymentOrderStatus.SUCCESS,
            EnumSet.of(PaymentOrderStatus.REFUNDING, PaymentOrderStatus.REFUNDED)
        );
        ALLOWED_TRANSITIONS.put(
            PaymentOrderStatus.REFUNDING,
            EnumSet.of(PaymentOrderStatus.SUCCESS, PaymentOrderStatus.REFUNDED)
        );
        ALLOWED_TRANSITIONS.put(PaymentOrderStatus.CLOSED, EnumSet.noneOf(PaymentOrderStatus.class));
        ALLOWED_TRANSITIONS.put(PaymentOrderStatus.FAILED, EnumSet.noneOf(PaymentOrderStatus.class));
        ALLOWED_TRANSITIONS.put(PaymentOrderStatus.REFUNDED, EnumSet.noneOf(PaymentOrderStatus.class));
    }

    private PaymentOrderStatusFlow() {}

    public static boolean canTransition(PaymentOrderStatus from, PaymentOrderStatus to) {
        if (from == null || to == null) {
            return false;
        }
        if (from == to) {
            return true;
        }
        return ALLOWED_TRANSITIONS.getOrDefault(from, EnumSet.noneOf(PaymentOrderStatus.class)).contains(to);
    }

    public static void assertCanTransition(PaymentOrderStatus from, PaymentOrderStatus to) {
        if (!canTransition(from, to)) {
            throw new BusinessException("illegal payment order status transition: " + from + " -> " + to);
        }
    }
}
