package com.thaca.framework.core.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class FwUtils {

    private static final String AES = "AES";
    private static final int AES_KEY_SIZE = 256;

    public static String hexString(String message) {
        try {
            MessageDigest localDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = localDigest.digest(message.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString(); // 64 hex chars
        } catch (Exception e) {
            return message;
        }
    }

    public static String generateSecretKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(AES);
            SecureRandom secureRandom = new SecureRandom();
            keyGenerator.init(AES_KEY_SIZE, secureRandom);
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            return null;
        }
    }
}
