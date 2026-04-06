package com.bestwo.dataplatform.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bestwo.dataplatform.common.exception.BusinessException;
import com.bestwo.dataplatform.order.domain.enums.OrderPayStatus;
import com.bestwo.dataplatform.order.domain.enums.OrderStatus;
import com.bestwo.dataplatform.order.domain.enums.PayPlatform;
import com.bestwo.dataplatform.order.domain.enums.PayTradeType;
import com.bestwo.dataplatform.order.domain.model.BizOrder;
import com.bestwo.dataplatform.order.dto.CreateOrderRequest;
import com.bestwo.dataplatform.order.dto.CreateOrderResponse;
import com.bestwo.dataplatform.order.mapper.BizOrderMapper;
import com.bestwo.dataplatform.order.util.OrderNoGenerator;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class OrderCommandService {

    private static final String DEFAULT_BIZ_TYPE = "PAYMENT";
    private static final String DEFAULT_ORDER_SOURCE = "ADMIN";
    private static final String DEFAULT_CURRENCY = "CNY";
    private static final String DEFAULT_OPERATOR = "system";

    private final BizOrderMapper bizOrderMapper;

    public OrderCommandService(BizOrderMapper bizOrderMapper) {
        this.bizOrderMapper = bizOrderMapper;
    }

    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        Long payableAmountFen = request.getPayableAmountFen() == null
            ? request.getTotalAmountFen()
            : request.getPayableAmountFen();
        if (payableAmountFen > request.getTotalAmountFen()) {
            throw new BusinessException("payableAmountFen cannot be greater than totalAmountFen");
        }

        String preferredPayPlatform = StringUtils.hasText(request.getPreferredPayPlatform())
            ? PayPlatform.fromCode(request.getPreferredPayPlatform()).getCode()
            : PayPlatform.WECHAT_PAY.getCode();
        String preferredTradeType = StringUtils.hasText(request.getPreferredTradeType())
            ? PayTradeType.fromCode(request.getPreferredTradeType()).getCode()
            : PayTradeType.NATIVE.getCode();

        BizOrder order = new BizOrder();
        order.setOrderId(OrderNoGenerator.generateOrderId());
        order.setOrderNo(generateUniqueOrderNo());
        order.setExternalOrderNo(trimToNull(request.getExternalOrderNo()));
        order.setBizType(DEFAULT_BIZ_TYPE);
        order.setOrderSource(DEFAULT_ORDER_SOURCE);
        order.setOrderTitle(request.getOrderTitle().trim());
        order.setOrderDescription(StringUtils.hasText(request.getOrderDescription())
            ? request.getOrderDescription().trim()
            : request.getOrderTitle().trim());
        order.setBuyerId(trimToNull(request.getBuyerId()));
        order.setBuyerNickname(trimToNull(request.getBuyerNickname()));
        order.setCurrency(DEFAULT_CURRENCY);
        order.setTotalAmountFen(request.getTotalAmountFen());
        order.setPayableAmountFen(payableAmountFen);
        order.setPaidAmountFen(0L);
        order.setRefundedAmountFen(0L);
        order.setOrderStatus(OrderStatus.CREATED.getCode());
        order.setPayStatus(OrderPayStatus.UNPAID.getCode());
        order.setPreferredPayPlatform(preferredPayPlatform);
        order.setPreferredTradeType(preferredTradeType);
        order.setRemark(trimToNull(request.getRemark()));
        order.setExtJson(trimToNull(request.getExtJson()));
        order.setVersion(0);
        order.setCreatedBy(resolveOperator(request.getCreatedBy()));
        order.setUpdatedBy(resolveOperator(request.getCreatedBy()));

        Instant now = Instant.now();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        int affectedRows = bizOrderMapper.insert(order);
        if (affectedRows != 1) {
            throw new BusinessException("failed to create order");
        }

        CreateOrderResponse response = new CreateOrderResponse();
        response.setOrderId(order.getOrderId());
        response.setOrderNo(order.getOrderNo());
        response.setOrderStatus(order.getOrderStatus());
        response.setPayStatus(order.getPayStatus());
        response.setTotalAmountFen(order.getTotalAmountFen());
        response.setPayableAmountFen(order.getPayableAmountFen());
        response.setPreferredPayPlatform(order.getPreferredPayPlatform());
        response.setPreferredTradeType(order.getPreferredTradeType());
        response.setCreatedAt(order.getCreatedAt());
        return response;
    }

    private String generateUniqueOrderNo() {
        for (int i = 0; i < 5; i++) {
            String candidate = OrderNoGenerator.generateOrderNo();
            Long count = bizOrderMapper.selectCount(
                new LambdaQueryWrapper<BizOrder>().eq(BizOrder::getOrderNo, candidate)
            );
            if (count == null || count == 0) {
                return candidate;
            }
        }
        throw new BusinessException("failed to generate unique order number");
    }

    private String resolveOperator(String createdBy) {
        return StringUtils.hasText(createdBy) ? createdBy.trim() : DEFAULT_OPERATOR;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
