package com.accountservice.handler;

import com.accountservice.command.WithdrawMoneyCommand;
import com.core.event.PaymentCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class PaymentCreatedEventHandler {

    private final CommandGateway commandGateway;

    @EventHandler
    public void handle(PaymentCreatedEvent event) {
        commandGateway.send(
                new WithdrawMoneyCommand(
                        event.getUserid(),
                        event.getTotalAmount(),
                        new Date().toString()
                )
        );
    }
}