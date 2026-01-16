package com.orderservice.aggregate;

import com.core.event.OrderCreatedEvent;
import com.orderservice.command.CreateOrderCommand;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.InterceptorChain;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.CommandHandlerInterceptor;
import org.axonframework.spring.stereotype.Aggregate;

import java.math.BigDecimal;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate
public class Order {

    @AggregateIdentifier
    private String id;
    private BigDecimal price;
    private Integer number;
    private String productId;

    @CommandHandler
    public Order(CreateOrderCommand command) {
        apply(OrderCreatedEvent
                .builder()
                .orderId(command.getOrderId())
                .price(command.getPrice())
                .number(command.getNumber())
                .productId(command.getProductId())
                .userid(command.getUserid())
                .build());
    }

    @EventSourcingHandler
    public void on(OrderCreatedEvent orderCreatedEvent) {
        id = orderCreatedEvent.getOrderId();
        price = orderCreatedEvent.getPrice();
        number = orderCreatedEvent.getNumber();
        productId = orderCreatedEvent.getProductId();
    }


    @CommandHandlerInterceptor
    public void checkUser(CommandMessage<?> command, InterceptorChain chain) throws Exception {
        if (null != command.getMetaData().get("userid")) {
            chain.proceed();
        } else {
            throw new Exception();
        }
    }
}
