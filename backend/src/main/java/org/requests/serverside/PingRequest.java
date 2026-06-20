package org.requests.serverside;

/**
 * The type Ping request.
 */
public class PingRequest extends ServerRequest {

    /**
     * Instantiates a new Ping request.
     *
     * @param identifier the identifier
     */
    public PingRequest(int identifier) {
        super(identifier,RequestType.PING);
    }
}
