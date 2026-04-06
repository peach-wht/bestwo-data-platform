package com.bestwo.dataplatform.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bestwo.dataplatform.common.exception.BusinessException;
import com.bestwo.dataplatform.order.domain.enums.OrderPayStatus;
import com.bestwo.dataplatform.order.domain.enums.OrderStatus;
import com.bestwo.dataplatform.order.domain.model.BizOrder;
import com.bestwo.dataplatform.order.domain.model.BizOrderItem;
import com.bestwo.dataplatform.order.dto.OrderDetailResponse;
import com.bestwo.dataplatform.order.dto.OrderItemResponse;
import com.bestwo.dataplatform.order.dto.OrderListItemResponse;
import com.bestwo.dataplatform.order.dto.OrderPageResponse;
import com.bestwo.dataplatform.order.dto.OrderQueryRequest;
import com.bestwo.dataplatform.order.mapper.BizOrderItemMapper;
import com.bestwo.dataplatform.order.mapper.BizOrderMapper;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class OrderQueryService {

    private final BizOrderMapper bizOrderMapper;
    private final BizOrderItemMapper bizOrderItemMapper;

    public OrderQueryService(BizOrderMapper bizOrderMapper, BizOrderItemMapper bizOrderItemMapper) {
        this.bizOrderMapper = bizOrderMapper;
        this.bizOrderItemMapper = bizOrderItemMapper;
    }

    public OrderPageResponse queryOrders(OrderQueryRequest request) {
        validateQueryRequest(request);

        LambdaQueryWrapper<BizOrder> wrapper = new LambdaQueryWrapper<BizOrder>()
            .orderByDesc(BizOrder::getCreatedAt);

        if (StringUtils.hasText(request.getKeyword())) {
            String keyword = request.getKeyword().trim();
            wrapper.and(condition -> condition
                .like(BizOrder::getOrderNo, keyword)
                .or()
                .like(BizOrder::getOrderTitle, keyword)
                .or()
                .like(BizOrder::getBuyerNickname, keyword)
                .or()
                .like(BizOrder::getExternalOrderNo, keyword)
            );
        }
        if (StringUtils.hasText(request.getOrderStatus())) {
            wrapper.eq(BizOrder::getOrderStatus, OrderStatus.fromCode(request.getOrderStatus()).getCode());
        }
        if (StringUtils.hasText(request.getPayStatus())) {
            wrapper.eq(BizOrder::getPayStatus, OrderPayStatus.fromCode(request.getPayStatus()).getCode());
        }

        Page<BizOrder> page = bizOrderMapper.selectPage(new Page<>(request.getPageNum(), request.getPageSize()), wrapper);

        OrderPageResponse response = new OrderPageResponse();
        response.setList(page.getRecords().stream().map(this::toOrderListItemResponse).collect(Collectors.toList()));
        response.setPageNum(page.getCurrent());
        response.setPageSize(page.getSize());
        response.setTotal(page.getTotal());
        return response;
    }

    public OrderDetailResponse getOrderDetail(String idOrOrderNo) {
        BizOrder order = findOrder(idOrOrderNo);
        if (order == null) {
            throw new BusinessException("order not found");
        }

        OrderDetailResponse response = toOrderDetailResponse(order);
        List<OrderItemResponse> items = bizOrderItemMapper.selectList(
                new LambdaQueryWrapper<BizOrderItem>()
                    .eq(BizOrderItem::getOrderId, order.getOrderId())
                    .orderByAsc(BizOrderItem::getLineNo)
            )
            .stream()
            .map(this::toOrderItemResponse)
            .collect(Collectors.toList());
        response.setItems(items);
        return response;
    }

    private void validateQueryRequest(OrderQueryRequest request) {
        if (request.getPageNum() < 1) {
            throw new BusinessException("pageNum must be greater than or equal to 1");
        }
        if (request.getPageSize() < 1 || request.getPageSize() > 100) {
            throw new BusinessException("pageSize must be between 1 and 100");
        }
    }

    private BizOrder findOrder(String idOrOrderNo) {
        String keyword = idOrOrderNo == null ? "" : idOrOrderNo.trim();
        if (!StringUtils.hasText(keyword)) {
            throw new BusinessException("order id is required");
        }

        LambdaQueryWrapper<BizOrder> wrapper = new LambdaQueryWrapper<BizOrder>()
            .eq(BizOrder::getOrderId, keyword)
            .or()
            .eq(BizOrder::getOrderNo, keyword);

        if (keyword.chars().allMatch(Character::isDigit)) {
            wrapper.or().eq(BizOrder::getId, Long.parseLong(keyword));
        }

        return bizOrderMapper.selectOne(wrapper.last("LIMIT 1"));
    }

    private OrderListItemResponse toOrderListItemResponse(BizOrder order) {
        OrderListItemResponse response = new OrderListItemResponse();
        response.setOrderId(order.getOrderId());
        response.setOrderNo(order.getOrderNo());
        response.setOrderTitle(order.getOrderTitle());
        response.setBuyerId(order.getBuyerId());
        response.setBuyerNickname(order.getBuyerNickname());
        response.setTotalAmountFen(order.getTotalAmountFen());
        response.setPayableAmountFen(order.getPayableAmountFen());
        response.setPaidAmountFen(order.getPaidAmountFen());
        response.setOrderStatus(order.getOrderStatus());
        response.setPayStatus(order.getPayStatus());
        response.setPreferredPayPlatform(order.getPreferredPayPlatform());
        response.setPreferredTradeType(order.getPreferredTradeType());
        response.setLatestPaymentOrderNo(order.getLatestPaymentOrderNo());
        response.setPaidTime(order.getPaidTime());
        response.setCreatedAt(order.getCreatedAt());
        return response;
    }

    private OrderDetailResponse toOrderDetailResponse(BizOrder order) {
        OrderDetailResponse response = new OrderDetailResponse();
        response.setOrderId(order.getOrderId());
        response.setOrderNo(order.getOrderNo());
        response.setExternalOrderNo(order.getExternalOrderNo());
        response.setBizType(order.getBizType());
        response.setOrderSource(order.getOrderSource());
        response.setOrderTitle(order.getOrderTitle());
        response.setOrderDescription(order.getOrderDescription());
        response.setBuyerId(order.getBuyerId());
        response.setBuyerNickname(order.getBuyerNickname());
        response.setCurrency(order.getCurrency());
        response.setTotalAmountFen(order.getTotalAmountFen());
        response.setPayableAmountFen(order.getPayableAmountFen());
        response.setPaidAmountFen(order.getPaidAmountFen());
        response.setRefundedAmountFen(order.getRefundedAmountFen());
        response.setOrderStatus(order.getOrderStatus());
        response.setPayStatus(order.getPayStatus());
        response.setPreferredPayPlatform(order.getPreferredPayPlatform());
        response.setPreferredTradeType(order.getPreferredTradeType());
        response.setSuccessPayPlatform(order.getSuccessPayPlatform());
        response.setLatestPaymentOrderNo(order.getLatestPaymentOrderNo());
        response.setLatestChannelOrderNo(order.getLatestChannelOrderNo());
        response.setPaidTime(order.getPaidTime());
        response.setExpiredTime(order.getExpiredTime());
        response.setClosedTime(order.getClosedTime());
        response.setCancelledTime(order.getCancelledTime());
        response.setRemark(order.getRemark());
        response.setExtJson(order.getExtJson());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        return response;
    }

    private OrderItemResponse toOrderItemResponse(BizOrderItem item) {
        OrderItemResponse response = new OrderItemResponse();
        response.setOrderItemId(item.getOrderItemId());
        response.setLineNo(item.getLineNo());
        response.setItemType(item.getItemType());
        response.setProductId(item.getProductId());
        response.setProductCode(item.getProductCode());
        response.setProductName(item.getProductName());
        response.setSkuId(item.getSkuId());
        response.setSkuCode(item.getSkuCode());
        response.setSkuName(item.getSkuName());
        response.setQuantity(item.getQuantity());
        response.setCurrency(item.getCurrency());
        response.setUnitPriceFen(item.getUnitPriceFen());
        response.setOriginalAmountFen(item.getOriginalAmountFen());
        response.setDiscountAmountFen(item.getDiscountAmountFen());
        response.setPayableAmountFen(item.getPayableAmountFen());
        response.setPaidAmountFen(item.getPaidAmountFen());
        response.setRefundAmountFen(item.getRefundAmountFen());
        response.setItemStatus(item.getItemStatus());
        response.setRemark(item.getRemark());
        response.setCreatedAt(item.getCreatedAt());
        return response;
    }
}
