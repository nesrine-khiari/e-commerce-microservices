package com.paymentservice.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;

@FeignClient(
        name = "order-service",
        path = "/api/orders"
)
public interface OrderServiceClient {

    @GetMapping("/{orderId}/exists")
    Boolean checkOrderExists(@PathVariable String orderId);

    @GetMapping("/{orderId}/price")
    BigDecimal getOrderPrice(@PathVariable String orderId);

    @GetMapping("/{orderId}")
    OrderResponse getOrderDetails(@PathVariable String orderId);

    record OrderResponse(
            String id,
            @JsonProperty("productId") Integer productId,
            Integer number,
            BigDecimal price
    ) {}
}
