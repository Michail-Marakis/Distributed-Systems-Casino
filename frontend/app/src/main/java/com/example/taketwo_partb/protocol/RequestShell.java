package com.example.taketwo_partb.protocol;

/**
 * The type Request shell.
 */
public class RequestShell {

    private GetGamesRequest getGamesRequest;
    private GetGameRequest getGameRequest;
    private BetGameRequest betGameRequest;
    private RateGameRequest rateGameRequest;
    private AddBalanceRequest addBalanceRequest;
    private ServerRequest.RequestType requestType;
    private GetPlayerRequest getplayerRequest;

    /**
     * Instantiates a new Request shell.
     *
     * @param r the r
     */
    public RequestShell(GetGamesRequest r) {
        this.getGamesRequest = r;
        this.requestType = ServerRequest.RequestType.GET_GAMES;
    }

    /**
     * Instantiates a new Request shell.
     *
     * @param r the r
     */
    public RequestShell(GetPlayerRequest r) {
        this.getplayerRequest = r;
        this.requestType = ServerRequest.RequestType.GET_PLAYER;
    }

    /**
     * Instantiates a new Request shell.
     *
     * @param r the r
     */
    public RequestShell(GetGameRequest r) {
        this.getGameRequest = r;
        this.requestType = ServerRequest.RequestType.GET_GAME;
    }

    /**
     * Instantiates a new Request shell.
     *
     * @param r the r
     */
    public RequestShell(BetGameRequest r) {
        this.betGameRequest = r;
        this.requestType = ServerRequest.RequestType.BET_GAME;
    }

    /**
     * Instantiates a new Request shell.
     *
     * @param r the r
     */
    public RequestShell(RateGameRequest r) {
        this.rateGameRequest = r;
        this.requestType = ServerRequest.RequestType.RATE_GAME;
    }

    /**
     * Instantiates a new Request shell.
     *
     * @param r the r
     */
    public RequestShell(AddBalanceRequest r) {
        this.addBalanceRequest = r;
        this.requestType = ServerRequest.RequestType.ADD_BALANCE;
    }

    /**
     * Gets get games request.
     *
     * @return the get games request
     */
    public GetGamesRequest getGetGamesRequest() {
        return getGamesRequest;
    }

    /**
     * Gets get game request.
     *
     * @return the get game request
     */
    public GetGameRequest getGetGameRequest() {
        return getGameRequest;
    }

    /**
     * Gets bet game request.
     *
     * @return the bet game request
     */
    public BetGameRequest getBetGameRequest() {
        return betGameRequest;
    }

    /**
     * Gets rate game request.
     *
     * @return the rate game request
     */
    public RateGameRequest getRateGameRequest() {
        return rateGameRequest;
    }

    /**
     * Gets add balance request.
     *
     * @return the add balance request
     */
    public AddBalanceRequest getAddBalanceRequest() {
        return addBalanceRequest;
    }

    /**
     * Gets request type.
     *
     * @return the request type
     */
    public ServerRequest.RequestType getRequestType() {
        return requestType;
    }

    /**
     * Gets get player request.
     *
     * @return the get player request
     */
    public GetPlayerRequest getGetPlayerRequest() {
        return getplayerRequest;
    }
}