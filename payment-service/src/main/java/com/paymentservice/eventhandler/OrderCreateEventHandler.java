package com.paymentservice.eventhandler;

import com.paymentservice.command.PaymentCreateCommand;
import com.core.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@ProcessingGroup("payments") // doit correspondre au nom du processor
public class OrderCreateEventHandler {

    private final CommandGateway commandGateway;

    @EventHandler
    public void handle(OrderCreatedEvent event) {
        System.out.println("💰 Order Event Received for order: " + event.getOrderId());

        // Envoi du command PaymentCreateCommand
        commandGateway.send(
                PaymentCreateCommand.builder()
                        .paymentId(UUID.randomUUID().toString())
                        .orderId(event.getOrderId())
                        .price(event.getPrice())
                        .quantity(event.getNumber())
                        .productId(event.getProductId())
                        .userId(event.getUserid())
                        .build()
        );

        System.out.println("✅ Payment command sent for order: " + event.getOrderId());
    }
}
