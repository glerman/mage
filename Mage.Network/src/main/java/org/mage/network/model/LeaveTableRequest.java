package org.mage.network.model;

import java.io.Serializable;
import java.util.UUID;

/**
 *
 * @author BetaSteward
 */
public class LeaveTableRequest extends TableRequest {

    public LeaveTableRequest(UUID roomId, UUID tableId) {
        super(roomId, tableId);
    }

}
