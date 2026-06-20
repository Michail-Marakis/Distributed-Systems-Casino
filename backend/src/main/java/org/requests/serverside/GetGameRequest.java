package org.requests.serverside;

import org.domain.CasinoPlayer;
import org.domain.Game;

import java.util.HashMap;

/**
 * The type Get game request.
 */
public class GetGameRequest extends  ServerRequest{

    private final String gameName;

    /**
     * Instantiates a new Get game request.
     *
     * @param identifier the identifier
     * @param gameName   the game name
     */
    public GetGameRequest(int identifier,String gameName ) {
        super(identifier,RequestType.GET_GAME);
        this.gameName = gameName;
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
     * The Game to get.
     */
    Game gameToGet;
    public void processRequest(HashMap<String, CasinoPlayer> CasinoPlayers, HashMap<String, Game> Games,
                               String RNG_IP, int RNG_PORT, String secret) {
        synchronized (Games) {
            if(Games.containsKey(gameName)) {
                gameToGet = Games.get(gameName);
                return;
            }
        }
        setStatus(RequestStatus.ERROR);
        setErrorMessage("User not found.");
    }

    /**
     * Gets game to get.
     *
     * @return the game to get
     */
    public Game getGameToGet() {
        return gameToGet;
    }

}
