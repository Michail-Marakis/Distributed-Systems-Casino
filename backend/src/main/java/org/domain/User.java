package org.domain;


import org.mindrot.jbcrypt.BCrypt;

/**
 * The type User.
 */
public class User {

    private String username;
    private String hash;
    private String salt;
    private String email;
    private UserSession session;


    /**
     * Instantiates a new User.
     *
     * @param username the username
     * @param hash     the hash
     * @param salt     the salt
     * @param email    the email
     */
    User(String username, String hash, String salt, String email) {
        this.username = username;
        this.hash = hash;
        this.salt = salt;
        this.email = email;
        this.session = new UserSession();
    }

    /**
     * Create user user.
     *
     * @param username the username
     * @param email    the email
     * @param password the password
     * @return the user
     */
    public static User createUser(String username, String email,String password) {
        String salt = BCrypt.gensalt();
        String hash = BCrypt.hashpw(password, salt);
        return new User(username, hash, salt,email);
    }

    /**
     * Gets username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets username.
     *
     * @param username the username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets hash.
     *
     * @return the hash
     */
    public String getHash() {
        return hash;
    }

    /**
     * Gets salt.
     *
     * @return the salt
     */
    public String getSalt() {
        return salt;
    }

    /**
     * Change password.
     *
     * @param password the password
     */
    public void changePassword(String password) {
        this.hash = BCrypt.hashpw(password, salt);
    }

    /**
     * Correct password boolean.
     *
     * @param password the password
     * @return the boolean
     */
    public boolean correctPassword(String password) {
        String tempHash = BCrypt.hashpw(password, salt);
        return tempHash.equals(hash);
    }

    /**
     * Gets email.
     *
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets email.
     *
     * @param email the email
     */
    public void setEmail(String email) {
        this.email = email;
    }


    /**
     * Gets session.
     *
     * @return the session
     */
    public UserSession getSession() {
        return session;
    }
}