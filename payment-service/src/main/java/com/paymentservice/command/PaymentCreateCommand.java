package com.paymentservice.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreateCommand {
    @TargetAggregateIdentifier
    private String id;
    private String orderId;
    private BigDecimal price;
    private Integer number;
    private String productId;
    private String userid;
}
