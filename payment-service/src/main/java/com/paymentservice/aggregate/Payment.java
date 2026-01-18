package com.paymentservice.aggregate;

import com.paymentservice.command.PaymentCreateCommand;
import com.paymentservice.event.PaymentCreatedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import java.math.BigDecimal;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate
public class Payment {
    
    @AggregateIdentifier
    private String paymentId;
    private String orderId;
    private BigDecimal totalAmount;
    private String userId;
    private String status;
    private Integer quantity;
    private String productId;

    protected Payment() {
        // Required by Axon
    }

    @CommandHandler
    public Payment(PaymentCreateCommand command) {
        BigDecimal total = command.getPrice().multiply(BigDecimal.valueOf(command.getQuantity()));
        
        apply(PaymentCreatedEvent.builder()
                .paymentId(command.getPaymentId())
                .orderId(command.getOrderId())
                .totalAmount(total)
                .userId(command.getUserId())
                .quantity(command.getQuantity())
                .productId(command.getProductId())
                .build());
    }

    @EventSourcingHandler
    public void on(PaymentCreatedEvent event) {
        this.paymentId = event.getPaymentId();
        this.orderId = event.getOrderId();
        this.userId = event.getUserId();
        this.totalAmount = event.getTotalAmount();
        this.quantity = event.getQuantity();
        this.productId = event.getProductId();
        this.status = "PAID";
    }
}