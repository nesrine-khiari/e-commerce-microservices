package com.shipmentservice.event;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class DeliverSuccessEvent {

    private final String transactionId;
}
