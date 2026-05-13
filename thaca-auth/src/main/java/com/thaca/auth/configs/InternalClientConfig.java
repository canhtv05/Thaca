package com.thaca.auth.configs;

import com.thaca.auth.clients.CmsClient;
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
    CmsClient cmsClient() {
        return proxyFactory.create(CmsClient.class, frameworkProperties.getRoutes().getCmsService() + "/internal");
    }
}
