package com.bestwo.dataplatform.gateway.controller;

import com.bestwo.dataplatform.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping({"/health", "/api/health"})
public class HealthController {

    @GetMapping
    public Mono<ApiResponse<String>> health() {
        return Mono.just(ApiResponse.success("gateway-service ok"));
    }
}
