package com.orderservice.controller;

import com.orderservice.dto.OrderDto;
import com.orderservice.model.OrderModel;
import com.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;


    @PostMapping
    public void handle(@RequestBody OrderDto orderDto) throws Exception {
        orderService.create(orderDto);
    }

    @GetMapping
    public CompletableFuture<List<OrderModel>> getOrders() {
        return orderService.getAll();
    }
}
