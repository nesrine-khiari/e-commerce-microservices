package com.paymentservice.repository;

import com.paymentservice.model.PaymentModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentModel, String> {
    List<PaymentModel> findByUserid(String userid);
}