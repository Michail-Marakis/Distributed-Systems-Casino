package org.requests.clientside;

import org.domain.CasinoPlayer;
import org.domain.Game;

import java.util.HashMap;

/**
 * The type Client add game request.
 */
public class ClientAddGameRequest extends  ClientRequest{


    /**
     * Instantiates a new Client add game request.
     *
     * @param identifier           the identifier
     * @param userName             the user name
     * @param authenticationString the authentication string
     */
    public ClientAddGameRequest(int identifier,String userName,String authenticationString) {
        super(identifier, RequestType.ADD_GAME,userName,authenticationString);
    }




}
