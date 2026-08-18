package com.estatelink.gateway.filter;

import com.estatelink.gateway.security.JwtService;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Central JWT validation. Public endpoints (register, login, verify, active
 * listing browse, open inspection slots, health) pass through; everything else
 * must carry a valid token. Valid tokens get identity forwarded downstream.
 *
 * Downstream services still validate the same JWT themselves (defence in
 * depth); this filter is the first line of defence and the place where
 * unauthenticated traffic is rejected before it reaches a service.
 */
@Component
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    private static final String BEARER = "Bearer ";
    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    private final JwtService jwtService;

    public JwtAuthGlobalFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();

        if (isPublic(path, method)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith(BEARER)) {
            return reject(exchange, HttpStatus.UNAUTHORIZED, "{\"error\":\"Missing or invalid Authorization header\"}");
        }

        String token = authHeader.substring(BEARER.length());
        if (!jwtService.isTokenValid(token)) {
            return reject(exchange, HttpStatus.UNAUTHORIZED, "{\"error\":\"Unauthorized: invalid or expired token\"}");
        }

        String userId = jwtService.extractUserId(token).toString();
        String role = jwtService.extractRole(token);

        ServerHttpRequest mutated = request.mutate()
                .header("X-User-Id", userId)
                .header("X-User-Role", role)
                .build();

        return chain.filter(exchange.mutate().request(mutated).build());
    }

    private boolean isPublic(String path, String method) {
        if ("GET".equals(method) && MATCHER.match("/actuator/health", path)) {
            return true;
        }
        if ("POST".equals(method) && (MATCHER.match("/api/v1/auth/register", path)
                || MATCHER.match("/api/v1/auth/login", path))) {
            return true;
        }
        if ("GET".equals(method) && MATCHER.match("/api/v1/auth/verify", path)) {
            return true;
        }
        if ("GET".equals(method) && MATCHER.match("/api/v1/listings/**", path)) {
            return true;
        }
        if ("GET".equals(method) && MATCHER.match("/api/v1/properties/**", path)) {
            return true;
        }
        if ("GET".equals(method) && MATCHER.match("/api/v1/uploads/**", path)) {
            return true;
        }
        if ("GET".equals(method) && MATCHER.match("/api/v1/inspection-slots/listing/**", path)) {
            return true;
        }
        if ("GET".equals(method) && MATCHER.match("/api/v1/inspection-slots/{id}", path)) {
            return true;
        }
        return false;
    }

    private Mono<Void> reject(ServerWebExchange exchange, HttpStatus status, String body) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -2;
    }
}
