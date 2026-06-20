package org.requests.serverside;

import org.domain.CasinoPlayer;
import org.domain.Game;

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

    @SuppressWarnings("rawtypes")
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
     * Merge results from another worker.
     *
     * @param other the other
     */
    public synchronized void merge(StatsProviderRequest other) {
        super.merge(other);
        synchronized (gameStats) {
            for (String key : other.getGameStats().keySet()) {
                Double value = other.getGameStats().get(key);

                
                gameStats.putIfAbsent(key, value);
            }
        }
    }
}