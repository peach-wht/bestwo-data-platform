package com.bestwo.dataplatform.gateway.controller;

import com.bestwo.dataplatform.common.api.ApiResponse;
import com.bestwo.dataplatform.gateway.dto.AuthLoginRequest;
import com.bestwo.dataplatform.gateway.dto.AuthUserResponse;
import java.time.Duration;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final ReactiveAuthenticationManager authenticationManager;
    private final ServerSecurityContextRepository securityContextRepository;

    public AuthController(
        ReactiveAuthenticationManager authenticationManager,
        ServerSecurityContextRepository securityContextRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<ApiResponse<AuthUserResponse>>> login(
        ServerWebExchange exchange,
        @RequestBody Mono<AuthLoginRequest> requestMono
    ) {
        return requestMono
            .defaultIfEmpty(new AuthLoginRequest("", ""))
            .flatMap(request -> {
                if (!StringUtils.hasText(request.username()) || !StringUtils.hasText(request.password())) {
                    return Mono.just(
                        ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(ApiResponse.<AuthUserResponse>of(400, "用户名和密码不能为空", null))
                    );
                }

                Authentication loginToken = UsernamePasswordAuthenticationToken.unauthenticated(
                    request.username(),
                    request.password()
                );

                return authenticationManager
                    .authenticate(loginToken)
                    .flatMap(authentication ->
                        securityContextRepository
                            .save(exchange, new SecurityContextImpl(authentication))
                            .thenReturn(
                                ResponseEntity.ok(
                                    ApiResponse.of(200, "success", AuthUserResponse.from(authentication))
                                )
                            )
                    )
                    .onErrorResume(BadCredentialsException.class, ex -> unauthorized())
                    .onErrorResume(UsernameNotFoundException.class, ex -> unauthorized())
                    .onErrorResume(AuthenticationCredentialsNotFoundException.class, ex -> unauthorized());
            });
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<ApiResponse<Void>>> logout(ServerWebExchange exchange) {
        exchange.getResponse().addCookie(
            ResponseCookie.from("SESSION", "")
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ZERO)
                .build()
        );

        return exchange.getSession()
            .flatMap(session ->
                session.invalidate().thenReturn(ResponseEntity.ok(ApiResponse.<Void>of(200, "success", null)))
            );
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<ApiResponse<AuthUserResponse>>> me(Mono<Principal> principalMono) {
        return principalMono.map(principal ->
            ResponseEntity.ok(ApiResponse.of(200, "success", AuthUserResponse.from((Authentication) principal)))
        );
    }

    private Mono<ResponseEntity<ApiResponse<AuthUserResponse>>> unauthorized() {
        return Mono.just(
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.<AuthUserResponse>of(401, "用户名或密码错误", null))
        );
    }
}
