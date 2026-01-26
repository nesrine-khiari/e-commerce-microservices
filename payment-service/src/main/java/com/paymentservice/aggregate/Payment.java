package com.paymentservice.aggregate;

import com.paymentservice.command.PaymentCreateCommand;
import com.paymentservice.command.ConfirmPaymentCommand;
import com.paymentservice.command.CancelPaymentCommand;
import com.paymentservice.event.PaymentCreatedEvent;
import com.paymentservice.event.PaymentConfirmedEvent;
import com.paymentservice.event.PaymentCanceledEvent;
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

    protected Payment() {}

    // Création paiement
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

    // Confirmation paiement → PAID
    @CommandHandler
    public void handle(ConfirmPaymentCommand command) {
        if ("PAID".equals(this.status)) {
            throw new IllegalStateException("Payment already confirmed");
        }
        if ("CANCELED".equals(this.status)) {
            throw new IllegalStateException("Cannot confirm a canceled payment");
        }
        apply(new PaymentConfirmedEvent(this.paymentId));
    }

    // Annulation paiement → CANCELED
    @CommandHandler
    public void handle(CancelPaymentCommand command) {
        if ("PAID".equals(this.status)) {
            throw new IllegalStateException("Cannot cancel a confirmed payment");
        }
        if ("CANCELED".equals(this.status)) {
            throw new IllegalStateException("Payment already canceled");
        }
        apply(new PaymentCanceledEvent(this.paymentId));
    }

    // Event sourcing handlers
    @EventSourcingHandler
    public void on(PaymentCreatedEvent event) {
        this.paymentId = event.getPaymentId();
        this.orderId = event.getOrderId();
        this.userId = event.getUserId();
        this.totalAmount = event.getTotalAmount();
        this.quantity = event.getQuantity();
        this.productId = event.getProductId();
        this.status = "EN ATTENTE";
    }

    @EventSourcingHandler
    public void on(PaymentConfirmedEvent event) {
        this.status = "PAID";
    }

    @EventSourcingHandler
    public void on(PaymentCanceledEvent event) {
        this.status = "CANCELED";
    }
}
