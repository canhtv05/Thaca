package com.thaca.framework.core.utils;

import com.thaca.common.dtos.TokenPair;
import com.thaca.framework.core.configs.FrameworkProperties;
import com.thaca.framework.core.constants.CommonConstants;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class CookieUtils {

    private final FrameworkProperties properties;

    public Cookie setTokenCookie(String accessToken, String refreshToken) {
        TokenPair tokenPair = new TokenPair(accessToken, refreshToken);

        String jsonData = JsonF.toJson(tokenPair);
        if (CommonUtils.isEmpty(jsonData)) {
            return null;
        }

        String encode = URLEncoder.encode(Objects.requireNonNull(jsonData), StandardCharsets.UTF_8);

        Cookie cookie = new Cookie(CommonConstants.COOKIE_NAME, encode);
        // cookie.setHttpOnly(true);

        // không cho phép lấy cookie từ phía client
        cookie.setHttpOnly(true);
        cookie.setMaxAge(properties.getSecurity().getRefreshDurationInSeconds().intValue()); // 2 weeks
        cookie.setPath("/");
        cookie.setSecure(false); // false for localhost development, true for production
        String domain = properties.getSecurity().getCookieDomain();
        if (StringUtils.hasText(domain)) cookie.setDomain(domain);

        return cookie;
    }

    public void deleteCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(CommonConstants.COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setSecure(false); // false for localhost development, true for production
        String domain = properties.getSecurity().getCookieDomain();
        if (StringUtils.hasText(domain)) cookie.setDomain(domain);
        response.addCookie(cookie);
    }
}
