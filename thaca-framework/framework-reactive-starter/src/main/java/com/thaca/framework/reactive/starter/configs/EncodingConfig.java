package com.thaca.framework.reactive.starter.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.server.WebFilter;

@Configuration
public class EncodingConfig {

    @Bean
    WebFilter encodingFilter() {
        return (exchange, chain) -> {
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            exchange
                .getResponse()
                .getHeaders()
                .set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
            return chain.filter(exchange);
        };
    }
}
