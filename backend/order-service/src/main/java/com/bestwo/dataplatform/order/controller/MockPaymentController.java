package com.bestwo.dataplatform.order.controller;

import com.bestwo.dataplatform.common.api.ApiResponse;
import com.bestwo.dataplatform.order.dto.MockPaymentActionRequest;
import com.bestwo.dataplatform.order.dto.MockPaymentQueryResponse;
import com.bestwo.dataplatform.order.service.MockPaymentService;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pay")
public class MockPaymentController {

    private final MockPaymentService mockPaymentService;

    public MockPaymentController(MockPaymentService mockPaymentService) {
        this.mockPaymentService = mockPaymentService;
    }

    @GetMapping("/mock-payments/{paymentOrderNo}")
    public ApiResponse<MockPaymentQueryResponse> getMockPayment(@PathVariable String paymentOrderNo) {
        return ApiResponse.success(mockPaymentService.getMockPayment(paymentOrderNo));
    }

    @PostMapping("/mock-payments/{paymentOrderNo}/success")
    public ApiResponse<MockPaymentQueryResponse> mockSuccess(
        @PathVariable String paymentOrderNo,
        @RequestBody(required = false) MockPaymentActionRequest request
    ) {
        return ApiResponse.success(mockPaymentService.mockSuccess(paymentOrderNo, request));
    }

    @PostMapping("/mock-payments/{paymentOrderNo}/fail")
    public ApiResponse<MockPaymentQueryResponse> mockFail(
        @PathVariable String paymentOrderNo,
        @RequestBody(required = false) MockPaymentActionRequest request
    ) {
        return ApiResponse.success(mockPaymentService.mockFail(paymentOrderNo, request));
    }

    @PostMapping("/mock/notify")
    public ResponseEntity<Map<String, String>> notify(@RequestBody String requestBody) {
        try {
            mockPaymentService.handleMockNotify(requestBody);
            return ResponseEntity.ok(Map.of("code", "SUCCESS", "message", "success"));
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("code", "FAIL", "message", exception.getMessage() == null ? "notify failed" : exception.getMessage()));
        }
    }
}
