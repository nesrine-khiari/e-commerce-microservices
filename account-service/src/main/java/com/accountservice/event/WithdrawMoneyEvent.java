package com.accountservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.serialization.Revision;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Revision("1")
public class WithdrawMoneyEvent {
    private String userid;
    private BigDecimal amount;
    private String timestamp;
}
