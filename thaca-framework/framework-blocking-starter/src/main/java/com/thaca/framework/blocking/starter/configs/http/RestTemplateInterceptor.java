package com.thaca.framework.blocking.starter.configs.http;

import com.thaca.framework.core.configs.FrameworkProperties;
import com.thaca.framework.core.constants.FwHttpHeaderConstants;
import com.thaca.framework.core.context.FwContextHeader;
import com.thaca.framework.core.dtos.ApiHeader;
import java.io.IOException;
import lombok.NonNull;
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

    @Override
    public @NullMarked @NonNull ClientHttpResponse intercept(
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
        if (StringUtils.hasText(traceId) && request.getHeaders().get(FwHttpHeaderConstants.TRACE_ID_HEADER) == null) {
            request.getHeaders().add(FwHttpHeaderConstants.TRACE_ID_HEADER, traceId);
        }

        String transId = MDC.get("transId");
        if (StringUtils.hasText(transId) && request.getHeaders().get(FwHttpHeaderConstants.SPAN_ID_HEADER) == null) {
            request.getHeaders().add(FwHttpHeaderConstants.SPAN_ID_HEADER, transId);
        }

        return execution.execute(request, body);
    }
}
