package com.bestwo.dataplatform.gateway.dto;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public record AuthUserResponse(String username, List<String> roles) {

    public static AuthUserResponse from(Authentication authentication) {
        List<String> roles = authentication.getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .map(authority -> authority.startsWith("ROLE_") ? authority.substring(5) : authority)
            .toList();

        return new AuthUserResponse(authentication.getName(), roles);
    }
}
