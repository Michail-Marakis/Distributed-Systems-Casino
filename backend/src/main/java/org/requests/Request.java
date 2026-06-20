package org.requests;

import org.requests.serverside.ServerRequest;

/**
 * The type Request.
 */
public class Request {

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
        ERROR,
    }


    private ServerRequest.RequestStatus status = RequestStatus.OK;
    private String errorMessage;


    /**
     * Gets status.
     *
     * @return the status
     */
    public ServerRequest.RequestStatus getStatus() {
        return status;
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
     * Sets status.
     *
     * @param status the status
     */
    public void setStatus(ServerRequest.RequestStatus status) {
        this.status = status;
    }

    /**
     * Sets error message.
     *
     * @param errorMessage the error message
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }



}
