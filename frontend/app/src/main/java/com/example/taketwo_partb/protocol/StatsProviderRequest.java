package com.example.taketwo_partb.protocol;

import com.example.taketwo_partb.domain.CasinoPlayer;
import com.example.taketwo_partb.domain.Game;

import java.util.HashMap;

/**
 * The type Stats provider request.
 */
public class StatsProviderRequest extends AssembledRequest {

    private final String providerName;
    private final HashMap<String, Double> gameStats = new HashMap<>();

    /**
     * Instantiates a new Stats provider request.
     *
     * @param identifier   the identifier
     * @param providerName the provider name
     */
    public StatsProviderRequest(int identifier, String providerName) {
        super(identifier, RequestType.STATS_BY_PROVIDER);
        this.providerName = providerName;
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
     * Gets game stats.
     *
     * @return the game stats
     */
    public HashMap<String, Double> getGameStats() {
        return gameStats;
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
        synchronized (games) {
            for (Game game : games.values()) {
                if (game.getProviderName().equalsIgnoreCase(providerName)) {
                    gameStats.put(game.getGameName(), game.getHouseProfit());
                }
            }
        }
    }

    /**
     * Merge.
     *
     * @param other the other
     */
    public synchronized void merge(StatsProviderRequest other) {
        super.merge(other);
        synchronized (gameStats) {
            for (String key : other.getGameStats().keySet()) {
                gameStats.merge(key, other.getGameStats().get(key), Double::sum);
            }
        }
    }
}
