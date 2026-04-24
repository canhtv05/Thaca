package com.thaca.framework.blocking.starter.services;

import com.thaca.common.dtos.ErrorData;
import com.thaca.framework.core.configs.FrameworkProperties;
import com.thaca.framework.core.context.FwContextHeader;
import com.thaca.framework.core.dtos.ApiBody;
import com.thaca.framework.core.dtos.ApiHeader;
import com.thaca.framework.core.dtos.ApiPayload;
import com.thaca.framework.core.enums.ChannelType;
import com.thaca.framework.core.exceptions.FwException;
import com.thaca.framework.core.utils.JsonF;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.core.type.TypeReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternalApiServiceClient {

    private final RestTemplate restTemplate;
    private final FrameworkProperties frameworkProperties;

    public <T, R> R post(String url, T requestData, ParameterizedTypeReference<R> responseType) {
        ApiHeader contextHeader = FwContextHeader.get();
        if (contextHeader == null) {
            contextHeader = ApiHeader.builder().build();
        }
        ApiBody<T> body = ApiBody.<T>builder()
            .transId(MDC.get("transId"))
            .status("OK")
            .data(requestData != null ? requestData : (T) new HashMap<>())
            .build();
        ApiPayload<T> payload = ApiPayload.<T>builder().header(contextHeader).body(body).build();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("X-Internal-Call", "true");
        String apiKey = StringUtils.defaultIfBlank(
            contextHeader.getApiKey(),
            frameworkProperties.getHttpClient().getApiKey()
        );
        httpHeaders.set(HttpHeaders.AUTHORIZATION, "Basic " + apiKey);

        org.springframework.web.context.request.ServletRequestAttributes attributes =
            (org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            jakarta.servlet.http.HttpServletRequest currentRequest = attributes.getRequest();
            String cookieHeader = currentRequest.getHeader(HttpHeaders.COOKIE);
            if (StringUtils.isNotBlank(cookieHeader)) {
                httpHeaders.add(HttpHeaders.COOKIE, cookieHeader);
            }
            String authHeader = currentRequest.getHeader(HttpHeaders.AUTHORIZATION);
            if (StringUtils.isNotBlank(authHeader) && authHeader.startsWith("Bearer ")) {
                httpHeaders.add(HttpHeaders.AUTHORIZATION, authHeader);
            }
            String userAgent = currentRequest.getHeader(HttpHeaders.USER_AGENT);
            if (StringUtils.isNotBlank(userAgent)) {
                httpHeaders.set(HttpHeaders.USER_AGENT, userAgent);
            }
        }

        try {
            ResponseEntity<ApiPayload<R>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(payload, httpHeaders),
                new ParameterizedTypeReference<>() {}
            );

            List<String> setCookieHeaders = response.getHeaders().get(HttpHeaders.SET_COOKIE);
            if (setCookieHeaders != null && !setCookieHeaders.isEmpty()) {
                org.springframework.web.context.request.ServletRequestAttributes respAttributes =
                    (org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
                if (respAttributes != null) {
                    jakarta.servlet.http.HttpServletResponse currentResponse = respAttributes.getResponse();
                    if (currentResponse != null) {
                        for (String cookie : setCookieHeaders) {
                            currentResponse.addHeader(HttpHeaders.SET_COOKIE, cookie);
                        }
                    }
                }
            }

            ApiPayload<R> responsePayload = response.getBody();
            if (responsePayload != null && responsePayload.getBody() != null) {
                String rawJson = JsonF.toJson(responsePayload.getBody().getData());
                return JsonF.jsonToObject(rawJson, responseType);
            }
            return null;
        } catch (HttpStatusCodeException ex) {
            String responseBodyAsString = ex.getResponseBodyAsString(StandardCharsets.UTF_8);
            if (!responseBodyAsString.isEmpty()) {
                try {
                    ApiPayload<ErrorData> errorPayload = JsonF.jsonToObject(
                        responseBodyAsString,
                        new TypeReference<>() {}
                    );
                    if (
                        errorPayload != null &&
                        errorPayload.getBody() != null &&
                        errorPayload.getBody().getData() != null
                    ) {
                        ErrorData errorData = errorPayload.getBody().getData();
                        if (errorData.code() != null) {
                            throw new FwException(errorData);
                        }
                    }
                } catch (FwException fwEx) {
                    throw fwEx;
                } catch (Exception parseEx) {
                    log.error("[InternalApiClient] Error parsing error response: {}", parseEx.getMessage());
                }
            }
            throw ex;
        }
    }
}
