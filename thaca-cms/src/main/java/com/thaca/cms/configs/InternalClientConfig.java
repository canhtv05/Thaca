package com.thaca.cms.configs;

import com.thaca.cms.clients.AuthClient;
import com.thaca.framework.blocking.starter.services.InternalApiProxyFactory;
import com.thaca.framework.core.configs.FrameworkProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class InternalClientConfig {

    private final InternalApiProxyFactory proxyFactory;
    private final FrameworkProperties frameworkProperties;

    @Bean
    AuthClient authClient() {
        return proxyFactory.create(AuthClient.class, frameworkProperties.getRoutes().getAuthService() + "/internal");
    }
}
