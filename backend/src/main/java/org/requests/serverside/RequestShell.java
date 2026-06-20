package org.requests.serverside;

/**
 * The type Request shell.
 */
public class RequestShell {

    private GetGamesRequest getGamesRequest = null;
    private AddGameRequest addGameRequest = null;
    private RemoveGameRequest removeGameRequest = null;
    private ModifyGameRequest modifyGameRequest = null;
    private GetGameRequest getGameRequest = null;
    private BetGameRequest betGameRequest = null;
    private RateGameRequest rateGameRequest = null;
    private AddBalanceRequest addBalanceRequest = null;
    private StatsProviderRequest statsProviderRequest = null;
    private StatsPlayerRequest statsPlayerRequest = null;
    private ServerRequest.RequestType requestType;
    private ReActivateGameRequest reActivateGameRequest;
    private GetPlayerRequest getplayerRequest = null;
    private PingRequest ping = null;
    private boolean replica;

    private ReplicateBetResultRequest replicateBetResultRequest;


    /**
     * Is replica boolean.
     *
     * @return the boolean
     */
    public boolean isReplica() {
        return replica;
    }

    /**
     * Sets replica.
     *
     * @param replica the replica
     */
    public void setReplica(boolean replica) {
        this.replica = replica;
    }

    /**
     * Gets replicate bet result request.
     *
     * @return the replicate bet result request
     */
    public ReplicateBetResultRequest getReplicateBetResultRequest() {
        return replicateBetResultRequest;
    }

    /**
     * Sets replicate bet result request.
     *
     * @param replicateBetResultRequest the replicate bet result request
     */
    public void setReplicateBetResultRequest(ReplicateBetResultRequest replicateBetResultRequest) {
        this.replicateBetResultRequest = replicateBetResultRequest;
    }

    /**
     * Instantiates a new Request shell.
     *
     * @param r the r
     */
    public RequestShell(ReplicateBetResultRequest r) {
        this.replicateBetResultRequest = r;
        requestType = ServerRequest.RequestType.REPLICATE_BET_RESULT;
    }

    /**
     * Instantiates a new Request shell.
     *
     * @param r the r
     */
    public RequestShell(GetPlayerRequest r) {
        this.getplayerRequest = r;
        requestType = ServerRequest.RequestType.GET_PLAYER;
    }

    /**
     * Instantiates a new Request shell.
     *
     * @param r the r
     */
    public RequestShell(GetGamesRequest r) { this.getGamesRequest = r; requestType = ServerRequest.RequestType.GET_GAMES; }

    /**
     * Instantiates a new Request shell.
     *
     * @param r the r
     */
    public RequestShell(AddGameRequest r) { this.addGameRequest = r; requestType = ServerRequest.RequestType.ADD_GAME; }

    /**
     * Instantiates a new Request shell.
     *
     * @param r the r
     */
    public RequestShell(RemoveGameRequest r) { this.removeGameRequest = r; requestType = ServerRequest.RequestType.REMOVE_GAME; }

    /**
     * Instantiates a new Request shell.
     *
     * @param r the r
     */
    public RequestShell(ModifyGameRequest r) { this.modifyGameRequest = r; requestType = ServerRequest.RequestType.MODIFY_GAME; }

    /**
     * Instantiates a new Request shell.
     *
     * @param r the r
     */
    public RequestShell(GetGameRequest r) { this.getGameRequest = r; requestType = ServerRequest.RequestType.GET_GAME; }

    /**
     * Instantiates a new Request shell.
     *
     * @param r the r
     */
    public RequestShell(BetGameRequest r) { this.betGameRequest = r; requestType = ServerRequest.RequestType.BET_GAME; }

    /**
     * Instantiates a new Request shell.
     *
     * @param r the r
     */
    public RequestShell(RateGameRequest r) { this.rateGameRequest = r; requestType = ServerRequest.RequestType.RATE_GAME; }

    /**
     * Instantiates a new Request shell.
     *
     * @param r the r
     */
    public RequestShell(AddBalanceRequest r) { this.addBalanceRequest = r; requestType = ServerRequest.RequestType.ADD_BALANCE; }

    /**
     * Instantiates a new Request shell.
     *
     * @param r the r
     */
    public RequestShell(StatsProviderRequest r) { this.statsProviderRequest = r; requestType = ServerRequest.RequestType.STATS_BY_PROVIDER; }

    /**
     * Instantiates a new Request shell.
     *
     * @param r the r
     */
    public RequestShell(StatsPlayerRequest r) { this.statsPlayerRequest = r; requestType = ServerRequest.RequestType.STATS_BY_PLAYER; }

    /**
     * Instantiates a new Request shell.
     *
     * @param r the r
     */
    public RequestShell(ReActivateGameRequest r){this.reActivateGameRequest = r; requestType =  ServerRequest.RequestType.REACTIVATE_GAME;}

    /**
     * Instantiates a new Request shell.
     *
     * @param r the r
     */
    public RequestShell(PingRequest r){this.ping = r; requestType = ServerRequest.RequestType.PING;}

    /**
     * Gets get games request.
     *
     * @return the get games request
     */
    public GetGamesRequest getGetGamesRequest() { return getGamesRequest; }

    /**
     * Gets add game request.
     *
     * @return the add game request
     */
    public AddGameRequest getAddGameRequest() { return addGameRequest; }

    /**
     * Gets remove game request.
     *
     * @return the remove game request
     */
    public RemoveGameRequest getRemoveGameRequest() { return removeGameRequest; }

    /**
     * Gets modify game request.
     *
     * @return the modify game request
     */
    public ModifyGameRequest getModifyGameRequest() { return modifyGameRequest; }

    /**
     * Gets get game request.
     *
     * @return the get game request
     */
    public GetGameRequest getGetGameRequest() { return getGameRequest; }

    /**
     * Gets bet game request.
     *
     * @return the bet game request
     */
    public BetGameRequest getBetGameRequest() { return betGameRequest; }

    /**
     * Gets rate game request.
     *
     * @return the rate game request
     */
    public RateGameRequest getRateGameRequest() { return rateGameRequest; }

    /**
     * Gets add balance request.
     *
     * @return the add balance request
     */
    public AddBalanceRequest getAddBalanceRequest() { return addBalanceRequest; }

    /**
     * Gets stats provider request.
     *
     * @return the stats provider request
     */
    public StatsProviderRequest getStatsProviderRequest() { return statsProviderRequest; }

    /**
     * Gets stats player request.
     *
     * @return the stats player request
     */
    public StatsPlayerRequest getStatsPlayerRequest() { return statsPlayerRequest; }

    /**
     * Gets re activate game request.
     *
     * @return the re activate game request
     */
    public ReActivateGameRequest getReActivateGameRequest() {return reActivateGameRequest;}

    /**
     * Gets request type.
     *
     * @return the request type
     */
    public ServerRequest.RequestType getRequestType() { return requestType; }

    /**
     * Gets get player request.
     *
     * @return the get player request
     */
    public GetPlayerRequest getGetPlayerRequest() {
        return getplayerRequest;
    }


    /**
     * Gets ping.
     *
     * @return the ping
     */
    public PingRequest getPing() { return ping; }

}
