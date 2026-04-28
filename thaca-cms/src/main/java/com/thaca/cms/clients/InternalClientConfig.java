package com.thaca.cms.clients;

import com.thaca.framework.blocking.starter.services.InternalApiProxyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class InternalClientConfig {

    private final InternalApiProxyFactory proxyFactory;

    @Bean
    AuthClient authClient() {
        return proxyFactory.create(AuthClient.class);
    }
}
