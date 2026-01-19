// CE QUE TU DOIS AVOIR dans core/src/main/java/com/core/event/OrderCreatedEvent.java
package com.core.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor  // IMPORTANT !
@AllArgsConstructor
public class OrderCreatedEvent {
    private String orderId;
    private BigDecimal price;
    private Integer number;      // ← DOIT AVOIR CE CHAMP !
    private String productId;
    private String userid;       // Note: "userid" avec "i" minuscule
}