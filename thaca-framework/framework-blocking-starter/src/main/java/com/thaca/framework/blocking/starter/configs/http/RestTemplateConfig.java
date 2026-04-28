package com.thaca.framework.blocking.starter.configs.http;

import com.thaca.framework.core.configs.FrameworkProperties;
import java.net.http.HttpClient;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {

    private final FrameworkProperties frameworkProperties;

    @Bean
    RestTemplate restTemplate() {
        FrameworkProperties.HttpClientConfig config = frameworkProperties.getHttpClient();

        HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofMillis(config.getConnectTimeout()))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofMillis(config.getReadTimeout()));

        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.getInterceptors().add(new RestTemplateInterceptor(frameworkProperties));

        log.info(
            "[RestTemplateConfig] JDK HttpClient (HTTP/2), connectTimeout={}ms, readTimeout={}ms",
            config.getConnectTimeout(),
            config.getReadTimeout()
        );

        return restTemplate;
    }
}
