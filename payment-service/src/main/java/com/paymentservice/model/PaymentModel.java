package com.paymentservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "payment_model")
@NoArgsConstructor
@AllArgsConstructor
public class PaymentModel {

    @Id
    private String id;

    private String orderId;

    private BigDecimal totalAmount;

    private String userid;

    private String status; // EN ATTENTE, PAID, CANCELED

    private Integer quantity;

    private String productId;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // timestamp de création
}
