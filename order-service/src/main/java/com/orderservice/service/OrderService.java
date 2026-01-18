package com.orderservice.service;

import com.orderservice.client.ProductEndpoint;
import com.orderservice.command.CreateOrderCommand;
import com.orderservice.dto.OrderDto;
import com.orderservice.dto.ProductDto;
import com.orderservice.model.OrderModel;
import com.orderservice.query.GetOrdersQuery;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;
    private final ProductEndpoint productEndpoint;

    // =======================
    // COMMAND SIDE (STRICT)
    // =======================
    @CircuitBreaker(name = "productCommand")
    @Retry(name = "productCommand")
    @RateLimiter(name = "productCommand")
    public void create(OrderDto orderDto) {

        ProductDto productDto =
                productEndpoint.getById(orderDto.getProductid());

        if (productDto == null || productDto.getStock() <= 0) {
            throw new IllegalStateException("Product unavailable or out of stock");
        }

        CreateOrderCommand cmd = new CreateOrderCommand(
                UUID.randomUUID().toString(),
                productDto.getPrice(),
                orderDto.getNumber(),
                orderDto.getProductid(),
                orderDto.getUserid()
        );

        commandGateway.sendAndWait(cmd);
    }

    // =======================
    // QUERY SIDE (TOLERANT)
    // =======================
    public CompletableFuture<List<OrderModel>> getAll() {
        return queryGateway.query(
                new GetOrdersQuery(),
                ResponseTypes.multipleInstancesOf(OrderModel.class)
        );
    }
}
