package com.example.taketwo_partb.protocol;

import com.example.taketwo_partb.domain.BetLevel;
import com.example.taketwo_partb.domain.Game;
import com.example.taketwo_partb.domain.RiskLevel;

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
    private final HashSet<RiskLevel> riskLevel;
    private final HashSet<BetLevel> betLevel;

    private final LinkedList<Game> grabbedGames = new LinkedList<>();

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

    /**
     * Gets search name.
     *
     * @return the search name
     */
    public String getSearchName() {
        return searchName;
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
     * Gets star lower bound.
     *
     * @return the star lower bound
     */
    public int getStarLowerBound() {
        return starLowerBound;
    }

    /**
     * Gets star upper bound.
     *
     * @return the star upper bound
     */
    public int getStarUpperBound() {
        return starUpperBound;
    }

    /**
     * Gets bet level.
     *
     * @return the bet level
     */
    public HashSet<BetLevel> getBetLevel() {
        return betLevel == null ? new HashSet<>() : new HashSet<>(betLevel);
    }

    /**
     * Gets risk level.
     *
     * @return the risk level
     */
    public HashSet<RiskLevel> getRiskLevel() {
        return riskLevel == null ? new HashSet<>() : new HashSet<>(riskLevel);
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
        if (other != null && other.getGrabbedGames() != null) {
            grabbedGames.addAll(other.getGrabbedGames());
        }
    }
}