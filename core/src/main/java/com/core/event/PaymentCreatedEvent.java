package com.core.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreatedEvent {

    private String paymentId;
    private String orderId;
    private BigDecimal totalAmount;
    private String userid;
    private Integer number;
    private String productId;
}