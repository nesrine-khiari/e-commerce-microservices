package com.paymentservice.controller;

import com.paymentservice.command.PaymentCreateCommand;
import com.paymentservice.command.ConfirmPaymentCommand;
import com.paymentservice.command.CancelPaymentCommand;
import com.paymentservice.model.PaymentModel;
import com.paymentservice.projection.PaymentProjection;
import com.paymentservice.query.FindPaymentsByUserIdQuery;
import com.paymentservice.query.GetAllPaymentsQuery;
import com.paymentservice.query.GetPaymentQuery;
import com.paymentservice.service.PaymentSyncService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
public class PaymentController {

    private final QueryGateway queryGateway;
    private final CommandGateway commandGateway;
    private final PaymentSyncService paymentSyncService;
    private final PaymentProjection paymentProjection;

    // ===================== DTO =====================
    public static record CreatePaymentRequest(
            String orderId,
            String userId,
            Integer quantity,
            BigDecimal price,
            String productId
    ) {}

    // ===================== SYNCHRONE =====================
    @PostMapping("/sync-validation")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> createPayment(
            @RequestBody CreatePaymentRequest request
    ) {
        log.info("SYNC PAYMENT orderId={} userId={} quantity={}",
                request.orderId(), request.userId(), request.quantity());

        return paymentSyncService.createPaymentWithSyncValidation(
                request.orderId(),
                request.userId(),
                request.quantity()
        );
    }

    @GetMapping("/test-connection/{orderId}")
    public CompletableFuture<ResponseEntity<?>> testConnection(
            @PathVariable String orderId,
            HttpServletRequest request
    ) {
        return paymentSyncService.createPaymentWithSyncValidation(orderId, "TEST", 1)
                .thenApply(result -> {
                    int port = request.getLocalPort();
                    log.info("Test connection - Instance port: {}", port);
                    return ResponseEntity.ok(Map.of(
                            "instancePort", port,
                            "result", result
                    ));
                });
    }

    // ===================== ASYNCHRONE (CQRS) =====================
    @PostMapping("/via-command")
    public CompletableFuture<String> createViaCommand(@RequestBody CreatePaymentRequest request) {
        String paymentId = "PAY-" + UUID.randomUUID().toString().substring(0, 8);

        PaymentCreateCommand command = PaymentCreateCommand.builder()
                .paymentId(paymentId)
                .orderId(request.orderId())
                .price(request.price())
                .quantity(request.quantity())
                .productId(request.productId())
                .userId(request.userId())
                .build();

        return commandGateway.send(command)
                .thenApply(r -> "Payment created: " + paymentId);
    }

    @PostMapping("/via-projection")
    public String createViaProjection(@RequestBody CreatePaymentRequest request) {
        String paymentId = "PAY-PROJ-" + UUID.randomUUID().toString().substring(0, 8);

        paymentProjection.on(
                com.paymentservice.event.PaymentCreatedEvent.builder()
                        .paymentId(paymentId)
                        .orderId(request.orderId())
                        .totalAmount(request.price().multiply(BigDecimal.valueOf(request.quantity())))
                        .userId(request.userId())
                        .quantity(request.quantity())
                        .productId(request.productId())
                        .build()
        );

        return "Payment created via projection: " + paymentId;
    }

    // ===================== QUERIES =====================
    @GetMapping
    public CompletableFuture<List<PaymentModel>> getAll() {
        return queryGateway.query(
                new GetAllPaymentsQuery(),
                ResponseTypes.multipleInstancesOf(PaymentModel.class)
        );
    }

    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<?>> getById(  // Changé ici
                                                          @PathVariable("id") String paymentId
    ) {
        log.info("Fetching payment with id: {}", paymentId);

        return queryGateway.query(
                new GetPaymentQuery(paymentId),
                ResponseTypes.instanceOf(PaymentModel.class)
        ).thenApply(payment -> {
            log.debug("Payment found: {}", payment != null);
            if (payment == null) {
                log.warn("Payment not found: {}", paymentId);
                return ResponseEntity.status(404).body(
                        Map.of("error", "Payment not found", "paymentId", paymentId)
                );
            }
            return ResponseEntity.ok(payment);
        }).exceptionally(ex -> {
            log.error("Error fetching payment {}", paymentId, ex);
            return ResponseEntity.status(500).body(
                    Map.of(
                            "error", "Internal server error",
                            "paymentId", paymentId,
                            "message", ex.getMessage(),
                            "exceptionType", ex.getClass().getName()
                    )
            );
        });
    }

    @GetMapping("/user/{userId}")
    public CompletableFuture<ResponseEntity<?>> getByUser(
            @PathVariable("userId") String userId
    ) {
        log.info("Fetching payments for user: {}", userId);

        CompletableFuture<List<PaymentModel>> paymentsFuture = queryGateway.query(
                new FindPaymentsByUserIdQuery(userId),
                ResponseTypes.multipleInstancesOf(PaymentModel.class)
        );

        return paymentsFuture.thenApply(payments -> {
            log.debug("Found {} payments for user {}", payments != null ? payments.size() : 0, userId);
            if (payments == null || payments.isEmpty()) {
                // On cast explicitement pour aider le compilateur
                return (ResponseEntity<?>) ResponseEntity.ok(Collections.emptyList());
            }
            // On cast explicitement pour aider le compilateur
            return (ResponseEntity<?>) ResponseEntity.ok(payments);
        }).exceptionally(ex -> {
            log.error("Error fetching payments for user {}", userId, ex);
            Map<String, Object> errorResponse = Map.of(
                    "error", "Internal server error",
                    "userId", userId,
                    "message", ex.getMessage(),
                    "exceptionType", ex.getClass().getName()
            );
            // On cast explicitement pour aider le compilateur
            return (ResponseEntity<?>) ResponseEntity.status(500).body(errorResponse);
        });
    }

    // ===================== HEALTH =====================
    @GetMapping("/health")
    public Map<String, Object> health(HttpServletRequest request) {
        int port = request.getLocalPort();
        log.info("Health check - Instance port: {}", port);

        return Map.of(
                "service", "payment-service",
                "instancePort", port,
                "status", "UP",
                "timestamp", new Date(),
                "instanceId", getInstanceId(port)
        );
    }

    // Méthode pour générer un ID d'instance unique
    private String getInstanceId(int port) {
        return "payment-service-instance-" + port + "-" + UUID.randomUUID().toString().substring(0, 4);
    }


    @PostMapping("/{paymentId}/confirm")
    public CompletableFuture<ResponseEntity<String>> confirmPayment(
            @PathVariable String paymentId
    ) {
        return commandGateway.send(new ConfirmPaymentCommand(paymentId))
                .thenApply(r -> ResponseEntity.ok("Payment confirmed"))
                .exceptionally(ex -> ResponseEntity.status(400)
                        .body("Failed to confirm payment: " + ex.getMessage()));
    }

    @PostMapping("/{paymentId}/cancel")
    public CompletableFuture<ResponseEntity<String>> cancelPayment(@PathVariable String paymentId) {
        return commandGateway.send(new CancelPaymentCommand(paymentId))
                .thenApply(r -> ResponseEntity.ok("Payment canceled"))
                .exceptionally(ex -> ResponseEntity.status(400)
                        .body("Failed to cancel payment: " + ex.getMessage()));
    }

}