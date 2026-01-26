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
    private final OrderProjectionRepository orderRepository;

    // =======================
    // COMMAND SIDE (STRICT)
    // =======================
    public void create(OrderDto orderDto) {
        // Step 1: Get product with resilience protection
        ProductDto productDto = getProductWithResilience(orderDto.getProductid());

        // Step 2: Business validation (NOT protected - fails fast with proper HTTP codes)
        validateOrder(productDto, orderDto);

        // Step 3: Create and send command
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
    // RESILIENCE LAYER (Infrastructure only)
    // =======================
    @CircuitBreaker(name = "productCommand", fallbackMethod = "getProductFallback")
    @Retry(name = "productCommand")
    @RateLimiter(name = "productCommand")
    private ProductDto getProductWithResilience(String productId) {
        return productEndpoint.getById(productId);
    }

    private ProductDto getProductFallback(String productId, Throwable ex) {
        log.error("Product service unavailable. productId={}", productId, ex);
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Product service temporarily unavailable. Please try again later."
        );
    }

    // =======================
    // BUSINESS VALIDATION (No resilience - fail fast)
    // =======================
    private void validateOrder(ProductDto productDto, OrderDto orderDto) {
        if (productDto == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Product not found"
            );
        }

        if (productDto.getStock() < orderDto.getNumber()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Insufficient stock available"
            );
        }
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

    // =======================
    // SYNCHRONOUS QUERIES (Read-only - OK for CQRS)
    // =======================
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

    // WARNING: This violates CQRS - should use Command/Event pattern instead
    public boolean updateOrderPaymentStatus(String orderId, String paymentId) {
        Optional<OrderModel> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            OrderModel order = optionalOrder.get();
            // TODO: Replace with proper CQRS command:
            // commandGateway.sendAndWait(new MarkOrderAsPaidCommand(orderId, paymentId));
            orderRepository.save(order);
            return true;
        }
        return false;
    }
}