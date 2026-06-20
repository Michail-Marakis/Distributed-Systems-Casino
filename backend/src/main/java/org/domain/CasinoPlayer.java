package org.domain;

/**
 * The type Casino player.
 */
public class CasinoPlayer {

    private String username;
    private float tokens;
    private float netProfit;


    /**
     * Instantiates a new Casino player.
     *
     * @param username the username
     * @param tokens   the tokens
     */
    public CasinoPlayer(String username, float tokens) {
        this.username = username;
        this.tokens = tokens;
        this.netProfit = 0;
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
     * Sets net profit.
     *
     * @param netProfit the net profit
     */
    public void setNetProfit(float netProfit) {
        this.netProfit = netProfit;
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
     * Gets tokens.
     *
     * @return the tokens
     */
    public synchronized float getTokens() {
        return tokens;
    }

    /**
     * Sets tokens.
     *
     * @param tokens the tokens
     */
    public synchronized void setTokens(float tokens) {
        this.tokens = tokens;
    }

    /**
     * Add tokens.
     *
     * @param amount the amount
     */
    public synchronized void addTokens(float amount) {
        this.tokens += amount;
    }

    /**
     * Deduct tokens boolean.
     *
     * @param amount the amount
     * @return the boolean
     */
    public synchronized boolean deductTokens(float amount) {
        if (tokens >= amount) {
            tokens -= amount;
            return true;
        }
        return false;
    }

    /**
     * Gets net profit.
     *
     * @return the net profit
     */
    public synchronized float getNetProfit() {
        return netProfit;
    }

    /**
     * Add net profit.
     *
     * @param amount the amount
     */
    public synchronized void addNetProfit(float amount) {
        this.netProfit += amount;
    }
}
