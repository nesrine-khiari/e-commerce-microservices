package com.orderservice.repository;

import com.orderservice.model.OrderModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderProjectionRepository extends JpaRepository<OrderModel, UUID> {
}
