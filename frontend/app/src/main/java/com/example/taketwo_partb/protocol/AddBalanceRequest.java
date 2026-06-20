package com.example.taketwo_partb.protocol;

/**
 * The type Add balance request.
 */
public class AddBalanceRequest extends ServerRequest {

    private final String playerId;
    private final float amount;

    /**
     * Instantiates a new Add balance request.
     *
     * @param identifier the identifier
     * @param playerId   the player id
     * @param amount     the amount
     */
    public AddBalanceRequest(int identifier, String playerId, float amount) {
        super(identifier, RequestType.ADD_BALANCE);
        this.playerId = playerId;
        this.amount = amount;
    }

    /**
     * Gets player id.
     *
     * @return the player id
     */
    public String getPlayerId() {
        return playerId;
    }

    /**
     * Gets amount.
     *
     * @return the amount
     */
    public float getAmount() {
        return amount;
    }
}