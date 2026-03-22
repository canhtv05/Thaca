package com.thaca.framework.blocking.starter.configs.http;

import com.thaca.framework.core.configs.FrameworkProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {

    private final FrameworkProperties frameworkProperties;

    @Bean
    RestTemplate restTemplate() {
        FrameworkProperties.HttpClientConfig config = frameworkProperties.getHttpClient();

        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory(config));
        restTemplate.getInterceptors().add(new RestTemplateInterceptor(frameworkProperties));

        return restTemplate;
    }

    private ClientHttpRequestFactory clientHttpRequestFactory(FrameworkProperties.HttpClientConfig config) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(config.getConnectTimeout());
        factory.setReadTimeout(config.getReadTimeout());
        return factory;
    }
}
