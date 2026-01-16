package com.productservice.controller;

import com.productservice.command.CreateProductCommand;
import com.productservice.dto.ProductDto;
import com.productservice.model.ProductModel;
import com.productservice.query.FindProductByIdQuery;
import com.productservice.query.GetProductsQuery;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;

    @PostMapping
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
    public CompletableFuture<List<ProductModel>> getProducts(){
        return queryGateway.query(new GetProductsQuery(), ResponseTypes.multipleInstancesOf(ProductModel.class));
    }

    @GetMapping("{id}")
    public ResponseEntity<ProductModel> findByAggregateId(@PathVariable("id") String id) {
        return queryGateway.query(new FindProductByIdQuery(id), ResponseTypes.optionalInstanceOf(ProductModel.class))
                .join()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
