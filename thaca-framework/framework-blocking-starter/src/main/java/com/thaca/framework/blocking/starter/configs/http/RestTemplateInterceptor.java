package com.thaca.framework.blocking.starter.configs.http;

import com.thaca.common.dtos.ApiHeader;
import com.thaca.framework.core.configs.FrameworkProperties;
import com.thaca.framework.core.context.FwContext;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class RestTemplateInterceptor implements ClientHttpRequestInterceptor {

    private final FrameworkProperties frameworkProperties;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
        throws IOException {
        FrameworkProperties.HttpClientConfig config = frameworkProperties.getHttpClient();

        ApiHeader contextHeader = FwContext.get();
        if (contextHeader != null) {
            if (StringUtils.hasText(contextHeader.getApiKey())) {
                request.getHeaders().add(HttpHeaders.AUTHORIZATION, "Basic " + contextHeader.getApiKey());
            }
        }

        if (request.getHeaders().get(HttpHeaders.AUTHORIZATION) == null && StringUtils.hasText(config.getApiKey())) {
            request.getHeaders().add(HttpHeaders.AUTHORIZATION, "Basic " + config.getApiKey());
        }

        return execution.execute(request, body);
    }
}
