package com.thaca.gateway.config;

import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.common.enums.TokenStatus;
import com.thaca.framework.core.configs.FrameworkProperties;
import com.thaca.framework.core.constants.CommonConstants;
import com.thaca.framework.core.dtos.ApiBody;
import com.thaca.framework.core.dtos.ApiHeader;
import com.thaca.framework.core.dtos.ApiPayload;
import com.thaca.framework.core.utils.JsonF;
import com.thaca.framework.reactive.starter.utils.JwtUtils;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final FrameworkProperties frameworkProperties;
    private final JwtUtils jwtUtil;

    @Override
    public int getOrder() {
        return -1;
    }

    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull GatewayFilterChain chain) {
        String accessToken = resolveAccessToken(exchange);
        if (StringUtils.hasText(accessToken)) {
            return jwtUtil
                .validateToken(accessToken)
                .flatMap(status -> {
                    if (status.equals(TokenStatus.VALID)) {
                        return chain.filter(withAuthHeader(exchange, accessToken));
                    }
                    if (status.equals(TokenStatus.EXPIRED)) {
                        String refreshToken = resolveRefreshToken(exchange);
                        if (StringUtils.hasText(refreshToken)) {
                            return refreshAndContinue(exchange, chain, refreshToken);
                        }
                    }
                    return chain.filter(exchange);
                })
                .onErrorResume(e -> chain.filter(exchange));
        }
        String refreshToken = resolveRefreshToken(exchange);
        if (StringUtils.hasText(refreshToken)) {
            return refreshAndContinue(exchange, chain, refreshToken);
        }
        return chain.filter(exchange);
    }

    private Mono<Void> refreshAndContinue(ServerWebExchange exchange, GatewayFilterChain chain, String refreshToken) {
        ApiPayload<Object> reqBody = new ApiPayload<>();
        reqBody.setHeader(new ApiHeader());
        reqBody.setBody(new ApiBody<>());

        String authServiceUrl = frameworkProperties.getRoutes().getAuthService();
        return WebClient.builder()
            .build()
            .post()
            .uri(authServiceUrl + "/internal/refresh-token")
            .cookie(CommonConstants.REFRESH_COOKIE_NAME, refreshToken)
            .header(HttpHeaders.AUTHORIZATION, "Basic " + frameworkProperties.getHttpClient().getApiKey())
            .bodyValue(reqBody)
            .retrieve()
            .bodyToMono(RefreshResponse.class)
            .flatMap(res -> {
                if (res == null || res.getData() == null || res.getData().getAccessToken() == null) {
                    return unauthenticated(exchange.getResponse());
                }
                String newAccessToken = res.getData().getAccessToken();
                return chain.filter(withAuthHeader(exchange, newAccessToken));
            })
            .onErrorResume(e -> {
                log.error("Failed to refresh token", e);
                return unauthenticated(exchange.getResponse());
            });
    }

    private String resolveAccessToken(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private String resolveRefreshToken(ServerWebExchange exchange) {
        HttpCookie cookie = exchange.getRequest().getCookies().getFirst(CommonConstants.REFRESH_COOKIE_NAME);
        return cookie != null ? cookie.getValue() : null;
    }

    private ServerWebExchange withAuthHeader(ServerWebExchange exchange, String accessToken) {
        ServerHttpRequest request = exchange
            .getRequest()
            .mutate()
            .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
            .build();
        return exchange.mutate().request(request).build();
    }

    private Mono<Void> unauthenticated(ServerHttpResponse response) {
        String body = JsonF.toJson(ApiPayload.error(CommonErrorMessage.UNAUTHORIZED));
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.addCookie(
            ResponseCookie.from(CommonConstants.REFRESH_COOKIE_NAME, "")
                .httpOnly(true)
                .maxAge(0)
                .path("/")
                .secure(false)
                .sameSite("Strict")
                .build()
        );
        return response.writeWith(
            Mono.just(response.bufferFactory().wrap(body != null ? body.getBytes() : new byte[0]))
        );
    }

    @Getter
    @Setter
    public static class RefreshResponse {

        private RefreshData data;
    }

    @Getter
    @Setter
    public static class RefreshData {

        private String accessToken;
    }
}
