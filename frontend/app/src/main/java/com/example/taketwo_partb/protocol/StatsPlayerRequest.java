package com.example.taketwo_partb.protocol;

import com.example.taketwo_partb.domain.CasinoPlayer;
import com.example.taketwo_partb.domain.Game;

import java.util.HashMap;

/**
 * The type Stats player request.
 */
public class StatsPlayerRequest extends AssembledRequest {

    private final String playerId;
    private double totalProfitLoss = 0.0;
    private final HashMap<String, Double> gameBreakdown = new HashMap<>();

    /**
     * Instantiates a new Stats player request.
     *
     * @param identifier the identifier
     * @param playerId   the player id
     */
    public StatsPlayerRequest(int identifier, String playerId) {
        super(identifier, RequestType.STATS_BY_PLAYER);
        this.playerId = playerId;
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
     * Gets total profit loss.
     *
     * @return the total profit loss
     */
    public double getTotalProfitLoss() {
        return totalProfitLoss;
    }

    /**
     * Sets total profit loss.
     *
     * @param totalProfitLoss the total profit loss
     */
    public void setTotalProfitLoss(double totalProfitLoss) {
        this.totalProfitLoss = totalProfitLoss;
    }

    /**
     * Gets game breakdown.
     *
     * @return the game breakdown
     */
    public HashMap<String, Double> getGameBreakdown() {
        return gameBreakdown;
    }

    /**
     * Process request.
     *
     * @param casinoPlayers the casino players
     * @param games         the games
     * @param RNG_IP        the rng ip
     * @param RNG_PORT      the rng port
     * @param secret        the secret
     */
    public void processRequest(HashMap<String, CasinoPlayer> casinoPlayers,
                               HashMap<String, Game> games,
                               String RNG_IP, int RNG_PORT, String secret) {
    }

    /**
     * Merge.
     *
     * @param other the other
     */
    public synchronized void merge(StatsPlayerRequest other) {
        super.merge(other);
        this.totalProfitLoss += other.getTotalProfitLoss();
        synchronized (gameBreakdown) {
            for (String key : other.getGameBreakdown().keySet()) {
                gameBreakdown.merge(key, other.getGameBreakdown().get(key), Double::sum);
            }
        }
    }
}
