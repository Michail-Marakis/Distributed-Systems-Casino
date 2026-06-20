package org.requests.serverside;

import org.domain.CasinoPlayer;
import org.domain.Game;
import org.domain.RiskLevel;

import java.util.HashMap;

/**
 * The type Modify game request.
 */
public class ModifyGameRequest extends ServerRequest {

    private String gameName;
    private String newRiskLevel;
    private Float newMinBet;
    private Float newMaxBet;

    /**
     * Instantiates a new Modify game request.
     *
     * @param identifier   the identifier
     * @param gameName     the game name
     * @param newRiskLevel the new risk level
     * @param newMinBet    the new min bet
     * @param newMaxBet    the new max bet
     */
    public ModifyGameRequest(int identifier, String gameName, String newRiskLevel, Float newMinBet, Float newMaxBet) {
        super(identifier, RequestType.MODIFY_GAME);
        this.gameName = gameName;
        this.newRiskLevel = newRiskLevel;
        this.newMinBet = newMinBet;
        this.newMaxBet = newMaxBet;
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
     * Gets new risk level.
     *
     * @return the new risk level
     */
    public String getNewRiskLevel() {
        return newRiskLevel;
    }

    /**
     * Gets new min bet.
     *
     * @return the new min bet
     */
    public Float getNewMinBet() {
        return newMinBet;
    }

    /**
     * Gets new max bet.
     *
     * @return the new max bet
     */
    public Float getNewMaxBet() {
        return newMaxBet;
    }

    public void processRequest(HashMap<String, CasinoPlayer> casinoPlayers,
                               HashMap<String, Game> games,
                               String RNG_IP, int RNG_PORT, String secret) {
        synchronized (games) {
            Game game = games.get(gameName);
            if (game != null) {
                if (newRiskLevel != null) {
                    try {
                        game.setRiskLevel(RiskLevel.valueOf(newRiskLevel.toLowerCase()));
                    } catch (IllegalArgumentException e) {
                        setStatus(RequestStatus.ERROR);
                        setErrorMessage("Invalid risk level: " + newRiskLevel);
                        return;
                    }
                }

                if (newMinBet != null) {
                    game.setMinBet(newMinBet);
                }

                if (newMaxBet != null) {
                    game.setMaxBet(newMaxBet);
                }

                setStatus(RequestStatus.OK);
                setErrorMessage(null);
                return;
            }
        }
        setStatus(RequestStatus.ERROR);
        setErrorMessage("Game not found: " + gameName);
    }

}
