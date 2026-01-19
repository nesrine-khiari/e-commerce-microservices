package com.orderservice.controller;

import com.orderservice.dto.OrderDto;
import com.orderservice.model.OrderModel;
import com.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // VOTRE CODE EXISTANT
    @PostMapping
    public void handle(@RequestBody OrderDto orderDto) throws Exception {
        orderService.create(orderDto);
    }

    @GetMapping
    public CompletableFuture<List<OrderModel>> getOrders() {
        return orderService.getAll();
    }

    // NOUVEAUX ENDPOINTS AVEC DONNÉES RÉELLES
    
    @GetMapping("/{orderId}/exists")
    public ResponseEntity<Boolean> checkOrderExists(@PathVariable("orderId") String orderId) {
        System.out.println("✅ Order Service: Checking if order exists: " + orderId);
        boolean exists = orderService.orderExists(orderId);
        System.out.println("📊 Result: " + exists);
        return ResponseEntity.ok(exists);
    }
    
    @GetMapping("/{orderId}/price")
    public ResponseEntity<BigDecimal> getOrderPrice(@PathVariable("orderId") String orderId) {
        System.out.println("✅ Order Service: Getting price for order: " + orderId);
        
        Optional<BigDecimal> price = orderService.getOrderPrice(orderId);
        
        if (price.isPresent()) {
            System.out.println("💰 Price found: " + price.get());
            return ResponseEntity.ok(price.get());
        } else {
            System.out.println("❌ Order not found: " + orderId);
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderDetails(@PathVariable("orderId") String orderId) {
        System.out.println("✅ Order Service: Getting details for order: " + orderId);
        
        Optional<OrderModel> order = orderService.getOrderDetails(orderId);
        
        if (order.isPresent()) {
            OrderModel orderModel = order.get();
            System.out.println("📋 Order found: " + orderModel);
            
            // Retourner les données dans le format attendu
            return ResponseEntity.ok(Map.of(
                "id", orderModel.getId(),
                "productid", orderModel.getProductid(),
                "number", orderModel.getNumber(),
                "price", orderModel.getPrice()
            ));
        } else {
            System.out.println("❌ Order not found: " + orderId);
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/{orderId}/update-payment")
    public ResponseEntity<Map<String, Object>> updateOrderPaymentStatus(
            @PathVariable("orderId") String orderId,
            @RequestBody Map<String, Object> request) {
        
        System.out.println("💰 Order Service: Payment update for order: " + orderId);
        System.out.println("📋 Request: " + request);
        
        String paymentId = (String) request.get("paymentId");
        boolean updated = orderService.updateOrderPaymentStatus(orderId, paymentId);
        
        if (updated) {
            return ResponseEntity.ok(Map.of(
                "orderId", orderId,
                "status", "UPDATED",
                "paymentStatus", "PAID",
                "paymentId", paymentId,
                "timestamp", System.currentTimeMillis()
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Order not found",
                "orderId", orderId
            ));
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("✅ Order Service is running on port 8081");
    }
}