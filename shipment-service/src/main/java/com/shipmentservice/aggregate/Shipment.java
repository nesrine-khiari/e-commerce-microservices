package com.shipmentservice.aggregate;

import com.shipmentservice.command.CreateShipmentCommand;
import com.shipmentservice.command.ShipmentDeliverCommand;
import com.shipmentservice.event.CreateShipmentEvent;
import com.shipmentservice.event.DeliverSuccessEvent;
import com.shipmentservice.event.ShipmentDeliverEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateMember;
import org.axonframework.spring.stereotype.Aggregate;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate
public class Shipment {

    @AggregateIdentifier
    private String id;
    private String paymentId;
    private String productId;
    private Integer productCount;
    private String address;
    private String cargoFirm;

    @AggregateMember
    private ShipmentTransaction transaction;


    @CommandHandler
    public Shipment(CreateShipmentCommand command) {
        apply(new CreateShipmentEvent(
                command.getId(),
                command.getPaymentId(),
                command.getProductId(),
                command.getProductCount(),
                command.getAddress(),
                command.getCargoFirm()
        ));
    }

    @CommandHandler
    public Shipment(ShipmentDeliverCommand command) {
        apply(new ShipmentDeliverEvent(command.getTransactionId()));
    }


    @EventSourcingHandler
    public void handle(CreateShipmentEvent event) {
        this.id = event.getId();
        this.paymentId = event.getPaymentId();
        this.productId = event.getProductId();
        this.productCount = event.getProductCount();
        this.address = event.getAddress();
        this.cargoFirm = event.getCargoFirm();
    }

    @EventSourcingHandler
    public void on(DeliverSuccessEvent event) {
        transaction = new ShipmentTransaction(event.getTransactionId());
    }
}
