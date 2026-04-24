package com.thaca.gateway.config;

import com.thaca.common.dtos.TokenPair;
import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.common.enums.TokenStatus;
import com.thaca.framework.core.configs.FrameworkProperties;
import com.thaca.framework.core.constants.CommonConstants;
import com.thaca.framework.core.dtos.ApiBody;
import com.thaca.framework.core.dtos.ApiHeader;
import com.thaca.framework.core.dtos.ApiPayload;
import com.thaca.framework.core.utils.CommonUtils;
import com.thaca.framework.core.utils.JsonF;
import com.thaca.framework.reactive.starter.utils.JwtUtils;
import com.thaca.framework.reactive.starter.utils.ReactiveCookieUtils;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
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
    private final ReactiveCookieUtils reactiveCookieUtils;

    @Override
    public int getOrder() {
        return -1;
    }

    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull GatewayFilterChain chain) {
        TokenPair tokens = resolveTokens(exchange);
        String accessToken = tokens.accessToken();
        String refreshToken = tokens.refreshToken();

        if (!StringUtils.hasText(accessToken)) {
            if (!StringUtils.hasText(refreshToken)) {
                return chain.filter(exchange);
            }
            return refreshAndContinue(exchange, chain, refreshToken);
        }

        return jwtUtil
            .validateToken(accessToken)
            .flatMap(status -> {
                if (status.equals(TokenStatus.VALID)) {
                    return chain.filter(withAuthHeader(exchange, accessToken));
                }
                if (status.equals(TokenStatus.EXPIRED) && StringUtils.hasText(refreshToken)) {
                    return refreshAndContinue(exchange, chain, refreshToken);
                }
                return chain.filter(exchange);
            })
            .onErrorResume(e -> {
                return chain.filter(exchange);
            });
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
            .cookie(CommonConstants.COOKIE_NAME, refreshToken)
            .header(HttpHeaders.AUTHORIZATION, "Basic " + frameworkProperties.getHttpClient().getApiKey())
            .bodyValue(reqBody)
            .retrieve()
            .bodyToMono(RefreshResponse.class)
            .flatMap(res -> {
                if (res == null || res.getData() == null) {
                    return unauthenticated(exchange.getResponse());
                }
                TokenPair newTokens = res.getData();
                exchange
                    .getResponse()
                    .addCookie(reactiveCookieUtils.setTokenCookie(newTokens.accessToken(), newTokens.refreshToken()));
                return chain.filter(withAuthHeader(exchange, newTokens.accessToken()));
            })
            .onErrorResume(e -> {
                log.error("Failed to refresh token", e);
                return unauthenticated(exchange.getResponse());
            });
    }

    private ServerWebExchange withAuthHeader(ServerWebExchange exchange, String accessToken) {
        ServerHttpRequest request = exchange
            .getRequest()
            .mutate()
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .build();
        return exchange.mutate().request(request).build();
    }

    private Mono<Void> unauthenticated(ServerHttpResponse response) {
        String body = JsonF.toJson(ApiPayload.error(CommonErrorMessage.UNAUTHORIZED));
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.addCookie(reactiveCookieUtils.deleteCookie());
        return response.writeWith(
            Mono.just(response.bufferFactory().wrap(body != null ? body.getBytes() : new byte[0]))
        );
    }

    private TokenPair resolveTokens(ServerWebExchange exchange) {
        List<String> authHeaders = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);

        if (!CollectionUtils.isEmpty(authHeaders)) {
            String bearer = authHeaders.getFirst().replace("Bearer ", "");
            return new TokenPair(bearer, null);
        }

        String cookieHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.COOKIE);
        return CommonUtils.tokenFromCookie(cookieHeader).orElseGet(() -> new TokenPair(null, null));
    }

    @Getter
    @Setter
    public static class RefreshResponse {

        private TokenPair data;
    }
}
