package com.paymentservice.service;

import com.paymentservice.client.OrderServiceClient;
import com.paymentservice.command.PaymentCreateCommand;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentSyncService {

    private final OrderServiceClient orderServiceClient;
    private final CommandGateway commandGateway;
    private final RetryRegistry retryRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public CompletableFuture<Map<String, Object>> createPaymentWithSyncValidation(
            String orderId,
            String userId,
            Integer quantity) {

        log.info("Starting synchronous validation for order: {}", orderId);

        return CompletableFuture.supplyAsync(() -> {

            // 1️⃣ Vérifier si la commande existe
            Boolean exists = checkOrderExists(orderId);
            if (Boolean.FALSE.equals(exists)) {
                throw new OrderValidationException("Order not found: " + orderId);
            }

            // 2️⃣ Récupérer le prix
            BigDecimal price = getOrderPrice(orderId);
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new OrderValidationException("Invalid order price: " + price);
            }

            // 3️⃣ Récupérer les détails de la commande
            OrderServiceClient.OrderResponse details = getOrderDetails(orderId);
            String productId = (details != null && details.productid() != null)
                    ? details.productid()
                    : "UNKNOWN-PRODUCT";

            BigDecimal totalAmount = price.multiply(BigDecimal.valueOf(quantity));

            // 4️⃣ Créer le paiement (Axon Command)
            String paymentId = "PAY-SYNC-" + UUID.randomUUID().toString().substring(0, 8);

            PaymentCreateCommand command = PaymentCreateCommand.builder()
                    .paymentId(paymentId)
                    .orderId(orderId)
                    .price(price)
                    .quantity(quantity)
                    .productId(productId)
                    .userId(userId)
                    .build();

            log.info("Sending PaymentCreateCommand for paymentId={}", paymentId);
            commandGateway.send(command).join();

            return Map.<String, Object>of(
                    "status", "SUCCESS",
                    "paymentId", paymentId,
                    "orderId", orderId,
                    "quantity", quantity,
                    "price", price,
                    "totalAmount", totalAmount,
                    "productId", productId,
                    "userId", userId,
                    "validationType", "SYNCHRONOUS"
            );

        }).exceptionally(ex -> {
            log.error("Payment creation failed", ex);
            return Map.<String, Object>of(
                    "status", "ERROR",
                    "message", ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage(),
                    "orderId", orderId,
                    "fallback", true
            );
        });
    }

    // =========================
    // APPELS DISTANTS PROTÉGÉS
    // =========================
    @CircuitBreaker(name = "orderService", fallbackMethod = "checkExistsFallback")
    @Retry(name = "orderService")
    @RateLimiter(name = "orderService")
    protected Boolean checkOrderExists(String orderId) {
        return orderServiceClient.checkOrderExists(orderId);
    }

    protected Boolean checkExistsFallback(String orderId, Throwable ex) {
        log.error("Order Service unavailable for existence check: {}", orderId, ex);
        throw new RuntimeException("Order Service unavailable - cannot check existence for order " + orderId);
    }

    @CircuitBreaker(name = "orderService", fallbackMethod = "getPriceFallback")
    @Retry(name = "orderService")
    @RateLimiter(name = "orderService")
    protected BigDecimal getOrderPrice(String orderId) {
        return orderServiceClient.getOrderPrice(orderId);
    }

    protected BigDecimal getPriceFallback(String orderId, Throwable ex) {
        log.error("Order Service unavailable for price: {}", orderId, ex);
        throw new RuntimeException("Order Service unavailable - cannot fetch price for order " + orderId);
    }

    @CircuitBreaker(name = "orderService", fallbackMethod = "getDetailsFallback")
    @Retry(name = "orderService")
    @RateLimiter(name = "orderService")
    protected OrderServiceClient.OrderResponse getOrderDetails(String orderId) {
        return orderServiceClient.getOrderDetails(orderId);
    }

    protected OrderServiceClient.OrderResponse getDetailsFallback(String orderId, Throwable ex) {
        log.error("Order Service unavailable for details: {}", orderId, ex);
        throw new RuntimeException("Order Service unavailable - cannot get details for order " + orderId);
    }

    // =========================
    // LOGGING AVANCÉ RESILIENCE4J
    // =========================
    @PostConstruct
    public void registerResilienceEvents() {
        io.github.resilience4j.retry.Retry retry = retryRegistry.retry("orderService");
        io.github.resilience4j.circuitbreaker.CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("orderService");

        retry.getEventPublisher()
                .onRetry(event -> log.warn("RETRY attempt {} - cause: {}", event.getNumberOfRetryAttempts(), event.getLastThrowable().toString()));

        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> log.warn("CIRCUIT BREAKER STATE CHANGE: {} -> {}", event.getStateTransition().getFromState(), event.getStateTransition().getToState()));
    }

    // =========================
    // EXCEPTION MÉTIER
    // =========================
    public static class OrderValidationException extends RuntimeException {
        public OrderValidationException(String message) {
            super(message);
        }
    }
}
