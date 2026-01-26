package com.paymentservice.service;

import com.paymentservice.command.CancelPaymentCommand;
import com.paymentservice.model.PaymentModel;
import com.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentTimeoutService {

    private final PaymentRepository paymentRepository;
    private final CommandGateway commandGateway;

    @Scheduled(fixedRate = 60000) // toutes les 60 secondes
    public void cancelExpiredPayments() {
        LocalDateTime now = LocalDateTime.now();

        paymentRepository.findAll().stream()
                .filter(p -> "EN ATTENTE".equals(p.getStatus()))
                .filter(p -> p.getCreatedAt() != null && p.getCreatedAt().isBefore(now.minusMinutes(2))) // timeout 2 min
                .forEach(p -> {
                    try {
                        commandGateway.send(new CancelPaymentCommand(p.getId()));
                        log.info("Payment {} canceled due to timeout", p.getId());
                    } catch (Exception e) {
                        log.error("Failed to cancel payment {}: {}", p.getId(), e.getMessage(), e);
                    }
                });
    }
}
