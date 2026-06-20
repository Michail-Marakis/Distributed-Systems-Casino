package com.example.taketwo_partb.protocol;

/**
 * The type Server request.
 */
public class ServerRequest {

    /**
     * The enum Request type.
     */
    public enum RequestType {
        /**
         * Get games request type.
         */
        GET_GAMES,
        /**
         * Get game request type.
         */
        GET_GAME,
        /**
         * Bet game request type.
         */
        BET_GAME,
        /**
         * Rate game request type.
         */
        RATE_GAME,
        /**
         * Add balance request type.
         */
        ADD_BALANCE,
        /**
         * Get player request type.
         */
        GET_PLAYER,
        /**
         * Stats by player request type.
         */
        STATS_BY_PLAYER,
        /**
         * Stats by provider request type.
         */
        STATS_BY_PROVIDER
    }

    /**
     * The enum Request status.
     */
    public enum RequestStatus {
        /**
         * Ok request status.
         */
        OK,
        /**
         * Error request status.
         */
        ERROR;

    }

    private int identifier;
    private RequestType requestType;
    private RequestStatus status = RequestStatus.OK;
    private String errorMessage;

    /**
     * Instantiates a new Server request.
     */
    public ServerRequest() {
    }

    /**
     * Instantiates a new Server request.
     *
     * @param identifier  the identifier
     * @param requestType the request type
     */
    public ServerRequest(int identifier, RequestType requestType) {
        this.identifier = identifier;
        this.requestType = requestType;
    }

    /**
     * Gets identifier.
     *
     * @return the identifier
     */
    public int getIdentifier() {
        return identifier;
    }

    /**
     * Sets identifier.
     *
     * @param identifier the identifier
     */
    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

    /**
     * Gets request type.
     *
     * @return the request type
     */
    public RequestType getRequestType() {
        return requestType;
    }

    /**
     * Sets request type.
     *
     * @param requestType the request type
     */
    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    /**
     * Gets status.
     *
     * @return the status
     */
    public RequestStatus getStatus() {
        return status;
    }

    /**
     * Sets status.
     *
     * @param status the status
     */
    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    /**
     * Gets error message.
     *
     * @return the error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets error message.
     *
     * @param errorMessage the error message
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Requires merging boolean.
     *
     * @return the boolean
     */
    public boolean requiresMerging() {
        return false;
    }
}