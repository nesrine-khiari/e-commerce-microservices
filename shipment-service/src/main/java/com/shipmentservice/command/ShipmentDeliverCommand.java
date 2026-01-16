package com.shipmentservice.command;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@RequiredArgsConstructor
public class ShipmentDeliverCommand {

    @TargetAggregateIdentifier
    private final String transactionId;
    private final String paymentid;
}
