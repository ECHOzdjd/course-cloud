package com.zjgsu.wy.gateway.filter;

import com.zjgsu.wy.gateway.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * JWT 认证过滤器
 * 负责验证请求中的 JWT Token 并提取用户信息
 */
@Component
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    // 白名单路径（不需要认证）
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/api/auth/login",
            "/api/auth/register",
            "/actuator/health"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String path = request.getPath().value();

        log.debug("Processing request: {} {}", request.getMethod(), path);

        // 1. 白名单路径直接放行
        if (isWhiteList(path)) {
            log.debug("White list path, skip authentication: {}", path);
            return chain.filter(exchange);
        }

        // 2. 获取 Authorization 请求头
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        // 3. 提取 Token
        String token = authHeader.substring(7);

        // 4. 验证 Token
        if (!jwtUtil.validateToken(token)) {
            log.warn("Invalid or expired token for path: {}", path);
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        // 5. 解析 Token 获取用户信息
        String userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);

        if (userId == null || username == null) {
            log.warn("Failed to extract user info from token");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        log.info("User authenticated: userId={}, username={}, role={}, path={}", 
                userId, username, role, path);

        // 6. 将用户信息添加到请求头，传递给下游服务
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Id", userId)
                .header("X-Username", username)
                .header("X-User-Role", role != null ? role : "USER")
                .build();

        ServerWebExchange modifiedExchange = exchange.mutate()
                .request(modifiedRequest)
                .build();

        // 7. 转发请求
        return chain.filter(modifiedExchange);
    }

    /**
     * 判断路径是否在白名单中
     */
    private boolean isWhiteList(String path) {
        return WHITE_LIST.stream().anyMatch(path::startsWith);
    }

    /**
     * 过滤器优先级，数值越小优先级越高
     * -100 表示在其他过滤器之前执行
     */
    @Override
    public int getOrder() {
        return -100;
    }
}
