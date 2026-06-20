package org.domain;

import org.utility.RandomStringGenerator;

import java.time.LocalDateTime;

/**
 * The type User session.
 */
public class UserSession {

    private final String SessionIdentifier;
    private final int ID_LENGTH = 30;
    private final LocalDateTime expiration;


    /**
     * Instantiates a new User session.
     */
    public UserSession() {
        SessionIdentifier = RandomStringGenerator.generate(ID_LENGTH);
        expiration = LocalDateTime.now().plusDays(1);
    }

    /**
     * Has expired boolean.
     *
     * @return the boolean
     */
    public boolean hasExpired() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        return currentDateTime.isAfter(expiration);
    }

}
