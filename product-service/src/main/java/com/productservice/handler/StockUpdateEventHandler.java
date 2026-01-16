package com.productservice.handler;

import com.core.event.OrderCreatedEvent;
import com.productservice.command.UpdateStockCommand;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockUpdateEventHandler {
    private final CommandGateway commandGateway;

    @EventHandler
    public void handle(OrderCreatedEvent event) {
        commandGateway.send(
                new UpdateStockCommand(
                        event.getProductId(),
                        event.getNumber()
                )
        );
    }

}
