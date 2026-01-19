package com.orderservice.service;
import java.util.Optional;
import com.orderservice.client.ProductEndpoint;
import com.orderservice.command.CreateOrderCommand;
import com.orderservice.dto.OrderDto;
import com.orderservice.dto.ProductDto;
import com.orderservice.model.OrderModel;
import com.orderservice.query.GetOrdersQuery;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import com.orderservice.repository.OrderProjectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;
    private final ProductEndpoint productEndpoint;
    private final OrderProjectionRepository orderRepository; // AJOUTEZ CE REPOSITORY

    // =======================
    // COMMAND SIDE (STRICT)
    // =======================
    @CircuitBreaker(name = "productCommand", fallbackMethod = "createFallback")
    @Retry(name = "productCommand")
    @RateLimiter(name = "productCommand")
    public void create(OrderDto orderDto) {

        ProductDto productDto =
                productEndpoint.getById(orderDto.getProductid());

        // Business validation (NO retry)
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
    // FALLBACK (MANDATORY)
    // =======================
    public void createFallback(OrderDto orderDto, Throwable ex) {

        log.error(
                "Product service unavailable. Order NOT created. productId={}",
                orderDto.getProductid(),
                ex
        );

        // Convert infra failure → clean business HTTP error
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Product service temporarily unavailable. Please try again later."
        );
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
    
    // NOUVELLES MÉTHODES POUR LA COMMUNICATION SYNCHRONE
    
    public boolean orderExists(String orderId) {
        return orderRepository.existsById(orderId);
    }
    
    public Optional<BigDecimal> getOrderPrice(String orderId) {
        return orderRepository.findById(orderId)
                .map(OrderModel::getPrice);
    }
    
    public Optional<OrderModel> getOrderDetails(String orderId) {
        return orderRepository.findById(orderId);
    }
    
    public boolean updateOrderPaymentStatus(String orderId, String paymentId) {
        Optional<OrderModel> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            OrderModel order = optionalOrder.get();
            // Vous pourriez ajouter un champ status dans OrderModel si nécessaire
            // order.setStatus("PAID");
            // order.setPaymentId(paymentId);
            orderRepository.save(order);
            return true;
        }
        return false;
    }
}