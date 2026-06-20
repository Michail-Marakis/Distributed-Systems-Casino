package org.requests.serverside;


import org.domain.CasinoPlayer;
import org.domain.Game;
import org.services.RandomNumberGenerator;
import org.utility.Connectivity;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

/**
 * The type Add game request.
 */
public class AddGameRequest extends ServerRequest {


    private Game gameToAdd;


    /**
     * Instantiates a new Add game request.
     *
     * @param identifier the identifier
     * @param gameToAdd  the game to add
     */
    public AddGameRequest(int identifier, Game gameToAdd) {
        super(identifier, RequestType.ADD_GAME);
        this.gameToAdd = gameToAdd;
    }

    /**
     * Gets game to add.
     *
     * @return the game to add
     */
    public Game getGameToAdd() {
        return gameToAdd;
    }

    public void processRequest(HashMap<String, CasinoPlayer> CasinoPlayers, HashMap<String, Game> Games,
                               String RNG_IP, int RNG_PORT,String secret) {
        try {
            Connectivity.sendStringData(new Socket(RNG_IP,RNG_PORT),gameToAdd.getGameName()+":"+ RandomNumberGenerator.RNGRequestType.CREATE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        synchronized (Games) {
            if( !Games.containsKey(getGameToAdd().getGameName())) {
                Games.put(getGameToAdd().getGameName(), getGameToAdd());
                return;
            }
        }
        //An error was occured
        setStatus(RequestStatus.ERROR);
        setErrorMessage("Game with such a name already exists.");
        try {
            Connectivity.sendStringData(new Socket(RNG_IP,RNG_PORT),gameToAdd.getGameName()+":"+ RandomNumberGenerator.RNGRequestType.CREATE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



}
