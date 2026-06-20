package org.requests.serverside;

/**
 * The type Assembled request.
 */
public class AssembledRequest extends  ServerRequest{

    /**
     * Instantiates a new Assembled request.
     *
     * @param identifier  the identifier
     * @param requestType the request type
     */
    AssembledRequest(int identifier, RequestType requestType){
        super(identifier, requestType);
    }



    private int packetsMerged = 1;

    /**
     * Merge.
     *
     * @param other the other
     */
    public void merge(AssembledRequest other){
        packetsMerged+=other.getPacketsMerged();
    }

    /**
     * Get packets merged int.
     *
     * @return the int
     */
    public int getPacketsMerged(){
        return packetsMerged;
    }

    public boolean requiresMerging() {
        return true;
    }

}
