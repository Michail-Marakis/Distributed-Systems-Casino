package org.utility;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * The type Security.
 */
public class Security {
    /**
     * Convert to sha 256 string.
     *
     * @param input the input
     * @return the string
     */
    public static String convertToSha256(String input) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));

                StringBuilder hex = new StringBuilder();
                for (byte b : hash) {
                    hex.append(String.format("%02x", b));
                }
                return hex.toString();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }


}
