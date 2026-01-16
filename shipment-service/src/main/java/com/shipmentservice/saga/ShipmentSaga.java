package com.shipmentservice.saga;

import com.core.event.PaymentCreatedEvent;
import com.shipmentservice.command.CreateShipmentCommand;
import com.shipmentservice.command.ShipmentDeliverCommand;
import com.shipmentservice.event.CreateShipmentEvent;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Saga
public class ShipmentSaga {

    private transient CommandGateway commandGateway;
    private String paymentId;
    private String productId;
    private Integer productCount;
    private String address;
    private String cargoFirm;

    @Autowired
    public void setCommandGateway(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @StartSaga
    @SagaEventHandler(associationProperty = "paymentId")
    public void on(PaymentCreatedEvent event) {
        this.paymentId = event.getPaymentId();
        this.productId = event.getProductId();
        this.productCount = event.getNumber();
        this.address = "ADRESS";
        this.cargoFirm = "TNT";

        SagaLifecycle.associateWith("productId", productId);

        commandGateway.send(new CreateShipmentCommand(UUID.randomUUID().toString(), paymentId, productId, productCount, address, cargoFirm))
                .exceptionally(throwable -> {
                    /*commandGateway.send(new RejectShipmentCommand());*/
                    return null;
                });

    }

    @EndSaga
    @SagaEventHandler(associationProperty = "paymentId")
    public void on(CreateShipmentEvent event) {
        if (!paymentId.equals(event.getPaymentId())) {
            return;
        }

        commandGateway.sendAndWait(new ShipmentDeliverCommand(UUID.randomUUID().toString(), event.getPaymentId()));

        SagaLifecycle.end();
    }
}
