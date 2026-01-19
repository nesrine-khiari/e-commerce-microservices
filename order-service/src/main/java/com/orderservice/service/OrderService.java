package com.orderservice.service;

import com.orderservice.client.ProductEndpoint;
import com.orderservice.command.CreateOrderCommand;
import com.orderservice.dto.OrderDto;
import com.orderservice.dto.ProductDto;
import com.orderservice.model.OrderModel;
import com.orderservice.query.GetOrdersQuery;
import com.orderservice.repository.OrderProjectionRepository;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;
    private final ProductEndpoint productEndpoint;
    private final OrderProjectionRepository orderRepository; // AJOUTEZ CE REPOSITORY

    public void create(OrderDto orderDto) throws Exception {
        ProductDto productDto = productEndpoint.getById(orderDto.getProductid());
        Optional.ofNullable(productDto).orElseThrow(Exception::new);
        CreateOrderCommand cmd = new CreateOrderCommand(
                UUID.randomUUID().toString(),
                productDto.getPrice(),
                orderDto.getNumber(),
                orderDto.getProductid(),
                orderDto.getUserid()
        );
        commandGateway.send(cmd);
    }

    public CompletableFuture<List<OrderModel>> getAll() {
        return queryGateway.query(new GetOrdersQuery(), ResponseTypes.multipleInstancesOf(OrderModel.class));
    }
    
    // NOUVELLES MÉTHODES POUR LA COMMUNICATION SYNCHRONE
    
    public boolean orderExists(String orderId) {
        return orderRepository.existsById(orderId);
    }
    
    public Optional<BigDecimal> getOrderPrice(String orderId) {
        return orderRepository.findById(orderId)
                .map(OrderModel::getPrice);
    }
    
    public Optional<OrderModel> getOrderDetails(String orderId) {
        return orderRepository.findById(orderId);
    }
    
    public boolean updateOrderPaymentStatus(String orderId, String paymentId) {
        Optional<OrderModel> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            OrderModel order = optionalOrder.get();
            // Vous pourriez ajouter un champ status dans OrderModel si nécessaire
            // order.setStatus("PAID");
            // order.setPaymentId(paymentId);
            orderRepository.save(order);
            return true;
        }
        return false;
    }
}