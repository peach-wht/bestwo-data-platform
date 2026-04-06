package com.bestwo.dataplatform.order.controller;

import com.bestwo.dataplatform.order.service.PaymentNotifyService;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pay/wechat")
public class PayNotifyController {

    private final PaymentNotifyService paymentNotifyService;

    public PayNotifyController(PaymentNotifyService paymentNotifyService) {
        this.paymentNotifyService = paymentNotifyService;
    }

    @PostMapping("/notify")
    public ResponseEntity<Map<String, String>> notify(
        @RequestBody String requestBody,
        @RequestHeader HttpHeaders headers
    ) {
        try {
            paymentNotifyService.handleWeChatPayNotify(requestBody, headers);
            return ResponseEntity.ok(Map.of("code", "SUCCESS", "message", "success"));
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("code", "FAIL", "message", exception.getMessage() == null ? "notify failed" : exception.getMessage()));
        }
    }
}
