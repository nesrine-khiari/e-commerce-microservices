package com.accountservice.handler;

import com.accountservice.command.CreateAccountCommand;
import com.accountservice.command.DepositMoneyCommand;
import com.core.event.UserCreatedEvent;
import com.core.event.UserDepositMoneyEvent;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserEventHandler {

    private final CommandGateway commandGateway;

    @EventHandler
    public void handle(UserCreatedEvent event) {
        commandGateway.send(
                new CreateAccountCommand(
                        UUID.randomUUID().toString(),
                        event.getId(),
                        BigDecimal.ZERO
                )
        );
    }

    @EventHandler
    public void handle(UserDepositMoneyEvent event) {
        commandGateway.send(
                new DepositMoneyCommand(
                        UUID.randomUUID().toString(),
                        event.getUserid(),
                        event.getAmount()
                )
        );
    }
}