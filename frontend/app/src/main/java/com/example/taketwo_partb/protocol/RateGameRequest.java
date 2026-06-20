package com.example.taketwo_partb.protocol;

/**
 * The type Rate game request.
 */
public class RateGameRequest extends ServerRequest {

    private final String gameName;
    private final int rating;

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
}