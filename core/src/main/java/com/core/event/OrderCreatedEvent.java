package com.core.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class OrderCreatedEvent {
    private String orderId;
    private BigDecimal price;
    private Integer number;
    private String productId;
    private String userid;
}
