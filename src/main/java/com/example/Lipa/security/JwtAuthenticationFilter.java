package com.example.Lipa.security;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenValidator jwtValidator;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(req, res);
            return;
        }
        String token = header.substring(7);
        try {
            if (jwtValidator.isValid(token)) {
                UUID userId    = jwtValidator.extractUserId(token);
                String email   = jwtValidator.extractEmail(token);
                List<String> roles = jwtValidator.extractRoles(token);

                UserPrincipal principal = new UserPrincipal(userId, email, roles);
                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            log.warn("JWT validation failed: {}", e.getMessage());
        }
        chain.doFilter(req, res);
    }
}