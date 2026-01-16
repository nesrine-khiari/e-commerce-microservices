package com.productservice.repository;

import com.productservice.model.ProductModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductProjectionRepository extends JpaRepository<ProductModel, String> {
}
