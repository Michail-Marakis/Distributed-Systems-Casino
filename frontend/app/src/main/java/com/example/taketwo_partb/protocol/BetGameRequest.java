package com.example.taketwo_partb.protocol;

/**
 * The type Bet game request.
 */
public class BetGameRequest extends ServerRequest {

    private final String gameName;
    private final String playerId;
    private final float betAmount;

    private double multiplier;
    private double winnings;
    private double playerProfit;
    private boolean jackpot;
    private int randomNumber;

    /**
     * Instantiates a new Bet game request.
     *
     * @param identifier the identifier
     * @param gameName   the game name
     * @param playerId   the player id
     * @param betAmount  the bet amount
     */
    public BetGameRequest(int identifier, String gameName, String playerId, float betAmount) {
        super(identifier, RequestType.BET_GAME);
        this.gameName = gameName;
        this.playerId = playerId;
        this.betAmount = betAmount;
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
     * Gets player id.
     *
     * @return the player id
     */
    public String getPlayerId() {
        return playerId;
    }

    /**
     * Gets bet amount.
     *
     * @return the bet amount
     */
    public float getBetAmount() {
        return betAmount;
    }

    /**
     * Gets multiplier.
     *
     * @return the multiplier
     */
    public double getMultiplier() {
        return multiplier;
    }

    /**
     * Gets winnings.
     *
     * @return the winnings
     */
    public double getWinnings() {
        return winnings;
    }

    /**
     * Gets player profit.
     *
     * @return the player profit
     */
    public double getPlayerProfit() {
        return playerProfit;
    }

    /**
     * Is jackpot boolean.
     *
     * @return the boolean
     */
    public boolean isJackpot() {
        return jackpot;
    }

    /**
     * Gets random number.
     *
     * @return the random number
     */
    public int getRandomNumber() {
        return randomNumber;
    }
}