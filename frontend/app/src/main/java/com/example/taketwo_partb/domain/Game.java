package com.example.taketwo_partb.domain;

/**
 * The type Game.
 */
public class Game {

    private String gameName;
    private String providerName;
    private int stars;
    private int noOfVotes;
    private String gameLogo;
    private float minBet;
    private float maxBet;
    private RiskLevel riskLevel;
    private String hashKey;
    private boolean isAvailable = true;

    private int totalStars;           //sum of all ratings for computing average
    private double houseProfit = 0.0; //casino wins

    /**
     * Instantiates a new Game.
     *
     * @param gameName     the game name
     * @param providerName the provider name
     * @param stars        the stars
     * @param noOfVotes    the no of votes
     * @param gameLogo     the game logo
     * @param minBet       the min bet
     * @param maxBet       the max bet
     * @param riskLevel    the risk level
     * @param hashKey      the hash key
     */
    public Game(String gameName, String providerName, int stars, int noOfVotes,
                String gameLogo, float minBet, float maxBet,
                RiskLevel riskLevel, String hashKey) {
        this.gameName = gameName;
        this.providerName = providerName;
        this.stars = stars;
        this.noOfVotes = noOfVotes;
        this.gameLogo = gameLogo;
        this.minBet = minBet;
        this.maxBet = maxBet;
        this.riskLevel = riskLevel;
        this.hashKey = hashKey;
        this.totalStars = stars * noOfVotes;
    }

    /**
     * Is available boolean.
     *
     * @return the boolean
     */
    public boolean isAvailable() {
        return isAvailable;
    }

    /**
     * Sets available.
     *
     * @param available the available
     */
    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    /**
     * Gets game name.
     *
     * @return the game name
     */
    public String getGameName() {
        return gameName;
    }

    /**
     * Sets game name.
     *
     * @param gameName the game name
     */
    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    /**
     * Gets provider name.
     *
     * @return the provider name
     */
    public String getProviderName() {
        return providerName;
    }

    /**
     * Sets provider name.
     *
     * @param providerName the provider name
     */
    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    /**
     * Gets stars.
     *
     * @return the stars
     */
    public int getStars() {
        return stars;
    }

    /**
     * Sets stars.
     *
     * @param stars the stars
     */
    public void setStars(int stars) {
        this.stars = stars;
    }

    /**
     * Gets no of votes.
     *
     * @return the no of votes
     */
    public int getNoOfVotes() {
        return noOfVotes;
    }

    /**
     * Sets no of votes.
     *
     * @param noOfVotes the no of votes
     */
    public void setNoOfVotes(int noOfVotes) {
        this.noOfVotes = noOfVotes;
    }

    /**
     * Gets game logo.
     *
     * @return the game logo
     */
    public String getGameLogo() {
        return gameLogo;
    }

    /**
     * Sets game logo.
     *
     * @param gameLogo the game logo
     */
    public void setGameLogo(String gameLogo) {
        this.gameLogo = gameLogo;
    }

    /**
     * Gets min bet.
     *
     * @return the min bet
     */
    public float getMinBet() {
        return minBet;
    }

    /**
     * Sets min bet.
     *
     * @param minBet the min bet
     */
    public void setMinBet(float minBet) {
        this.minBet = minBet;
    }

    /**
     * Gets max bet.
     *
     * @return the max bet
     */
    public float getMaxBet() {
        return maxBet;
    }

    /**
     * Gets bet level.
     *
     * @return the bet level
     */
    public BetLevel getBetLevel() {
        if (Math.abs(minBet - 0.1f) < 0.0001f) {
            return BetLevel.$;
        } else if (Math.abs(minBet - 1f) < 0.0001f) {
            return BetLevel.$$;
        }
        return BetLevel.$$$;
    }

    /**
     * Sets max bet.
     *
     * @param maxBet the max bet
     */
    public void setMaxBet(float maxBet) {
        this.maxBet = maxBet;
    }

    /**
     * Gets risk level.
     *
     * @return the risk level
     */
    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    /**
     * Sets risk level.
     *
     * @param riskLevel the risk level
     */
    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    /**
     * Gets hash key.
     *
     * @return the hash key
     */
    public String getHashKey() {
        return hashKey;
    }

    /**
     * Sets hash key.
     *
     * @param hashKey the hash key
     */
    public void setHashKey(String hashKey) {
        this.hashKey = hashKey;
    }

    /**
     * Gets jackpot.
     *
     * @return the jackpot
     */
//jackpot computed from risk level
    public double getJackpot() {
        switch (riskLevel) {
            case low: return 10;
            case medium: return 20;
            case high: return 40;
            default: return 10;
        }
    }

    /**
     * Get multiplier table double [ ].
     *
     * @return the double [ ]
     */
//multiplier table based on risk level
    public double[] getMultiplierTable() {
        switch (riskLevel) {
            case low: return new double[]{0.0, 0.0, 0.0, 0.1, 0.5, 1.0, 1.1, 1.3, 2.0, 2.5};
            case medium: return new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.5, 1.0, 1.5, 2.5, 3.5};
            case high: return new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 2.0, 6.5};
            default: return new double[]{0.0, 0.0, 0.0, 0.1, 0.5, 1.0, 1.1, 1.3, 2.0, 2.5};
        }
    }

    /**
     * Add rating.
     *
     * @param rating the rating
     */
//thread rating
    public synchronized void addRating(int rating) {
        this.totalStars += rating;
        this.noOfVotes++;
        this.stars = Math.round((float) totalStars / noOfVotes);
    }

    /**
     * Add house profit.
     *
     * @param amount the amount
     */
//thread house profit tracking
    public synchronized void addHouseProfit(double amount) {
        this.houseProfit += amount;
    }

    /**
     * Gets house profit.
     *
     * @return the house profit
     */
    public synchronized double getHouseProfit() {
        return houseProfit;
    }

    /**
     * Gets total stars.
     *
     * @return the total stars
     */
    public int getTotalStars() {
        return totalStars;
    }
    @Override
    public String toString() {
        return "Game{" +
                "gameName='" + gameName + '\'' +
                ", providerName='" + providerName + '\'' +
                ", stars=" + stars +
                ", noOfVotes=" + noOfVotes +
                ", gameLogo='" + gameLogo + '\'' +
                ", minBet=" + minBet +
                ", maxBet=" + maxBet +
                ", betLevel=" + getBetLevel() +
                ", riskLevel=" + riskLevel +
                ", jackpot=" + getJackpot() +
                ", isAvailable=" + isAvailable +
                ", houseProfit=" + houseProfit +
                '}';
    }



}
