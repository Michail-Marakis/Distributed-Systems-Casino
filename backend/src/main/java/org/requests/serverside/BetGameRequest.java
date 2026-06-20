package org.requests.serverside;

import org.domain.CasinoPlayer;
import org.domain.Game;
import org.services.RandomNumberGenerator;

import java.util.HashMap;

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
     * Sets multiplier.
     *
     * @param multiplier the multiplier
     */
    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    /**
     * Sets winnings.
     *
     * @param winnings the winnings
     */
    public void setWinnings(double winnings) {
        this.winnings = winnings;
    }

    /**
     * Sets player profit.
     *
     * @param playerProfit the player profit
     */
    public void setPlayerProfit(double playerProfit) {
        this.playerProfit = playerProfit;
    }

    /**
     * Sets jackpot.
     *
     * @param jackpot the jackpot
     */
    public void setJackpot(boolean jackpot) {
        this.jackpot = jackpot;
    }

    /**
     * Sets random number.
     *
     * @param randomNumber the random number
     */
    public void setRandomNumber(int randomNumber) {
        this.randomNumber = randomNumber;
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

    public void processRequest(HashMap<String, CasinoPlayer> casinoPlayers,
                               HashMap<String, Game> games,
                               String RNG_IP, int RNG_PORT, String secret) {
        Game game;
        synchronized (games) {
            game = games.get(gameName);
        }

        if (game == null) {
            setStatus(RequestStatus.ERROR);
            setErrorMessage("Game does not exist: " + gameName);
            return;
        }

        if (!game.isAvailable()) {
            setStatus(RequestStatus.ERROR);
            setErrorMessage("Game not available: " + gameName);
            return;
        }

        if (betAmount < game.getMinBet() || betAmount > game.getMaxBet()) {
            setStatus(RequestStatus.ERROR);
            setErrorMessage(String.format("Bet must be between %.2f and %.2f FUN",
                    game.getMinBet(), game.getMaxBet()));
            return;
        }

        //get random number from SRG
        int number = RandomNumberGenerator.retrieveValue(RNG_IP, RNG_PORT, secret,getGameName());
        if(number == -1) {
            setStatus(RequestStatus.ERROR);
            setErrorMessage("Random generator error occurred: " + gameName);
            return;
        }


        //int number = (int)(Math.random() * 1000);
        this.randomNumber = number;

        //calculate winnings
        int mod100 = number % 100;
        if (mod100 == 0) {
            //JACKPOT
            this.multiplier = game.getJackpot();
            this.jackpot = true;
        } else {
            int mod10 = number % 10;
            double[] table = game.getMultiplierTable();
            this.multiplier = table[mod10];
            this.jackpot = false;
        }

        this.winnings = betAmount * multiplier;
        this.playerProfit = winnings - betAmount;

        //update game house profit (house perspective = -playerProfit)
        game.addHouseProfit(-playerProfit);
        setStatus(RequestStatus.OK);
    }


}
