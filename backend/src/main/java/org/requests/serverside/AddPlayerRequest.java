package org.requests.serverside;

import org.domain.CasinoPlayer;
import org.domain.Game;
import org.domain.User;

import java.util.HashMap;

/**
 * The type Add player request.
 */
public class AddPlayerRequest extends ServerRequest{


    private CasinoPlayer casinoPlayerToAdd;

    /**
     * Instantiates a new Add player request.
     *
     * @param identifier the identifier
     * @param userToAdd  the user to add
     */
    AddPlayerRequest(int identifier, User userToAdd) {
        super(identifier,RequestType.ADD_PLAYER);
    }

    /**
     * Gets casino player to add.
     *
     * @return the casino player to add
     */
    public CasinoPlayer getCasinoPlayerToAdd() {
        return casinoPlayerToAdd;
    }

    public void processRequest(HashMap<String, CasinoPlayer> CasinoPlayers, HashMap<String, Game> Games,
                               String RNG_IP, int RNG_PORT,String secret) {
        synchronized (CasinoPlayers) {
            if(!CasinoPlayers.containsKey(getCasinoPlayerToAdd().getUsername())) {
                CasinoPlayers.put(getCasinoPlayerToAdd().getUsername(),getCasinoPlayerToAdd());
                return;
            }
        }
        setStatus(RequestStatus.ERROR);
        setErrorMessage("User not found.");
    }




}
