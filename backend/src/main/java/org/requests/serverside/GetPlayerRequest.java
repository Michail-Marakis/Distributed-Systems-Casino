package org.requests.serverside;

import org.domain.CasinoPlayer;
import org.domain.Game;

import java.util.HashMap;

/**
 * The type Get player request.
 */
public class GetPlayerRequest extends ServerRequest {

    private final String username;
    private CasinoPlayer userToGet;

    /**
     * Instantiates a new Get player request.
     *
     * @param identifier the identifier
     * @param username   the username
     */
    public GetPlayerRequest(int identifier, String username) {
        super(identifier, RequestType.GET_PLAYER);
        this.username = username;
    }

    /**
     * Gets user to get.
     *
     * @return the user to get
     */
    public CasinoPlayer getUserToGet() {
        return userToGet;
    }

    /**
     * Sets user to get.
     *
     * @param userToGet the user to get
     */
    public void setUserToGet(CasinoPlayer userToGet) {
        this.userToGet = userToGet;
    }


    /**
     * Gets username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets user.
     *
     * @return the user
     */
    public CasinoPlayer getUser() {
        return userToGet;
    }

    /**
     * Sets user.
     *
     * @param player the player
     */
    public void setUser(CasinoPlayer player) {
        this.userToGet = player;
    }

    public void processRequest(HashMap<String, CasinoPlayer> CasinoPlayers,
                               HashMap<String, Game> Games,
                               String RNG_IP,
                               int RNG_PORT,
                               String secret) {

        synchronized (CasinoPlayers) {

            if (CasinoPlayers.containsKey(username)) {

                userToGet = CasinoPlayers.get(username);

                setStatus(RequestStatus.OK);
                return;
            }
        }

        setStatus(RequestStatus.ERROR);
        setErrorMessage("User not found.");
    }
}