package com.productservice.controller;

import com.productservice.command.CreateProductCommand;
import com.productservice.dto.ProductDto;
import com.productservice.model.ProductModel;
import com.productservice.query.FindProductByIdQuery;
import com.productservice.query.GetProductsQuery;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@Slf4j

public class ProductController {

    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;

    @PostMapping
    @CircuitBreaker(name = "productCommand", fallbackMethod = "createProductFallback")
    @Retry(name = "productCommand")
    @RateLimiter(name = "createProduct")
    public void handle(@RequestBody ProductDto productDto){
        CreateProductCommand cmd = new CreateProductCommand(
                UUID.randomUUID().toString(),
                productDto.getPrice(),
                productDto.getStock(),
                productDto.getName(),
                productDto.getDescription()
        );
        commandGateway.sendAndWait(cmd);
    }

    @GetMapping
    @CircuitBreaker(name = "productQuery", fallbackMethod = "getProductsFallback")
    @Retry(name = "productQuery")
    @RateLimiter(name = "productQuery")
    public CompletableFuture<List<ProductModel>> getProducts(){
        return queryGateway.query(new GetProductsQuery(), ResponseTypes.multipleInstancesOf(ProductModel.class));
    }

    @GetMapping("{id}")
    @CircuitBreaker(name = "productQuery", fallbackMethod = "findByIdFallback")
    @Retry(name = "productQuery")
    @RateLimiter(name = "productQuery")
    public ResponseEntity<ProductModel> findByAggregateId(@PathVariable("id") String id) {
        return queryGateway.query(new FindProductByIdQuery(id), ResponseTypes.optionalInstanceOf(ProductModel.class))
                .join()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    // ========== MÉTHODES FALLBACK ==========

    /**
     * Fallback pour createProduct
     * IMPORTANT: La signature doit correspondre exactement + Exception en dernier paramètre
     */
    public ResponseEntity<String> createProductFallback(ProductDto productDto, Exception ex) {
        System.out .println("FALLBACK createProduct: Cannot create product '{}'. Reason: {}");
        System.out.println( ex.getMessage());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Service de création temporairement indisponible. Réessayez plus tard.");
    }

    /**
     * Fallback pour getProducts
     */
    public CompletableFuture<ResponseEntity<List<ProductModel>>> getProductsFallback(Exception ex) {
      System.out.print("FALLBACK getProducts: Cannot fetch products. Reason: {}");
      System.out.println( ex.getMessage());

        // Retourner une liste vide au lieu d'une erreur
        return CompletableFuture.completedFuture(
                ResponseEntity.ok(new ArrayList<>())
        );
    }

    /**
     * Fallback pour findByAggregateId
     */
    public CompletableFuture<ResponseEntity<ProductModel>> findByIdFallback(String id, Exception ex) {
        System.out.println("FALLBACK findById: Cannot fetch product '{}'. Reason: {}");
        System.out.println( ex.getMessage());

        return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
        );
    }
    @GetMapping("/test-error")
    @CircuitBreaker(name = "productQuery", fallbackMethod = "getProductsFallback")
    @Retry(name = "productQuery")
    @RateLimiter(name = "productQuery")
    public CompletableFuture<List<ProductModel>> getProductsWithError() {
        return CompletableFuture.supplyAsync(() -> {
            // Simulation d'une erreur à chaque appel
            throw new RuntimeException("Simulated service failure for testing circuit breaker");
        });
    }

    // Méthode fallback
    public String getProductsFallback(Throwable t) {
        System.out.println("⚠ Circuit breaker triggered! Reason: " + t.getMessage());
        return("⚠ Circuit breaker triggered!");
    }
}
