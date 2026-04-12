package com.thaca.framework.core.filter;

import com.thaca.common.dtos.TokenPair;
import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.framework.core.configs.FrameworkProperties;
import com.thaca.framework.core.context.FwContext;
import com.thaca.framework.core.dtos.ApiHeader;
import com.thaca.framework.core.dtos.ApiPayload;
import com.thaca.framework.core.enums.ChannelType;
import com.thaca.framework.core.utils.CommonUtils;
import com.thaca.framework.core.utils.JsonF;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Slf4j
public class FwFilter extends OncePerRequestFilter {

    private final FrameworkProperties frameworkProperties;
    private static final int CACHE_LIMIT = 8192;
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TRANS_ID_HEADER = "X-Trans-Id";
    private static final String SPAN_ID_HEADER = "X-Span-Id";
    private static final Set<String> IN_FLIGHT_TRANS_IDS = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public FwFilter(FrameworkProperties frameworkProperties) {
        this.frameworkProperties = frameworkProperties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(frameworkProperties.getSecurity().getBase64Secret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    protected @NullMarked void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        CachedBodyRequestWrapper requestWrapper = new CachedBodyRequestWrapper(request, CACHE_LIMIT);
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
            FwContext.set(buildContextHeader(envelope));
        } catch (Exception e) {
            log.warn("Cannot set FwContext: {}", e.getMessage());
        }

        String uri = request.getRequestURI();
        String method = request.getMethod();
        String clientIp = request.getRemoteAddr();
        String payload = new String(requestBody, StandardCharsets.UTF_8).replaceAll("[\\r\\n]+", "");
        String username = extractUsername(request);

        log.info("IN - URI: '[{}] {}', User: [{}], IP: [{}], Payload: {}", method, uri, username, clientIp, payload);
        long startTime = System.currentTimeMillis();
        response.setHeader(TRACE_ID_HEADER, traceId);
        response.setHeader(TRANS_ID_HEADER, transId);
        response.setHeader(SPAN_ID_HEADER, spanId);

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

        if (!IN_FLIGHT_TRANS_IDS.add(transId)) {
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

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            IN_FLIGHT_TRANS_IDS.remove(transId);
            long duration = System.currentTimeMillis() - startTime;
            byte[] responseArray = responseWrapper.getContentAsByteArray();
            String responseStr = new String(responseArray, StandardCharsets.UTF_8).replaceAll("[\\r\\n]+", "");

            int status = responseWrapper.getStatus();
            String logMsg = "OUT - URI: '[{}] {}', User: [{}], Status: [{}], Duration: [{}ms], Payload: {}";
            if (status >= 400) {
                log.error(logMsg, method, uri, username, status, duration, responseStr);
            } else {
                log.info(logMsg, method, uri, username, status, duration, responseStr);
            }

            responseWrapper.copyBodyToResponse();
            MDC.clear();
            FwContext.clear();
        }
    }

    private ApiHeader buildContextHeader(ApiPayload<?> envelope) {
        return envelope != null && envelope.getHeader() != null ? envelope.getHeader() : ApiHeader.builder().build();
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
        log.error(
            "OUT - URI: '[{}] {}', User: [{}], Status: [{}], Duration: [{}ms], Payload: {}",
            method,
            uri,
            username,
            response.getStatus(),
            duration,
            responseStr.replaceAll("[\\r\\n]+", "")
        );

        MDC.clear();
        FwContext.clear();
    }

    private String extractUsername(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else {
            Optional<TokenPair> tokenPair = CommonUtils.tokenFromCookie(request.getHeader(HttpHeaders.COOKIE));
            if (tokenPair.isPresent() && tokenPair.get().accessToken() != null) {
                token = tokenPair.get().accessToken();
            }
        }
        try {
            if (token == null) return "ANONYMOUS";
            Claims claims = Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
            if (claims.getSubject() != null) return claims.getSubject();
            else return "ANONYMOUS";
        } catch (ExpiredJwtException e) {
            if (e.getClaims() != null) {
                if (e.getClaims().getSubject() != null) return e.getClaims().getSubject();
            } else return "ANONYMOUS";
        } catch (Exception e) {
            log.warn("[FwFilter] extractUsername failed: {}", e.getMessage());
        }
        return "ANONYMOUS";
    }

    private String resolveTraceId(HttpServletRequest request) {
        String traceId = request.getHeader(TRACE_ID_HEADER);
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
}
