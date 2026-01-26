package com.productservice.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        name = "order-service",
        url = "${order.service.url}"
)
public interface OrderServiceClient {
    /**
     * Notifie Order Service qu'un produit est en rupture de stock
     */
    @PostMapping("/api/orders/products/{productId}/out-of-stock")
    void notifyOutOfStock(@PathVariable("productId") String productId);
}
