package com.thaca.framework.reactive.starter.filter;

import com.thaca.common.dtos.TokenPair;
import com.thaca.framework.core.utils.CommonUtils;
import com.thaca.framework.reactive.starter.utils.JwtUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtFilter implements WebFilter {

    private final JwtUtils jwtUtils;

    @NonNull
    @Override
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        String jwt;
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
        } else {
            Optional<TokenPair> tokenPair = CommonUtils.tokenFromCookie(
                    exchange.getRequest().getHeaders().getFirst(HttpHeaders.COOKIE));
            jwt = tokenPair.map(TokenPair::accessToken).orElse(null);
        }

        return jwtUtils.getBasicAuthentication(jwt)
                .flatMap(authentication -> chain.filter(exchange).contextWrite(
                        ReactiveSecurityContextHolder.withSecurityContext(Mono.just(new SecurityContextImpl(authentication))))
                )
                .onErrorResume(ex -> chain.filter(exchange));
    }
}
