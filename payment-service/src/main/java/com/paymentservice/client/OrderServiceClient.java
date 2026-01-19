package com.paymentservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.Map;

/**
 * CLIENT FEIGN POUR COMMUNICATION SYNCHRONE avec Order Service
 * 
 * UTILITÉ : 
 * - Récupère le prix d'une commande pour calculer total_amount
 * - Vérifie si une commande existe avant paiement
 * - Démontre la communication synchrone REST
 */
@FeignClient(
    name = "order-service",
    url = "${service.order.url}",
    fallback = OrderServiceClient.OrderServiceFallback.class
)
public interface OrderServiceClient {

    @GetMapping("/api/orders/{orderId}/price")
    BigDecimal getOrderPrice(@PathVariable("orderId") String orderId);

    @GetMapping("/api/orders/{orderId}/exists")
    Boolean checkOrderExists(@PathVariable("orderId") String orderId);

    @GetMapping("/api/orders/{orderId}")
    OrderResponse getOrderDetails(@PathVariable("orderId") String orderId);

    @PostMapping("/api/orders/{orderId}/update-payment")
    Map<String, Object> updateOrderAfterPayment(
            @PathVariable("orderId") String orderId,
            @RequestBody PaymentUpdateRequest request
    );

    record OrderResponse(String id, String productid, Integer number, BigDecimal price) {}

    record PaymentUpdateRequest(String paymentId, String status, BigDecimal amount) {}

    @Component
    class OrderServiceFallback implements OrderServiceClient {

        @Override
        public BigDecimal getOrderPrice(String orderId) {
            throw new RuntimeException("Order Service unavailable - cannot fetch price for order " + orderId);
        }

        @Override
        public Boolean checkOrderExists(String orderId) {
            throw new RuntimeException("Order Service unavailable - cannot check existence for order " + orderId);
        }

        @Override
        public OrderResponse getOrderDetails(String orderId) {
            throw new RuntimeException("Order Service unavailable - cannot get details for order " + orderId);
        }

        @Override
        public Map<String, Object> updateOrderAfterPayment(String orderId, PaymentUpdateRequest request) {
            throw new RuntimeException("Order Service unavailable - cannot update order " + orderId);
        }
    }
}
