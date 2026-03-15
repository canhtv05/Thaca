package com.thaca.framework.core.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class FwUtils {

    private static final MessageDigest digest;

    static {
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String hexString(String message) {
        try {
            byte[] hash = digest.digest(message.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString(); // 64 hex chars
        } catch (Exception e) {
            return message;
        }
    }
}
