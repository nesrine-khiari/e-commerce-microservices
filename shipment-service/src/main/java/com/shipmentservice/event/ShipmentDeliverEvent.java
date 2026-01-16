package com.shipmentservice.event;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ShipmentDeliverEvent {

    private final String transactionId;
}
