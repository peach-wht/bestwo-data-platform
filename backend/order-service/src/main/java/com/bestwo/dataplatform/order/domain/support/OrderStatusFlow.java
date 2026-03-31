package com.bestwo.dataplatform.order.domain.support;

import com.bestwo.dataplatform.common.exception.BusinessException;
import com.bestwo.dataplatform.order.domain.enums.OrderStatus;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class OrderStatusFlow {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(OrderStatus.CREATED, EnumSet.of(OrderStatus.WAIT_PAY, OrderStatus.CLOSED, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(
            OrderStatus.WAIT_PAY,
            EnumSet.of(OrderStatus.PAYING, OrderStatus.PAID, OrderStatus.CLOSED, OrderStatus.CANCELLED)
        );
        ALLOWED_TRANSITIONS.put(OrderStatus.PAYING, EnumSet.of(OrderStatus.WAIT_PAY, OrderStatus.PAID, OrderStatus.CLOSED));
        ALLOWED_TRANSITIONS.put(OrderStatus.PAID, EnumSet.of(OrderStatus.REFUNDING, OrderStatus.REFUNDED));
        ALLOWED_TRANSITIONS.put(OrderStatus.REFUNDING, EnumSet.of(OrderStatus.PAID, OrderStatus.REFUNDED));
        ALLOWED_TRANSITIONS.put(OrderStatus.CLOSED, EnumSet.noneOf(OrderStatus.class));
        ALLOWED_TRANSITIONS.put(OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class));
        ALLOWED_TRANSITIONS.put(OrderStatus.REFUNDED, EnumSet.noneOf(OrderStatus.class));
    }

    private OrderStatusFlow() {}

    public static boolean canTransition(OrderStatus from, OrderStatus to) {
        if (from == null || to == null) {
            return false;
        }
        if (from == to) {
            return true;
        }
        return ALLOWED_TRANSITIONS.getOrDefault(from, EnumSet.noneOf(OrderStatus.class)).contains(to);
    }

    public static void assertCanTransition(OrderStatus from, OrderStatus to) {
        if (!canTransition(from, to)) {
            throw new BusinessException("illegal order status transition: " + from + " -> " + to);
        }
    }
}
