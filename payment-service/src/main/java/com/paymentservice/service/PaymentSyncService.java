package com.paymentservice.service;

import com.paymentservice.client.OrderServiceClient;
import com.paymentservice.command.PaymentCreateCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentSyncService {

    private final OrderValidationService orderValidationService;
    private final CommandGateway commandGateway;

    public CompletableFuture<ResponseEntity<Map<String, Object>>> createPaymentWithSyncValidation(
            String orderId,
            String userId,
            Integer quantity
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1️⃣ Vérifier l'existence
                if (!orderValidationService.checkOrderExists(orderId)) {
                    log.error("Order not found: {}", orderId);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", "Order not found"));
                }

                // 2️⃣ Récupérer les infos
                BigDecimal price = orderValidationService.getOrderPrice(orderId);
                OrderServiceClient.OrderResponse details = orderValidationService.getOrderDetails(orderId);

                if (details == null || details.productId() == null) {
                    log.error("Invalid order data for order: {}", orderId);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("error", "Invalid order data"));
                }

                // 3️⃣ Créer le paiement
                String paymentId = "PAY-SYNC-" + UUID.randomUUID().toString().substring(0, 8);

                PaymentCreateCommand command = PaymentCreateCommand.builder()
                        .paymentId(paymentId)
                        .orderId(orderId)
                        .productId(String.valueOf(details.productId()))
                        .price(price)
                        .quantity(quantity)
                        .userId(userId)
                        .build();

                commandGateway.sendAndWait(command);

                return ResponseEntity.ok(
                        Map.of(
                                "status", "SUCCESS",
                                "paymentId", paymentId
                        )
                );

            } catch (Exception ex) {
                log.error("Payment creation failed for order {}: {}", orderId, ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of(
                                "error", "Order service unavailable",
                                "details", ex.getMessage()
                        ));
            }
        });
    }
}
