package com.paymentservice.projection;

import com.paymentservice.event.PaymentCreatedEvent;
import com.paymentservice.model.PaymentModel;
import com.paymentservice.query.FindPaymentsByUserIdQuery;
import com.paymentservice.query.GetAllPaymentsQuery;
import com.paymentservice.query.GetPaymentQuery;
import com.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ProcessingGroup("payments")
public class PaymentProjection {

    private final PaymentRepository paymentRepository;

    @EventHandler
    public void on(PaymentCreatedEvent event) {
        log.info("💾 PROJECTION: Saving payment to database: {}", event.getPaymentId());
        
        PaymentModel payment = new PaymentModel(
            event.getPaymentId(),
            event.getOrderId(),
            event.getTotalAmount(),
            event.getUserId(),
            "PAID",
            event.getQuantity(),
            event.getProductId()
        );
        
        paymentRepository.save(payment);
        log.info("✅ PROJECTION: Payment saved to database");
    }

    @QueryHandler
    public PaymentModel handle(GetPaymentQuery query) {
        log.info("🔍 Query: Get payment by ID: {}", query.getPaymentId());
        return paymentRepository.findById(query.getPaymentId()).orElse(null);
    }

    @QueryHandler
    public List<PaymentModel> handle(GetAllPaymentsQuery query) {
        log.info("🔍 Query: Get all payments");
        return paymentRepository.findAll();
    }

    @QueryHandler
    public List<PaymentModel> handle(FindPaymentsByUserIdQuery query) {
        log.info("🔍 Query: Get payments for user: {}", query.getUserId());
        return paymentRepository.findByUserid(query.getUserId());
    }
}