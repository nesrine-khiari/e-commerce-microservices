package com.paymentservice.eventhandler;

import com.core.event.OrderCreatedEvent;
import com.paymentservice.command.PaymentCreateCommand;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderCreateEventHandler {
    private final CommandGateway commandGateway;

    @EventHandler
    public void handle(OrderCreatedEvent event) {
        commandGateway.send(
                new PaymentCreateCommand(
                        UUID.randomUUID().toString(),
                        event.getOrderId(),
                        event.getPrice(),
                        event.getNumber(),
                        event.getProductId(),
                        event.getUserid()
                )
        );
    }

}
