package com.userservice.aggregate;

import com.core.event.UserCreatedEvent;
import com.userservice.command.CreateUserCommand;
import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate
@NoArgsConstructor
public class User {

    @AggregateIdentifier
    private String id;
    private String name;


    @CommandHandler
    public User(CreateUserCommand command) {
        apply(new UserCreatedEvent(
                command.getId(),
                command.getName()
        ));
    }

    @EventSourcingHandler
    public void handle(UserCreatedEvent event) {
        this.id = event.getId();
        this.name = event.getName();
    }
}
