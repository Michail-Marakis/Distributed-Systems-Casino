package org.requests.serverside;

import org.domain.CasinoPlayer;
import org.domain.Game;
import org.requests.Request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The type Replicate bet result request.
 */
public class ReplicateBetResultRequest extends Request {

    private String gameName;
    private String playerId;
    private float betAmount;
    private double multiplier;
    private double winnings;
    private double playerProfit;

    /**
     * Instantiates a new Replicate bet result request.
     *
     * @param gameName     the game name
     * @param playerId     the player id
     * @param betAmount    the bet amount
     * @param multiplier   the multiplier
     * @param winnings     the winnings
     * @param playerProfit the player profit
     */
    public ReplicateBetResultRequest(String gameName,
                                     String playerId,
                                     float betAmount,
                                     double multiplier,
                                     double winnings,
                                     double playerProfit) {
        this.gameName = gameName;
        this.playerId = playerId;
        this.betAmount = betAmount;
        this.multiplier = multiplier;
        this.winnings = winnings;
        this.playerProfit = playerProfit;
    }

    /**
     * Process request.
     *
     * @param casinoPlayers    the casino players
     * @param casinoGames      the casino games
     * @param playerBetHistory the player bet history
     */
    public void processRequest(HashMap<String, CasinoPlayer> casinoPlayers,
                               HashMap<String, Game> casinoGames,
                               HashMap<String, List<Object[]>> playerBetHistory) {

        Game game;
        synchronized (casinoGames) {
            game = casinoGames.get(gameName);
            if (game == null) {
                setStatus(RequestStatus.ERROR);
                setErrorMessage("Game not found for replication: " + gameName);
                return;
            }

            game.addHouseProfit(-playerProfit);
        }

        synchronized (playerBetHistory) {
            List<Object[]> history = playerBetHistory.get(playerId);
            if (history == null) {
                history = new ArrayList<>();
                playerBetHistory.put(playerId, history);
            }

            history.add(new Object[]{gameName, playerProfit});
        }

        setStatus(RequestStatus.OK);
        setErrorMessage(null);
    }

    /**
     * Gets game name.
     *
     * @return the game name
     */
    public String getGameName() { return gameName; }

    /**
     * Gets player id.
     *
     * @return the player id
     */
    public String getPlayerId() { return playerId; }

    /**
     * Gets bet amount.
     *
     * @return the bet amount
     */
    public float getBetAmount() { return betAmount; }

    /**
     * Gets multiplier.
     *
     * @return the multiplier
     */
    public double getMultiplier() { return multiplier; }

    /**
     * Gets winnings.
     *
     * @return the winnings
     */
    public double getWinnings() { return winnings; }

    /**
     * Gets player profit.
     *
     * @return the player profit
     */
    public double getPlayerProfit() { return playerProfit; }
}