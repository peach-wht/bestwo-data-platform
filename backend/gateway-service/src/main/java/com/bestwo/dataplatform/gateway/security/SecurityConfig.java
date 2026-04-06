package com.bestwo.dataplatform.gateway.security;

import com.bestwo.dataplatform.common.api.ApiResponse;
import com.bestwo.dataplatform.gateway.config.AdminAuthProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@EnableConfigurationProperties(AdminAuthProperties.class)
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public MapReactiveUserDetailsService reactiveUserDetailsService(AdminAuthProperties adminAuthProperties) {
        UserDetails admin = User.withUsername(adminAuthProperties.getUsername())
            .password(adminAuthProperties.getPasswordHash())
            .roles(adminAuthProperties.getRoles().toArray(String[]::new))
            .build();

        return new MapReactiveUserDetailsService(admin);
    }

    @Bean
    public ReactiveAuthenticationManager reactiveAuthenticationManager(
        MapReactiveUserDetailsService userDetailsService,
        PasswordEncoder passwordEncoder
    ) {
        UserDetailsRepositoryReactiveAuthenticationManager authenticationManager =
            new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        authenticationManager.setPasswordEncoder(passwordEncoder);
        return authenticationManager;
    }

    @Bean
    public ServerSecurityContextRepository securityContextRepository() {
        return new WebSessionServerSecurityContextRepository();
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(
        ServerHttpSecurity http,
        ServerSecurityContextRepository securityContextRepository,
        ObjectMapper objectMapper
    ) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
            .logout(ServerHttpSecurity.LogoutSpec::disable)
            .securityContextRepository(securityContextRepository)
            .authorizeExchange(exchange -> exchange
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                .pathMatchers("/health", "/api/health").permitAll()
                .pathMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .pathMatchers(HttpMethod.POST, "/api/pay/wechat/notify").permitAll()
                .anyExchange().authenticated()
            )
            .exceptionHandling(exceptionHandlingSpec -> exceptionHandlingSpec
                .authenticationEntryPoint((exchange, ex) ->
                    writeJson(exchange, HttpStatus.UNAUTHORIZED, ApiResponse.of(401, "未登录", null), objectMapper)
                )
                .accessDeniedHandler((exchange, ex) ->
                    writeJson(exchange, HttpStatus.FORBIDDEN, ApiResponse.of(403, "无权限", null), objectMapper)
                )
            )
            .build();
    }

    private Mono<Void> writeJson(
        ServerWebExchange exchange,
        HttpStatus status,
        ApiResponse<?> body,
        ObjectMapper objectMapper
    ) {
        byte[] responseBody;
        try {
            responseBody = objectMapper.writeValueAsString(body).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException ex) {
            responseBody = "{\"code\":500,\"message\":\"响应序列化失败\",\"data\":null}".getBytes(StandardCharsets.UTF_8);
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return exchange.getResponse().writeWith(
            Mono.just(exchange.getResponse().bufferFactory().wrap(responseBody))
        );
    }
}
