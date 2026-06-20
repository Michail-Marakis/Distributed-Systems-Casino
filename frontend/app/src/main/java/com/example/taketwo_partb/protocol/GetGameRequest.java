package com.example.taketwo_partb.protocol;

import com.example.taketwo_partb.domain.Game;

/**
 * The type Get game request.
 */
public class GetGameRequest extends ServerRequest {

    private final String gameName;
    private Game gameToGet;

    /**
     * Instantiates a new Get game request.
     *
     * @param identifier the identifier
     * @param gameName   the game name
     */
    public GetGameRequest(int identifier, String gameName) {
        super(identifier, RequestType.GET_GAME);
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
     * Gets game to get.
     *
     * @return the game to get
     */
    public Game getGameToGet() {
        return gameToGet;
    }
}