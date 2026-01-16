package com.productservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Data
@Table(name = "PRODUCT_MODEL")
@NoArgsConstructor
@AllArgsConstructor
public class ProductModel {

    @Id
    private String id;
    private BigDecimal price;
    private Integer stock;
    private String name;
    private String description;
}

