package com.estatelink.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory rate limiter (per client IP). Suitable for a single gateway
 * instance; for horizontal scaling swap this for Redis-backed
 * RequestRateLimiter with a real Redis limiter.
 */
@Component
public class RateLimitGlobalFilter implements GlobalFilter, Ordered {

    private static final int MAX_REQUESTS = 120;
    private static final long WINDOW_SECONDS = 60;

    private static final ConcurrentHashMap<String, Bucket> BUCKETS = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientIp = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";

        Bucket bucket = BUCKETS.computeIfAbsent(clientIp, k -> new Bucket());
        if (bucket.tryAcquire()) {
            return chain.filter(exchange);
        }

        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] bytes = "{\"error\":\"Too many requests. Please try again shortly.\"}"
                .getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private static final class Bucket {
        private final AtomicInteger count = new AtomicInteger();
        private volatile Instant windowStart = Instant.now();

        synchronized boolean tryAcquire() {
            Instant now = Instant.now();
            if (windowStart.plusSeconds(WINDOW_SECONDS).isBefore(now)) {
                windowStart = now;
                count.set(0);
            }
            return count.incrementAndGet() <= MAX_REQUESTS;
        }
    }
}
