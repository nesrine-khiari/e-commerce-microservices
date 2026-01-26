package com.paymentservice.projection;

import com.paymentservice.event.PaymentCreatedEvent;
import com.paymentservice.event.PaymentCanceledEvent;
import com.paymentservice.event.PaymentConfirmedEvent;
import com.paymentservice.model.PaymentModel;
import com.paymentservice.query.GetAllPaymentsQuery;
import com.paymentservice.query.GetPaymentQuery;
import com.paymentservice.query.FindPaymentsByUserIdQuery;
import com.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ProcessingGroup("payments")
public class PaymentProjection {

    private final PaymentRepository paymentRepository;

    // =========================================
    // Event Handlers
    // =========================================
    @EventHandler
    public void on(PaymentCreatedEvent event) {
        log.info("💾 PROJECTION: Saving payment {}", event.getPaymentId());

        PaymentModel payment = new PaymentModel(
                event.getPaymentId(),
                event.getOrderId(),
                event.getTotalAmount(),
                event.getUserId(),
                "EN ATTENTE",            // status initial
                event.getQuantity(),
                event.getProductId(),
                LocalDateTime.now()       // createdAt ajouté
        );

        paymentRepository.save(payment);
        log.info("✅ Payment {} saved with status EN ATTENTE", event.getPaymentId());
    }

    @EventHandler
    public void on(PaymentConfirmedEvent event) {
        paymentRepository.findById(event.getPaymentId()).ifPresent(payment -> {
            payment.setStatus("PAID");
            paymentRepository.save(payment);
            log.info("✅ Payment {} confirmed and status set to PAID", event.getPaymentId());
        });
    }

    @EventHandler
    public void on(PaymentCanceledEvent event) {
        paymentRepository.findById(event.getPaymentId()).ifPresent(payment -> {
            payment.setStatus("CANCELED");
            paymentRepository.save(payment);
            log.info("⚠️ Payment {} canceled", event.getPaymentId());
        });
    }

    // =========================================
    // Query Handlers
    // =========================================
    @QueryHandler
    public PaymentModel handle(GetPaymentQuery query) {
        return paymentRepository.findById(query.getPaymentId()).orElse(null);
    }

    @QueryHandler
    public List<PaymentModel> handle(GetAllPaymentsQuery query) {
        return paymentRepository.findAll();
    }

    @QueryHandler
    public List<PaymentModel> handle(FindPaymentsByUserIdQuery query) {
        return paymentRepository.findByUserid(query.getUserId());
    }
}
