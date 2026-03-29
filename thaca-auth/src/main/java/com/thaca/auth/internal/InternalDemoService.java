package com.thaca.auth.internal;

import com.thaca.framework.core.dtos.ApiPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Example Internal Service that uses the configured RestTemplate
 * to communicate with other microservices using API Key Authentication.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InternalDemoService {

    private final RestTemplate restTemplate;

    /**
     * Calls an internal API of another service.
     * The RestTemplateInterceptor will automatically add the 'Authorization: Basic
     * <apiKey>' header.
     */
    public ApiPayload<Object> callOtherService() {
        String url = "http://internal-product-service/api/v1/internal/data";

        log.info("[InternalDemoService] Calling other service at: {}", url);

        try {
            // The response is expected to be wrapped in ApiResponse
            ApiPayload<Object> response = restTemplate.getForObject(url, ApiPayload.class);
            return response;
        } catch (Exception e) {
            log.error("[InternalDemoService] Error calling other service: ", e);
            throw e;
        }
    }
}
