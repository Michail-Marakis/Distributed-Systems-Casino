package com.example.taketwo_partb.protocol;

import com.example.taketwo_partb.domain.CasinoPlayer;

import java.util.HashMap;

import com.example.taketwo_partb.domain.Game;

/**
 * The type Get player request.
 */
public class GetPlayerRequest extends ServerRequest {

    private final String username;

    /**
     * Instantiates a new Get player request.
     *
     * @param identifier the identifier
     * @param username   the username
     */
    public GetPlayerRequest(int identifier,String username) {
        super(identifier, RequestType.GET_PLAYER);
        this.username = username;
    }

    /**
     * Gets username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    private CasinoPlayer userToGet;

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
        synchronized (CasinoPlayers) {
            if(CasinoPlayers.containsKey(username)) {
                userToGet = CasinoPlayers.get(username);
                return;
            }
        }
        setStatus(RequestStatus.ERROR);
        setErrorMessage("User not found.");
    }

    /**
     * Get user casino player.
     *
     * @return the casino player
     */
    public CasinoPlayer getUser(){
        return userToGet;
    }





}
