package com.paymentservice.service;

import com.paymentservice.client.OrderServiceClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderValidationService {

    private final OrderServiceClient orderServiceClient;

    @Retry(name = "orderService", fallbackMethod = "fallback")
    @CircuitBreaker(name = "orderService")
    public Boolean checkOrderExists(String orderId) {
        return orderServiceClient.checkOrderExists(orderId);
    }

    @Retry(name = "orderService", fallbackMethod = "fallback")
    @CircuitBreaker(name = "orderService")
    public BigDecimal getOrderPrice(String orderId) {
        return orderServiceClient.getOrderPrice(orderId);
    }

    @Retry(name = "orderService", fallbackMethod = "fallback")
    @CircuitBreaker(name = "orderService")
    public OrderServiceClient.OrderResponse getOrderDetails(String orderId) {
        return orderServiceClient.getOrderDetails(orderId);
    }

    // Fallback qui déclenche un HTTP 503
    public <T> T fallback(String orderId, Throwable ex) {
        log.error("Order service unavailable for order {}", orderId, ex);
        throw new RuntimeException("Order service unavailable");
    }
}
