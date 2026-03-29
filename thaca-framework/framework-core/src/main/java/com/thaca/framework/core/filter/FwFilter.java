package com.thaca.framework.core.filter;

import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.framework.core.configs.FrameworkProperties;
import com.thaca.framework.core.context.FwContext;
import com.thaca.framework.core.dtos.ApiEnvelope;
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
import java.util.Objects;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;
import tools.jackson.databind.JsonNode;

@Slf4j
public class FwFilter extends OncePerRequestFilter {

    private final FrameworkProperties frameworkProperties;
    private static final int CACHE_LIMIT = 8192;

    public FwFilter(FrameworkProperties frameworkProperties) {
        this.frameworkProperties = frameworkProperties;
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

        ApiEnvelope<?> envelope = JsonF.jsonToObject(requestBody, ApiEnvelope.class);
        if (envelope == null || envelope.getHeader() == null || envelope.getBody() == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response
                .getWriter()
                .write(
                    Objects.requireNonNull(JsonF.toJson(ApiEnvelope.error(CommonErrorMessage.REQUEST_INVALID_PARAMS)))
                );
            return;
        }

        String transId = UUID.randomUUID().toString().replace("-", "");
        String traceId = extractTraceId(requestBody);
        if (traceId == null) {
            traceId = UUID.randomUUID() + "-" + System.currentTimeMillis();
        }

        MDC.put("transId", transId);
        MDC.put("traceId", traceId);
        if (requestBody.length > 0) {
            try {
                FwContext.set(envelope.getHeader());
            } catch (Exception e) {
                log.warn("Cannot parse request body as ApiEnvelope: {}", e.getMessage());
            }
        }

        String uri = request.getRequestURI();
        String method = request.getMethod();
        String clientIp = request.getRemoteAddr();
        String payload = new String(requestBody, StandardCharsets.UTF_8).replaceAll("[\\r\\n]+", "");
        String username = extractUsername(request);

        log.info("IN - URI: '[{}] {}', User: [{}], IP: [{}], Payload: {}", method, uri, username, clientIp, payload);
        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            byte[] responseArray = responseWrapper.getContentAsByteArray();
            String responseStr = new String(responseArray, StandardCharsets.UTF_8).replaceAll("[\\r\\n]+", "");
            log.info(
                "OUT - URI: '[{}] {}', User: [{}], Status: [{}], Duration: [{}ms], Payload: {}",
                method,
                uri,
                username,
                responseWrapper.getStatus(),
                duration,
                responseStr
            );
            responseWrapper.copyBodyToResponse();
            MDC.clear();
            FwContext.clear();
        }
    }

    private String extractUsername(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
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
        }
        return "ANONYMOUS";
    }

    private String extractTraceId(byte[] body) {
        try {
            if (body != null && body.length > 0) {
                JsonNode root = JsonF.readTree(body);
                JsonNode node = root.path("header").path("traceId");
                if (!node.isMissingNode() && !node.isNull() && !node.asString().isBlank()) {
                    return node.asString().trim();
                }
            }
        } catch (Exception e) {
            log.warn("[FwFilter] extractTraceId failed: {}", e.getMessage());
        }
        return null;
    }
}
