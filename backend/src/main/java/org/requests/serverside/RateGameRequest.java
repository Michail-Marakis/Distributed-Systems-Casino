package org.requests.serverside;

import org.domain.CasinoPlayer;
import org.domain.Game;

import java.util.HashMap;

/**
 * The type Rate game request.
 */
public class RateGameRequest extends ServerRequest {

    private String gameName;
    private int rating;

    /**
     * Instantiates a new Rate game request.
     *
     * @param identifier the identifier
     * @param gameName   the game name
     * @param rating     the rating
     */
    public RateGameRequest(int identifier, String gameName, int rating) {
        super(identifier, RequestType.RATE_GAME);
        this.gameName = gameName;
        this.rating = rating;
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
     * Gets rating.
     *
     * @return the rating
     */
    public int getRating() {
        return rating;
    }

    public void processRequest(HashMap<String, CasinoPlayer> casinoPlayers,
                               HashMap<String, Game> games,
                               String RNG_IP, int RNG_PORT, String secret) {
        if (rating < 1 || rating > 5) {
            setStatus(RequestStatus.ERROR);
            setErrorMessage("Rating must be between 1 and 5");
            return;
        }
        synchronized (games) {
            Game game = games.get(gameName);
            if (game != null) {
                if(game.isAvailable()) {
                    game.addRating(rating);
                }else{
                    setStatus(RequestStatus.ERROR);
                    setErrorMessage("Game not available: " + gameName);
                }
            }else{
                setStatus(RequestStatus.ERROR);
                setErrorMessage("Game not found: " + gameName);
            }
        }
    }
}
