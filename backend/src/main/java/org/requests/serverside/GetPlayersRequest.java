package org.requests.serverside;

import org.domain.CasinoPlayer;
import org.domain.Game;
import org.domain.User;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * The type Get players request.
 */
public class GetPlayersRequest extends AssembledRequest {

    private final String username;
    private final String email;

    /**
     * Instantiates a new Get players request.
     *
     * @param identifier the identifier
     * @param username   the username
     * @param email      the email
     */
    GetPlayersRequest(int identifier,String username,String email) {
        super(identifier, RequestType.GET_PLAYERS);
        this.username = username;
        this.email = email;
    }

    /**
     * Gets email.
     *
     * @return the email
     */
    public String getEmail() {
        return email;
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
     * The Users to send.
     */
    final LinkedList<CasinoPlayer> usersToSend = new LinkedList<>();
    public void processRequest(HashMap<String, CasinoPlayer> CasinoPlayers, HashMap<String, Game> Games,
                               String RNG_IP, int RNG_PORT, String secret) {
        synchronized (CasinoPlayers) {
            for (CasinoPlayer player : CasinoPlayers.values()) {
                if (player.getUsername().contains(getUsername())) {
                    usersToSend.add(player);
                }
            }
        }
    }


    /**
     * Gets users to send.
     *
     * @return the users to send
     */
    public LinkedList<CasinoPlayer> getUsersToSend() {
        return usersToSend;
    }

    /**
     * Merge.
     *
     * @param other the other
     */
    public synchronized void merge(GetPlayersRequest other){
        super.merge(other);
        synchronized (getUsersToSend()){
            getUsersToSend().addAll(other.getUsersToSend());
        }
    }



}
