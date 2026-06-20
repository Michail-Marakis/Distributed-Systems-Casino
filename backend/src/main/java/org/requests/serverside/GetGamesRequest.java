package org.requests.serverside;

import org.domain.BetLevel;
import org.domain.CasinoPlayer;
import org.domain.Game;
import org.domain.RiskLevel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * The type Get games request.
 */
public class GetGamesRequest extends AssembledRequest {

    private final String searchName;
    private final String providerName;
    private final int starLowerBound;
    private final int starUpperBound;
    private final HashSet<BetLevel> betLevel;
    private final HashSet<RiskLevel> riskLevel;

    /**
     * Instantiates a new Get games request.
     *
     * @param identifier     the identifier
     * @param searchName     the search name
     * @param providerName   the provider name
     * @param starLowerBound the star lower bound
     * @param starUpperBound the star upper bound
     * @param betLevel       the bet level
     * @param riskLevel      the risk level
     */
    public GetGamesRequest(int identifier,
                           String searchName,
                           String providerName,
                           int starLowerBound,
                           int starUpperBound,
                           HashSet<BetLevel> betLevel,
                           HashSet<RiskLevel> riskLevel) {
        super(identifier, RequestType.GET_GAMES);
        this.searchName = searchName;
        this.providerName = providerName;
        this.starLowerBound = starLowerBound;
        this.starUpperBound = starUpperBound;
        this.betLevel = betLevel;
        this.riskLevel = riskLevel;
    }

    private String getSearchName() {
        return searchName;
    }

    private String getProviderName() {
        return providerName;
    }

    private int getStarLowerBound() {
        return starLowerBound;
    }

    private int getStarUpperBound() {
        return starUpperBound;
    }

    /**
     * Get bet level hash set.
     *
     * @return the hash set
     */
    public HashSet<BetLevel> getBetLevel() {
        return betLevel == null ? null : new HashSet<>(betLevel);
    }

    private HashSet<RiskLevel> getRiskLevel() {
        return riskLevel;
    }

    /**
     * The grabbed games.
     */
    private final LinkedList<Game> grabbedGames = new LinkedList<>();

    @SuppressWarnings("rawtypes")
    public void processRequest(HashMap<String, CasinoPlayer> CasinoPlayers,
                               HashMap<String, Game> Games,
                               String RNG_IP, int RNG_PORT, String secret) {

        synchronized (Games) {
            for (Game game : Games.values()) {

                if (!game.isAvailable()) {
                    continue;
                }

                boolean matchesName = true;
                if (getSearchName() != null && !getSearchName().isEmpty()) {
                    matchesName = game.getGameName()
                            .toLowerCase()
                            .contains(getSearchName().toLowerCase());
                }

                boolean matchesProvider = true;
                if (getProviderName() != null && !getProviderName().isEmpty()) {
                    matchesProvider = game.getProviderName()
                            .toLowerCase()
                            .contains(getProviderName().toLowerCase());
                }

                boolean withinStarBound =
                        game.getStars() >= getStarLowerBound()
                                && game.getStars() <= getStarUpperBound();

                boolean matchesBetLevel = true;
                if (betLevel != null && !betLevel.isEmpty()) {
                    matchesBetLevel = betLevel.contains(game.getBetLevel());
                }

                boolean matchesRiskLevel = true;
                if (riskLevel != null && !riskLevel.isEmpty()) {
                    matchesRiskLevel = riskLevel.contains(game.getRiskLevel());
                }

                if (matchesName && matchesProvider && withinStarBound && matchesBetLevel && matchesRiskLevel) {
                    grabbedGames.add(game);
                }
            }
        }
    }

    /**
     * Gets grabbed games.
     *
     * @return the grabbed games
     */
    public LinkedList<Game> getGrabbedGames() {
        return grabbedGames;
    }

    /**
     * Merge.
     *
     * @param other the other
     */
    public synchronized void merge(GetGamesRequest other) {
        super.merge(other);
        synchronized (grabbedGames) {
            grabbedGames.addAll(other.getGrabbedGames());
        }
    }
}