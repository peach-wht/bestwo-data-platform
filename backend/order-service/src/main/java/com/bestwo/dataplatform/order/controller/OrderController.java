package com.bestwo.dataplatform.order.controller;

import com.bestwo.dataplatform.common.api.ApiResponse;
import com.bestwo.dataplatform.order.dto.CreateOrderRequest;
import com.bestwo.dataplatform.order.dto.CreateOrderResponse;
import com.bestwo.dataplatform.order.dto.OrderDetailResponse;
import com.bestwo.dataplatform.order.dto.OrderPageResponse;
import com.bestwo.dataplatform.order.dto.OrderPrepayResponse;
import com.bestwo.dataplatform.order.dto.OrderQueryRequest;
import com.bestwo.dataplatform.order.service.OrderCommandService;
import com.bestwo.dataplatform.order.service.OrderQueryService;
import com.bestwo.dataplatform.order.service.PaymentCommandService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderCommandService orderCommandService;
    private final OrderQueryService orderQueryService;
    private final PaymentCommandService paymentCommandService;

    public OrderController(
        OrderCommandService orderCommandService,
        OrderQueryService orderQueryService,
        PaymentCommandService paymentCommandService
    ) {
        this.orderCommandService = orderCommandService;
        this.orderQueryService = orderQueryService;
        this.paymentCommandService = paymentCommandService;
    }

    @PostMapping
    public ApiResponse<CreateOrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return ApiResponse.success(orderCommandService.createOrder(request));
    }

    @GetMapping
    public ApiResponse<OrderPageResponse> queryOrders(@Valid @ModelAttribute OrderQueryRequest request) {
        return ApiResponse.success(orderQueryService.queryOrders(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderDetailResponse> getOrderDetail(@PathVariable("id") String id) {
        return ApiResponse.success(orderQueryService.getOrderDetail(id));
    }

    @PostMapping("/{id}/pay")
    public ApiResponse<OrderPrepayResponse> prepay(@PathVariable("id") String id, HttpServletRequest request) {
        return ApiResponse.success(paymentCommandService.prepay(id, resolveClientIp(request)));
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
