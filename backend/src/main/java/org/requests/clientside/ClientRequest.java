package org.requests.clientside;

import org.requests.Request;
import org.requests.serverside.ServerRequest;

/**
 * The type Client request.
 */
public class ClientRequest  extends Request {


    /**
     * The enum Request type.
     */
    public enum RequestType {
        /**
         * Auth request type.
         */
//common
        AUTH,
        /**
         * Disconnect request type.
         */
        DISCONNECT,
        /**
         * Get games request type.
         */
        GET_GAMES,

        /**
         * Add game request type.
         */
// Manager
        ADD_GAME,
        /**
         * Remove game request type.
         */
        REMOVE_GAME,
        /**
         * Set game request type.
         */
        SET_GAME,
        /**
         * Get players request type.
         */
        GET_PLAYERS,

        /**
         * Play game request type.
         */
//Player
        PLAY_GAME,
    }

    private final int identifier;
    private final RequestType requestType;
    private final String userName;
    private final String authenticationToken;


    /**
     * Instantiates a new Client request.
     *
     * @param identifier          the identifier
     * @param requestType         the request type
     * @param userName            the user name
     * @param authenticationToken the authentication token
     */
    ClientRequest(int identifier, RequestType requestType, String userName, String authenticationToken) {
        this.identifier = identifier;
        this.requestType = requestType;
        this.userName = userName;
        this.authenticationToken = authenticationToken;
    }

    /**
     * Gets user name.
     *
     * @return the user name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Gets authentication token.
     *
     * @return the authentication token
     */
    public String getAuthenticationToken() {
        return authenticationToken;
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
     * Gets request type.
     *
     * @return the request type
     */
    public RequestType getRequestType() {
        return requestType;
    }



}
