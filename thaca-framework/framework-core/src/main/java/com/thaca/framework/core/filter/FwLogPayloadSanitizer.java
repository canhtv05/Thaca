package com.thaca.framework.core.filter;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class FwLogPayloadSanitizer {

    private static final int MAX_LOG_PAYLOAD_LENGTH = 1200;
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("\"password\"\\s*:\\s*\"[^\"]+\"");
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\"(accessToken|refreshToken)\"\\s*:\\s*\"[^\"]+\"");
    private static final Pattern CAPTCHA_PATTERN = Pattern.compile("\"captcha\"\\s*:\\s*\"[^\"]+\"");
    private static final Pattern LONG_IMAGE_PATTERN = Pattern.compile(
        "\"(image|base64|captchaImage)\"\\s*:\\s*\"([^\"]{80,})\""
    );
    private static final Pattern DATA_URI_PATTERN = Pattern.compile("\"data:image/[^\"]+\"");
    private static final Pattern PAGINATION_PATTERN = Pattern.compile(
        "\"pagination\"\\s*:\\s*\\{[^}]*\"currentPage\"\\s*:\\s*(\\d+)[^}]*\"totalPages\"\\s*:\\s*(\\d+)[^}]*\"size\"\\s*:\\s*(\\d+)[^}]*\"count\"\\s*:\\s*(\\d+)[^}]*\"total\"\\s*:\\s*(\\d+)[^}]*\\}"
    );

    private FwLogPayloadSanitizer() {}

    static String sanitize(String uri, String payload, boolean response) {
        if (payload == null) {
            return null;
        }
        String sanitized = maskSensitiveData(payload);
        if (isCaptchaUri(uri)) {
            return abbreviate("[CAPTCHA PAYLOAD OMITTED] " + buildPayloadMeta(sanitized));
        }
        if (isSearchUri(uri)) {
            if (response) {
                return summarizeSearchResponse(sanitized);
            }
            return abbreviate("[SEARCH PAYLOAD OMITTED] " + buildPayloadMeta(sanitized));
        }
        return abbreviate(sanitized);
    }

    private static String maskSensitiveData(String payload) {
        String masked = PASSWORD_PATTERN.matcher(payload).replaceAll("\"password\":\"******\"");
        masked = TOKEN_PATTERN.matcher(masked).replaceAll("\"$1\":\"******\"");
        masked = CAPTCHA_PATTERN.matcher(masked).replaceAll("\"captcha\":\"***\"");
        masked = maskLongJsonFields(masked, LONG_IMAGE_PATTERN, value -> "\"<omitted:" + value.length() + " chars>\"");
        masked = replaceLongMatches(
            masked,
            DATA_URI_PATTERN,
            value -> "\"<base64-image:" + Math.max(0, value.length() - 2) + " chars>\""
        );
        return masked;
    }

    private static String summarizeSearchResponse(String payload) {
        Matcher matcher = PAGINATION_PATTERN.matcher(payload);
        if (matcher.find()) {
            return String.format(
                "[SEARCH RESPONSE] page=%s/%s size=%s count=%s total=%s",
                matcher.group(1),
                matcher.group(2),
                matcher.group(3),
                matcher.group(4),
                matcher.group(5)
            );
        }
        return abbreviate("[SEARCH RESPONSE] " + buildPayloadMeta(payload));
    }

    private static String buildPayloadMeta(String payload) {
        return "length=" + payload.length() + " chars";
    }

    private static boolean isSearchUri(String uri) {
        return uri != null && uri.toLowerCase().contains("/search");
    }

    private static boolean isCaptchaUri(String uri) {
        return uri != null && uri.toLowerCase().contains("/generate-captcha");
    }

    private static String abbreviate(String payload) {
        if (payload.length() <= MAX_LOG_PAYLOAD_LENGTH) {
            return payload;
        }
        return payload.substring(0, MAX_LOG_PAYLOAD_LENGTH) + "... [truncated " + payload.length() + " chars]";
    }

    private static String maskLongJsonFields(
        String payload,
        Pattern pattern,
        java.util.function.Function<String, String> replacer
    ) {
        Matcher matcher = pattern.matcher(payload);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String fieldName = matcher.group(1);
            String fieldValue = matcher.groupCount() >= 2 ? matcher.group(2) : matcher.group();
            String replacement = "\"" + fieldName + "\":" + replacer.apply(fieldValue);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static String replaceLongMatches(String payload, Pattern pattern, Function<String, String> replacer) {
        Matcher matcher = pattern.matcher(payload);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacer.apply(matcher.group())));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
