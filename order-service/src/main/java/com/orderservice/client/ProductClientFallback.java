package com.orderservice.client;

import com.orderservice.dto.ProductDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ProductClientFallback implements ProductEndpoint {

    @Override
    public ProductDto getById(String id) {
        return new ProductDto(
                id,
                BigDecimal.ZERO,   // price
                0,                 // stock
                "Unavailable product",
                "Product service is currently unavailable"
        );
    }
}
