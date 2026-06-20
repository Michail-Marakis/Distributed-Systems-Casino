package org.utility;

import java.security.SecureRandom;

/**
 * The type Random string generator.
 */
public class RandomStringGenerator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generate string.
     *
     * @param k the k
     * @return the string
     */
    public static String generate(int k) {
        StringBuilder sb = new StringBuilder(k);

        for (int i = 0; i < k; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }

        return sb.toString();
    }
}