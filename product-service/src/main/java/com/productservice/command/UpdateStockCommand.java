package com.productservice.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.RoutingKey;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStockCommand {
    @RoutingKey
    private String productid;
    private Integer number;
}