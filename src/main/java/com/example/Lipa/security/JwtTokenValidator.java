package com.example.Lipa.security;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class JwtTokenValidator {

    private final JwtDecoder jwtDecoder;

    public JwtTokenValidator(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    public boolean isValid(String token) {
        try {
            jwtDecoder.decode(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(decode(token).getSubject());
    }

    public String extractEmail(String token) {
        return decode(token).getClaimAsString("email");
    }

    public List<String> extractRoles(String token) {
        return decode(token).getClaimAsStringList("roles");
    }

    private Jwt decode(String token) {
        return jwtDecoder.decode(token);
    }
}