package com.shipmentservice.aggregate;

import com.shipmentservice.event.ShipmentDeliverEvent;
import lombok.RequiredArgsConstructor;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.EntityId;

@RequiredArgsConstructor
public class ShipmentTransaction {

    @EntityId
    private final String transactionId;
    private boolean delivered = false;


    @EventSourcingHandler
    public void on(ShipmentDeliverEvent event) {
        if (!transactionId.equals(event.getTransactionId())) {
            return;
        }
        if (delivered) {
            throw new IllegalStateException("Transaction has already done!");
        }

        this.delivered = true;
    }
}
