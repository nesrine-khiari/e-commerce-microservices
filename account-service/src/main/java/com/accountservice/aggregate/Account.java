package com.accountservice.aggregate;

import com.accountservice.command.CreateAccountCommand;
import com.accountservice.command.DepositMoneyCommand;
import com.accountservice.command.WithdrawMoneyCommand;
import com.accountservice.event.AccountCreateEvent;
import com.accountservice.event.DepositMoneyEvent;
import com.accountservice.event.WithdrawMoneyEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import java.math.BigDecimal;
import java.util.UUID;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate(snapshotTriggerDefinition = "accountSnapshotTriggerDefinition")
public class Account {

    @AggregateIdentifier
    private String id;
    private String userid;
    private BigDecimal balance;
    private String timestamp;

    @CommandHandler
    public Account(CreateAccountCommand command) {
        apply(new AccountCreateEvent(
                command.getId(),
                command.getUserid(),
                command.getBalance()
        ));
    }

    @CommandHandler
    public Account(WithdrawMoneyCommand command) {
        apply(new WithdrawMoneyEvent(
                command.getUserid(),
                command.getAmount(),
                command.getTimestamp()
        ));
    }

    @CommandHandler
    public Account(DepositMoneyCommand command) {
        apply(new DepositMoneyEvent(
                command.getId(),
                command.getUserid(),
                command.getAmount()
        ));
    }

    @EventSourcingHandler
    protected void createAccount(AccountCreateEvent event) {
        this.id = event.getId();
        this.userid = event.getUserid();
        this.balance = event.getBalance();
    }

    @EventSourcingHandler
    protected void depositMoney(DepositMoneyEvent event) {
        if (event.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException();
        }
        this.id = event.getId();
        this.userid = event.getUserid();
        this.balance = event.getAmount();
    }

    @EventSourcingHandler
    protected void withdrawMoney(WithdrawMoneyEvent event) {
        this.id = UUID.randomUUID().toString();
        this.userid = event.getUserid();
        this.balance = event.getAmount();
        this.timestamp = event.getTimestamp();
    }
}
