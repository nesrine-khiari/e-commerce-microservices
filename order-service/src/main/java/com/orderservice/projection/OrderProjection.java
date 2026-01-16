package com.orderservice.projection;

import com.core.event.OrderCreatedEvent;
import com.orderservice.model.OrderModel;
import com.orderservice.query.GetOrdersQuery;
import com.orderservice.repository.OrderProjectionRepository;
import lombok.RequiredArgsConstructor;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderProjection {

    private final EventGateway eventGateway;
    private final OrderProjectionRepository orderProjectionRepository;

    @EventHandler
    public void handle(OrderCreatedEvent orderCreatedEvent) {
        OrderModel orderModel = new OrderModel(
                orderCreatedEvent.getOrderId(),
                orderCreatedEvent.getPrice(),
                orderCreatedEvent.getNumber(),
                orderCreatedEvent.getProductId()
        );
        orderProjectionRepository.save(orderModel);
    }

    @QueryHandler
    public List<OrderModel> handle(GetOrdersQuery getOrdersQuery) {
        return orderProjectionRepository.findAll();
    }

}
