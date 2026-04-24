package com.thaca.framework.reactive.starter.utils;

import com.thaca.common.dtos.TokenPair;
import com.thaca.framework.core.configs.FrameworkProperties;
import com.thaca.framework.core.constants.CommonConstants;
import com.thaca.framework.core.utils.CommonUtils;
import com.thaca.framework.core.utils.JsonF;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class ReactiveCookieUtils {

    private final FrameworkProperties properties;

    public ResponseCookie setTokenCookie(String accessToken, String refreshToken) {
        TokenPair tokenPair = new TokenPair(accessToken, refreshToken);

        String jsonData = JsonF.toJson(tokenPair);
        if (CommonUtils.isEmpty(jsonData)) {
            return null;
        }

        String encode = URLEncoder.encode(Objects.requireNonNull(jsonData), StandardCharsets.UTF_8);

        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(CommonConstants.COOKIE_NAME, encode)
            .httpOnly(true)
            .maxAge(properties.getSecurity().getRefreshDurationInSeconds().intValue())
            .path("/")
            .secure(false) // false for localhost, true for production
            .sameSite("Strict");

        String domain = properties.getSecurity().getCookieDomain();
        if (StringUtils.hasText(domain)) {
            builder.domain(domain);
        }

        return builder.build();
    }

    public ResponseCookie deleteCookie() {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(CommonConstants.COOKIE_NAME, "")
            .httpOnly(true)
            .maxAge(0)
            .path("/")
            .secure(false)
            .sameSite("Strict");

        String domain = properties.getSecurity().getCookieDomain();
        if (StringUtils.hasText(domain)) {
            builder.domain(domain);
        }

        return builder.build();
    }
}
