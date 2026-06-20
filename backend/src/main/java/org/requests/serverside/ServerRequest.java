package org.requests.serverside;

import org.domain.CasinoPlayer;
import org.domain.Game;
import org.requests.Request;

import java.util.HashMap;

/**
 * The type Server request.
 */
public class ServerRequest  extends Request {


    /**
     * The enum Request type.
     */
    public enum RequestType {
        /**
         * Get games request type.
         */
        GET_GAMES,
        /**
         * Add game request type.
         */
        ADD_GAME,
        /**
         * Remove game request type.
         */
        REMOVE_GAME,
        /**
         * Modify game request type.
         */
        MODIFY_GAME,
        /**
         * Get game request type.
         */
        GET_GAME,
        /**
         * Bet game request type.
         */
        BET_GAME,
        /**
         * Rate game request type.
         */
        RATE_GAME,
        /**
         * Add balance request type.
         */
        ADD_BALANCE,
        /**
         * Get players request type.
         */
        GET_PLAYERS,
        /**
         * Get player request type.
         */
        GET_PLAYER,
        /**
         * Add player request type.
         */
        ADD_PLAYER,
        /**
         * Stats by provider request type.
         */
        STATS_BY_PROVIDER,
        /**
         * Stats by player request type.
         */
        STATS_BY_PLAYER,
        /**
         * Reactivate game request type.
         */
        REACTIVATE_GAME,

        /**
         * Replicate bet result request type.
         */
        REPLICATE_BET_RESULT,
        /**
         * Ping request type.
         */
        PING,

    }

    private int identifier;
    private RequestType requestType;

    /**
     * Instantiates a new Server request.
     *
     * @param identifier  the identifier
     * @param requestType the request type
     */
    public ServerRequest(int identifier, RequestType requestType) {
        this.identifier = identifier;
        this.requestType = requestType;
    }

    /**
     * Gets identifier.
     *
     * @return the identifier
     */
    public int getIdentifier() {
        return identifier;
    }

    /**
     * Sets identifier.
     *
     * @param identifier the identifier
     */
    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

    /**
     * Gets request type.
     *
     * @return the request type
     */
    public RequestType getRequestType() {
        return requestType;
    }

    /**
     * Process request.
     *
     * @param CasinoPlayers the casino players
     * @param Games         the games
     * @param RNG_IP        the rng ip
     * @param RNG_PORT      the rng port
     * @param secret        the secret
     */
    public void processRequest(HashMap<String, CasinoPlayer> CasinoPlayers, HashMap<String, Game> Games,
                                   String RNG_IP, int RNG_PORT,String secret) {

    }


    /**
     * Requires merging boolean.
     *
     * @return the boolean
     */
    public boolean requiresMerging() {
        return false;
    }
}
