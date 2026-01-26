package com.paymentservice.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmPaymentCommand {

    @TargetAggregateIdentifier
    private String paymentId;
}
