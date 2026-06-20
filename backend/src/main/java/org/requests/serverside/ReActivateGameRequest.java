package org.requests.serverside;

import org.domain.CasinoPlayer;
import org.domain.Game;
import org.requests.Request;

import java.util.HashMap;

/**
 * The type Re activate game request.
 */
public class ReActivateGameRequest extends ServerRequest{

    private String gameName;

    /**
     * Instantiates a new Re activate game request.
     *
     * @param identifier the identifier
     * @param gameName   the game name
     */
    public ReActivateGameRequest(int identifier, String gameName) {
        super(identifier, ServerRequest.RequestType.REACTIVATE_GAME);
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
        synchronized (games) {
            Game game = games.get(gameName);
            if (game != null) {
                if(game.isAvailable()){
                    setErrorMessage("Game is already active.");
                    return;
                }
                game.setAvailable(true); //activate but keep stats
                return;
            }
        }
        setStatus(Request.RequestStatus.ERROR);
        setErrorMessage("Game not found: " + gameName);
    }
}
