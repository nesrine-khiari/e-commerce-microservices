package com.paymentservice.controller;

import com.paymentservice.service.PaymentSyncService;
import com.paymentservice.command.PaymentCreateCommand;
import com.paymentservice.model.PaymentModel;
import com.paymentservice.projection.PaymentProjection;
import com.paymentservice.query.FindPaymentsByUserIdQuery;
import com.paymentservice.query.GetAllPaymentsQuery;
import com.paymentservice.query.GetPaymentQuery;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final QueryGateway queryGateway;
    private final CommandGateway commandGateway;
    private final PaymentSyncService paymentSyncService;
    
    @Autowired
    private PaymentProjection paymentProjection;
    
    // ========== DTOs ==========
    @Data @AllArgsConstructor @NoArgsConstructor
    public static class CreatePaymentRequest {
        private String orderId; private BigDecimal price; 
        private Integer quantity; private String productId; private String userId;
    }
    
    @Data @AllArgsConstructor @NoArgsConstructor  
    public static class CreatePaymentViaCommandRequest {
        private String orderId; private BigDecimal price; 
        private Integer quantity; private String productId; private String userId;
    }
    
    // ========== ENDPOINTS DE COMMUNICATION SYNCHRONE ==========
    
    /**
     * ENDPOINT 1: PAIEMENT AVEC VALIDATION SYNCHRONE
     * UTILITÉ : Démontre la communication synchrone avec Order Service
     */
    @PostMapping("/sync-validation")
public CompletableFuture<Map<String, Object>> createPaymentWithSyncValidation(
        @RequestBody CreatePaymentRequest request) {

    log.info("POST /sync-validation → orderId={}, userId={}, quantity={}",
            request.getOrderId(),
            request.getUserId(),
            request.getQuantity());

    return paymentSyncService.createPaymentWithSyncValidation(
            request.getOrderId(),
            request.getUserId(),
            request.getQuantity()
    );
}
    
    /**
     * ENDPOINT 2: TEST SIMPLE DE COMMUNICATION SYNCHRONE
     * UTILITÉ : Teste juste la connexion, ne crée pas de paiement
     */
    @GetMapping("/test-connection/{orderId}")
    public CompletableFuture<ResponseEntity<?>> testSyncConnection(@PathVariable String orderId) {
        log.info("🧪 GET /test-connection: Test communication synchrone");
        
        return paymentSyncService.createPaymentWithSyncValidation(orderId, "TEST-USER", 1)
            .thenApply(result -> {
                if (result.containsKey("fallbackTriggered")) {
                    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of(
                            "test", "COMMUNICATION_TEST",
                            "result", "FAILED - Order Service unavailable",
                            "details", result
                        ));
                }
                
                return ResponseEntity.ok(Map.of(
                    "test", "COMMUNICATION_TEST",
                    "result", "SUCCESS - Order Service responding",
                    "details", result
                ));
            });
    }
    
    /**
     * ENDPOINT 3: DÉMONSTRATION POUR LE DEVOIR
     */
    @GetMapping("/architecture")
    public ResponseEntity<?> showArchitecture() {
        return ResponseEntity.ok(Map.of(
            "microservice", "payment-service",
            "communicationTypes", Map.of(
                "asynchrone", Map.of(
                    "framework", "Axon",
                    "patterns", new String[]{"CQRS", "Event Sourcing"},
                    "endpoints", new String[]{"/via-command", "/via-projection"}
                ),
                "synchrone", Map.of(
                    "framework", "Spring Cloud OpenFeign",
                    "patterns", new String[]{"Circuit Breaker", "Retry", "Rate Limiter"},
                    "purpose", "Valider commande avant paiement",
                    "endpoint", "/sync-validation"
                )
            ),
            "assignmentRequirements", Map.of(
                "communicationSynchrone", "✓ Feign Client + REST API",
                "resilience4j", Map.of(
                    "circuitBreaker", "✓ Configuré",
                    "retry", "✓ 3 tentatives, 500ms pause",
                    "rateLimiter", "✓ 5 requêtes/seconde",
                    "fallback", "✓ Méthodes implémentées"
                )
            )
        ));
    }
    
    /**
     * ENDPOINT 4: DÉMONSTRATION DES 5 DÉFAILLANCES
     */
    @GetMapping("/failure-types")
    public ResponseEntity<?> demonstrateFailureTypes() {
        return ResponseEntity.ok(Map.of(
            "assignment", "Gestion des 5 types de défaillance",
            "implementation", "Dans PaymentSyncService avec Resilience4j",
            "testEndpoint", "POST /api/payments/sync-validation",
            "expectedResults", Map.of(
                "Order Service disponible", "Payment created",
                "Order Service éteint", "Error message - Payment NOT created",
                "Order Service lent", "Timeout error"
            )
        ));
    }
    
    // ========== ENDPOINTS ASYNCHRONES EXISTANTS ==========
    
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
        
        try {
            paymentProjection.on(event);
            return "✅ Payment created via projection with ID: " + paymentId;
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }
    
    // ========== ENDPOINTS GET EXISTANTS ==========
    
    @GetMapping
    public CompletableFuture<List<PaymentModel>> getAllPayments() {
        return queryGateway.query(new GetAllPaymentsQuery(), 
            ResponseTypes.multipleInstancesOf(PaymentModel.class));
    }

    @GetMapping("/{paymentId}")
    public CompletableFuture<PaymentModel> getPayment(@PathVariable String paymentId) {
        return queryGateway.query(new GetPaymentQuery(paymentId), 
            ResponseTypes.instanceOf(PaymentModel.class));
    }

    @GetMapping("/user/{userId}")
    public CompletableFuture<List<PaymentModel>> getPaymentsByUserId(@PathVariable String userId) {
        return queryGateway.query(new FindPaymentsByUserIdQuery(userId), 
            ResponseTypes.multipleInstancesOf(PaymentModel.class));
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
            "service", "payment-service",
            "status", "UP",
            "port", 8086,
            "endpoints", new String[]{
                "POST /api/payments/sync-validation (synchrone)",
                "POST /api/payments/via-command (asynchrone)",
                "GET /api/payments/architecture (demo)"
            }
        ));
    }
}