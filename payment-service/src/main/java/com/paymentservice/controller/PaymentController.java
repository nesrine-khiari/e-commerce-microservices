package com.paymentservice.controller;

import com.paymentservice.command.PaymentCreateCommand;
import com.paymentservice.model.PaymentModel;
import com.paymentservice.projection.PaymentProjection;
import com.paymentservice.query.FindPaymentsByUserIdQuery;
import com.paymentservice.query.GetAllPaymentsQuery;
import com.paymentservice.query.GetPaymentQuery;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final QueryGateway queryGateway;
    private final CommandGateway commandGateway;
    
    @Autowired
    private PaymentProjection paymentProjection;
    
    // ========== DTOs ==========
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreatePaymentRequest {
        private String orderId;
        private BigDecimal price;
        private Integer quantity;
        private String productId;
        private String userId;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor  
    public static class CreatePaymentViaCommandRequest {
        private String orderId;
        private BigDecimal price;
        private Integer quantity;
        private String productId;
        private String userId;
    }
    
    // ========== ENDPOINTS POST ==========
    
    @PostMapping("/via-command")
    public CompletableFuture<String> createPaymentViaCommand(@RequestBody CreatePaymentViaCommandRequest request) {
        log.info("🎯 POST /via-command: Creating payment via Axon Command");
        
        String paymentId = "PAY-" + UUID.randomUUID().toString().substring(0, 8);
        
        PaymentCreateCommand command = PaymentCreateCommand.builder()
                .paymentId(paymentId)
                .orderId(request.getOrderId())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .productId(request.getProductId())
                .userId(request.getUserId())
                .build();
        
        log.info("📤 Sending command: {}", command);
        return commandGateway.send(command)
                .thenApply(result -> "✅ Payment created via command with ID: " + paymentId)
                .exceptionally(ex -> "❌ Error: " + ex.getMessage());
    }
    
    @PostMapping("/via-projection")
    public String createPaymentViaProjection(@RequestBody CreatePaymentRequest request) {
        log.info("🎯 POST /via-projection: Creating payment directly via projection");
        
        String paymentId = "PAY-PROJ-" + UUID.randomUUID().toString().substring(0, 8);
        BigDecimal totalAmount = request.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
        
        com.paymentservice.event.PaymentCreatedEvent event = 
            com.paymentservice.event.PaymentCreatedEvent.builder()
                .paymentId(paymentId)
                .orderId(request.getOrderId())
                .totalAmount(totalAmount)
                .userId(request.getUserId())
                .quantity(request.getQuantity())
                .productId(request.getProductId())
                .build();
        
        log.info("📤 Creating event: {}", event);
        
        try {
            paymentProjection.on(event);
            log.info("✅ Payment created via projection with ID: {}", paymentId);
            return "✅ Payment created via projection with ID: " + paymentId;
        } catch (Exception e) {
            log.error("❌ Error: ", e);
            return "❌ Error: " + e.getMessage();
        }
    }
    
    @PostMapping("/simple")
    public String createSimplePayment(
            @RequestParam String orderId,
            @RequestParam BigDecimal price,
            @RequestParam Integer quantity,
            @RequestParam String productId,
            @RequestParam String userId) {
        
        log.info("🎯 POST /simple: Creating simple payment");
        
        return String.format(
            "✅ Simple payment created! Order: %s, Price: %s, Qty: %d, Product: %s, User: %s",
            orderId, price, quantity, productId, userId
        );
    }
    
    // ========== ENDPOINTS GET ==========
    
    @GetMapping
    public CompletableFuture<List<PaymentModel>> getAllPayments() {
        log.info("📋 GET /: Getting all payments");
        return queryGateway.query(
            new GetAllPaymentsQuery(), 
            ResponseTypes.multipleInstancesOf(PaymentModel.class)
        );
    }

    @GetMapping("/{paymentId}")
    public CompletableFuture<PaymentModel> getPayment(@PathVariable String paymentId) {
        log.info("🔍 GET /{}: Getting payment by ID", paymentId);
        return queryGateway.query(
            new GetPaymentQuery(paymentId), 
            ResponseTypes.instanceOf(PaymentModel.class)
        );
    }

    @GetMapping("/user/{userId}")
    public CompletableFuture<List<PaymentModel>> getPaymentsByUserId(@PathVariable String userId) {
        log.info("👤 GET /user/{}: Getting payments for user", userId);
        return queryGateway.query(
            new FindPaymentsByUserIdQuery(userId), 
            ResponseTypes.multipleInstancesOf(PaymentModel.class)
        );
    }

    @GetMapping("/health")
    public String health() {
        return "✅ Payment Service is running on port 8086";
    }
    
    @GetMapping("/test")
    public String test() {
        return """
               Payment Service Test Endpoint
               
               🎯 POST Endpoints:
               - POST /api/payments/via-command    → Create payment via Axon Command
               - POST /api/payments/via-projection → Create payment directly via projection
               - POST /api/payments/simple         → Simple test endpoint (query params)
               
               📋 GET Endpoints:
               - GET /api/payments                 → List all payments
               - GET /api/payments/{id}            → Get payment details
               - GET /api/payments/user/{id}       → Get payments by user
               - GET /api/payments/health          → Health check
               - GET /api/payments/test            → This page
               
               🧪 Example POST to /via-command:
               {
                 "orderId": "ORDER-123",
                 "price": 99.99,
                 "quantity": 3,
                 "productId": "PROD-456",
                 "userId": "USER-789"
               }
               
               🧪 Example POST to /simple (query params):
               /api/payments/simple?orderId=TEST&price=50&quantity=2&productId=PROD&userId=USER
               """;
    }
}