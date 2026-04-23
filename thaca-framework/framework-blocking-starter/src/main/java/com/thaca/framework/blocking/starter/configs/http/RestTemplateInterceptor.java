package com.thaca.framework.blocking.starter.configs.http;

import com.thaca.framework.core.configs.FrameworkProperties;
import com.thaca.framework.core.context.FwContextHeader;
import com.thaca.framework.core.dtos.ApiHeader;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class RestTemplateInterceptor implements ClientHttpRequestInterceptor {

    private final FrameworkProperties frameworkProperties;
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String PARENT_TRANS_ID_HEADER = "X-Parent-Trans-Id";

    @Override
    public @NullMarked ClientHttpResponse intercept(
        HttpRequest request,
        byte[] body,
        ClientHttpRequestExecution execution
    ) throws IOException {
        FrameworkProperties.HttpClientConfig config = frameworkProperties.getHttpClient();

        ApiHeader contextHeader = FwContextHeader.get();
        if (contextHeader != null) {
            if (StringUtils.hasText(contextHeader.getApiKey())) {
                request.getHeaders().add(HttpHeaders.AUTHORIZATION, "Basic " + contextHeader.getApiKey());
            }
        }

        if (request.getHeaders().get(HttpHeaders.AUTHORIZATION) == null && StringUtils.hasText(config.getApiKey())) {
            request.getHeaders().add(HttpHeaders.AUTHORIZATION, "Basic " + config.getApiKey());
        }

        String traceId = MDC.get("traceId");
        if (StringUtils.hasText(traceId) && request.getHeaders().get(TRACE_ID_HEADER) == null) {
            request.getHeaders().add(TRACE_ID_HEADER, traceId);
        }

        String transId = MDC.get("transId");
        if (StringUtils.hasText(transId) && request.getHeaders().get(PARENT_TRANS_ID_HEADER) == null) {
            request.getHeaders().add(PARENT_TRANS_ID_HEADER, transId);
        }

        return execution.execute(request, body);
    }
}
