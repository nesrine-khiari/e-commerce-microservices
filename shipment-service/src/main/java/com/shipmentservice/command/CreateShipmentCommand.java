package com.shipmentservice.command;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@RequiredArgsConstructor
public class CreateShipmentCommand {

    @TargetAggregateIdentifier
    private final String id;
    private final String paymentId;
    private final String productId;
    private final Integer productCount;
    private final String address;
    private final String cargoFirm;

}
