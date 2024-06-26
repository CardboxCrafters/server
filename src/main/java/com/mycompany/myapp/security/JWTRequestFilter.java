package com.mycompany.myapp.security;

import com.mycompany.myapp.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JWTRequestFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private static final List<String> PERMITTED_ENDPOINTS = Arrays.asList("/name", "/sms-certification/send","/sms-certification/confirm");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");
        log.info("authorization:{}",authorizationHeader);
        final String requestURI = request.getRequestURI();

        if (isPermittedEndpoint(requestURI) || authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            // 토큰이 필요하지 않거나 헤더가 존재하지 않는 엔드포인트에 대한 처리
            filterChain.doFilter(request, response);
            return;
        }
        Long userId = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            userId = jwtUtil.extractId(jwt);

            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userId, null, List.of(new SimpleGrantedAuthority("USER")));
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                } catch (Exception e) {
                    log.error("JWT token validation failed: {}", e.getMessage());
                }
            }
        }
        filterChain.doFilter(request, response);
    }
    private boolean isPermittedEndpoint(String requestURI) {
        return PERMITTED_ENDPOINTS.contains(requestURI);
    }
}