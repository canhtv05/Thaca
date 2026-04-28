package com.thaca.framework.blocking.starter.services;

import com.thaca.common.dtos.ErrorData;
import com.thaca.framework.core.configs.FrameworkProperties;
import com.thaca.framework.core.constants.FwHttpHeaderConstants;
import com.thaca.framework.core.context.FwContextHeader;
import com.thaca.framework.core.dtos.ApiBody;
import com.thaca.framework.core.dtos.ApiHeader;
import com.thaca.framework.core.dtos.ApiPayload;
import com.thaca.framework.core.exceptions.FwException;
import com.thaca.framework.core.utils.JsonF;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternalApiClient {

    private final RestTemplate restTemplate;
    private final FrameworkProperties frameworkProperties;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <R> R post(String url, Object requestData, ParameterizedTypeReference responseType) {
        if (isByteType(responseType)) {
            return (R) postRaw(url, requestData);
        }

        HttpEntity<ApiPayload<Object>> entity = buildRequestEntity(requestData);

        try {
            ResponseEntity response = restTemplate.exchange(url, HttpMethod.POST, entity, responseType);
            forwardCookies(response);
            return (R) unwrapRaw(response);
        } catch (HttpStatusCodeException ex) {
            handleError(ex);
            throw ex;
        }
    }

    public <T> byte[] postRaw(String url, T requestData) {
        HttpEntity<ApiPayload<Object>> entity = buildRequestEntity(requestData);

        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.POST, entity, byte[].class);
            forwardCookies(response);
            return response.getBody();
        } catch (HttpStatusCodeException ex) {
            handleError(ex);
            throw ex;
        }
    }

    private <T> HttpEntity<ApiPayload<Object>> buildRequestEntity(T requestData) {
        ApiHeader header = FwContextHeader.get();
        if (header == null) {
            header = ApiHeader.builder().build();
        }

        Object safeData = (requestData != null) ? requestData : new HashMap<>();
        ApiBody<Object> body = ApiBody.builder().transId(MDC.get("transId")).status("OK").data(safeData).build();

        ApiPayload<Object> payload = ApiPayload.builder().header(header).body(body).build();

        return new HttpEntity<>(payload, buildHeaders());
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(FwHttpHeaderConstants.INTERNAL_CALL_HEADER, "true");
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + frameworkProperties.getHttpClient().getApiKey());

        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attr != null) {
            HttpServletRequest req = attr.getRequest();
            copyIfPresent(headers, HttpHeaders.COOKIE, req.getHeader(HttpHeaders.COOKIE));
            copyIfPresent(headers, HttpHeaders.USER_AGENT, req.getHeader(HttpHeaders.USER_AGENT));

            String bearer = req.getHeader(HttpHeaders.AUTHORIZATION);
            if (StringUtils.isNotBlank(bearer) && bearer.startsWith("Bearer ")) {
                headers.set(HttpHeaders.AUTHORIZATION, bearer);
            }
        }
        return headers;
    }

    private void copyIfPresent(HttpHeaders headers, String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            headers.add(key, value);
        }
    }

    private boolean isByteType(ParameterizedTypeReference<?> responseType) {
        Type type = responseType.getType();
        if (type instanceof ParameterizedType pt) {
            return pt.getActualTypeArguments().length > 0 && pt.getActualTypeArguments()[0].equals(byte[].class);
        }
        return false;
    }

    @SuppressWarnings("rawtypes")
    private Object unwrapRaw(ResponseEntity response) {
        if (response == null || response.getBody() == null) {
            return null;
        }
        Object body = response.getBody();
        if (body instanceof ApiPayload<?> payload) {
            if (payload.getBody() == null) {
                return null;
            }
            return payload.getBody().getData();
        }
        return body;
    }

    private void forwardCookies(ResponseEntity<?> response) {
        List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (cookies == null || cookies.isEmpty()) return;
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attr == null || attr.getResponse() == null) return;
        HttpServletResponse resp = attr.getResponse();
        for (String c : cookies) {
            resp.addHeader(HttpHeaders.SET_COOKIE, c);
        }
    }

    private void handleError(HttpStatusCodeException ex) {
        String body = ex.getResponseBodyAsString(StandardCharsets.UTF_8);
        if (StringUtils.isBlank(body)) return;
        try {
            ApiPayload<ErrorData> error = JsonF.jsonToObject(body, new ParameterizedTypeReference<>() {});
            if (
                error != null &&
                error.getBody() != null &&
                error.getBody().getData() != null &&
                error.getBody().getData().code() != null
            ) {
                throw new FwException(error.getBody().getData());
            }
        } catch (FwException e) {
            throw e;
        } catch (Exception e) {
            log.error("[InternalApiClient] Error parsing error response", e);
        }
    }
}
