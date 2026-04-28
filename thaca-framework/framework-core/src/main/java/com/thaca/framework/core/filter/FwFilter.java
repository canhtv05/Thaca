package com.thaca.framework.core.filter;

import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.framework.core.constants.FwHttpHeaderConstants;
import com.thaca.framework.core.context.FwContextBody;
import com.thaca.framework.core.context.FwContextHeader;
import com.thaca.framework.core.dtos.ApiBody;
import com.thaca.framework.core.dtos.ApiHeader;
import com.thaca.framework.core.dtos.ApiPayload;
import com.thaca.framework.core.enums.ChannelType;
import com.thaca.framework.core.utils.JsonF;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.redisson.api.RLock;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Slf4j
@RequiredArgsConstructor
public class FwFilter extends OncePerRequestFilter {

    private final IdempotencyGuard requestGuard;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod()) || "GET".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected @NullMarked void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String contentType = request.getContentType();
        if (contentType == null || !contentType.toLowerCase().contains(MediaType.APPLICATION_JSON_VALUE)) {
            filterChain.doFilter(request, response);
            return;
        }

        CachedBodyRequestWrapper requestWrapper = new CachedBodyRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        byte[] requestBody = requestWrapper.getCachedBody();
        ApiPayload<?> envelope = JsonF.jsonToObject(requestBody, ApiPayload.class);
        String transId = resolveTransId(envelope);
        String traceId = resolveTraceId(request);
        String spanId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        MDC.put("transId", transId);
        MDC.put("traceId", traceId);
        MDC.put("spanId", spanId);

        try {
            FwContextHeader.set(buildContextHeader(envelope));
            FwContextBody.set(buildContextBody(envelope));
        } catch (Exception e) {
            log.warn("Cannot set FwContext: {}", e.getMessage());
        }

        String uri = request.getRequestURI();
        String method = request.getMethod();
        String clientIp = request.getRemoteAddr();
        String payload = new String(requestBody, StandardCharsets.UTF_8).replaceAll("[\\r\\n]+", "");
        payload = maskSensitiveData(payload);
        String username = extractUsername();

        log.info("IN - URI: '[{}] {}', User: [{}], IP: [{}], Payload: {}", method, uri, username, clientIp, payload);
        long startTime = System.currentTimeMillis();
        response.setHeader(FwHttpHeaderConstants.TRACE_ID_HEADER, traceId);
        response.setHeader(FwHttpHeaderConstants.TRANS_ID_HEADER, transId);
        response.setHeader(FwHttpHeaderConstants.SPAN_ID_HEADER, spanId);

        if (
            envelope == null ||
            envelope.getHeader() == null ||
            envelope.getBody() == null ||
            envelope.getBody().getData() == null
        ) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

            ApiPayload<?> errorRes = ApiPayload.error(CommonErrorMessage.REQUEST_INVALID_PARAMS);
            if (envelope != null && envelope.getHeader() != null) {
                errorRes.setHeader(envelope.getHeader());
            }
            this.processLog(response, errorRes, startTime, method, uri, username);
            return;
        }

        if (
            requestBody.length > 0 &&
            !ChannelType.WEB.name().equals(envelope.getHeader().getChannel()) &&
            !ChannelType.MOBILE.name().equals(envelope.getHeader().getChannel())
        ) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

            ApiPayload<?> channelError = ApiPayload.error(CommonErrorMessage.CHANNEL_INVALID);
            channelError.setHeader(envelope.getHeader());

            this.processLog(response, channelError, startTime, method, uri, username);
            return;
        }

        RLock locked = null;
        boolean isInternal = "true".equals(request.getHeader(FwHttpHeaderConstants.INTERNAL_CALL_HEADER));
        String lockKey = (username != null ? username : clientIp) + ":" + transId;
        try {
            if (!isInternal) {
                locked = requestGuard.tryAcquire(lockKey);
                if (locked == null) {
                    log.warn(
                        "[FwFilter] Duplicate transId detected: [{}], URI: '[{}] {}', User: [{}]",
                        transId,
                        method,
                        uri,
                        username
                    );
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding(StandardCharsets.UTF_8.name());

                    ApiPayload<?> dupError = ApiPayload.error(CommonErrorMessage.DUPLICATE_TRANS_ID);
                    dupError.setHeader(envelope.getHeader());

                    this.processLog(response, dupError, startTime, method, uri, username);
                    return;
                }
            }
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            if (locked != null) {
                requestGuard.release(locked);
            }
            long duration = System.currentTimeMillis() - startTime;
            byte[] responseArray = responseWrapper.getContentAsByteArray();
            String responseContentType = responseWrapper.getContentType();
            String responseStr;

            if (responseContentType != null && responseContentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
                responseStr = new String(responseArray, StandardCharsets.UTF_8).replaceAll("[\\r\\n]+", "");
                responseStr = maskSensitiveData(responseStr);
            } else {
                responseStr = "[BINARY DATA (" + responseArray.length + " bytes)]";
            }

            int status = responseWrapper.getStatus();
            String logMsg = "OUT - URI: '[{}] {}', User: [{}], Status: [{}], Duration: [{}ms], Payload: {}";
            if (status >= 400) {
                log.error(logMsg, method, uri, username, status, duration, responseStr);
            } else {
                log.info(logMsg, method, uri, username, status, duration, responseStr);
            }

            responseWrapper.copyBodyToResponse();
            MDC.clear();
            FwContextHeader.clear();
        }
    }

    private ApiHeader buildContextHeader(ApiPayload<?> envelope) {
        return envelope != null && envelope.getHeader() != null ? envelope.getHeader() : ApiHeader.builder().build();
    }

    private ApiBody<?> buildContextBody(ApiPayload<?> envelope) {
        return envelope != null && envelope.getBody() != null ? envelope.getBody() : ApiBody.builder().build();
    }

    private void processLog(
        HttpServletResponse response,
        ApiPayload<?> channelError,
        long startTime,
        String method,
        String uri,
        String username
    ) throws IOException {
        String responseStr = Objects.requireNonNull(JsonF.toJson(channelError));
        response.getWriter().write(responseStr);

        long duration = System.currentTimeMillis() - startTime;
        String logResponseStr = maskSensitiveData(responseStr.replaceAll("[\\r\\n]+", ""));
        log.error(
            "OUT - URI: '[{}] {}', User: [{}], Status: [{}], Duration: [{}ms], Payload: {}",
            method,
            uri,
            username,
            response.getStatus(),
            duration,
            logResponseStr
        );

        MDC.clear();
        FwContextHeader.clear();
    }

    private String extractUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "ANONYMOUS";
    }

    private String resolveTraceId(HttpServletRequest request) {
        String traceId = request.getHeader(FwHttpHeaderConstants.TRACE_ID_HEADER);
        if (StringUtils.hasText(traceId)) {
            return traceId.trim();
        }
        return UUID.randomUUID().toString().replace("-", "") + "-" + System.currentTimeMillis();
    }

    private String resolveTransId(ApiPayload<?> envelope) {
        if (envelope != null && envelope.getBody() != null && StringUtils.hasText(envelope.getBody().getTransId())) {
            return envelope.getBody().getTransId().trim();
        }
        return UUID.randomUUID().toString().replace("-", "") + "-" + System.currentTimeMillis();
    }

    private String maskSensitiveData(String payload) {
        if (payload == null) {
            return null;
        }
        return payload.replaceAll("\"password\"\\s*:\\s*\"[^\"]+\"", "\"password\":\"******\"");
    }
}
