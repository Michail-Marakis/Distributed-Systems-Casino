package org.requests.serverside;

import org.domain.CasinoPlayer;
import org.domain.Game;
import org.services.RandomNumberGenerator;
import org.utility.Connectivity;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

/**
 * The type Remove game request.
 */
public class RemoveGameRequest extends ServerRequest {

    private String gameName;

    /**
     * Instantiates a new Remove game request.
     *
     * @param identifier the identifier
     * @param gameName   the game name
     */
    public RemoveGameRequest(int identifier, String gameName) {
        super(identifier, RequestType.REMOVE_GAME);
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

    public void processRequest(HashMap<String, CasinoPlayer> casinoPlayers,
                               HashMap<String, Game> games,
                               String RNG_IP, int RNG_PORT, String secret) {

        try {
            Connectivity.sendStringData(new Socket(RNG_IP,RNG_PORT),gameName+":"+ RandomNumberGenerator.RNGRequestType.CLEAR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        synchronized (games) {
            Game game = games.get(gameName);
            if (game != null) {
                if(!game.isAvailable()){
                    setErrorMessage("Game already not available");
                    return;
                }
                game.setAvailable(false); //deactivate but keep stats
                return;
            }
        }
        setStatus(RequestStatus.ERROR);
        setErrorMessage("Game not found: " + gameName);
    }
}
