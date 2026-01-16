package com.orderservice.command;

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
public class CreateOrderCommand {

    @TargetAggregateIdentifier
    private String orderId;
    private BigDecimal price;
    private Integer number;
    private String productId;
    private String userid;

}