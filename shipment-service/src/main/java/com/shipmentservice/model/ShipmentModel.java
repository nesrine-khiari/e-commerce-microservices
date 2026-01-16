package com.shipmentservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentModel {
    @Id
    private String id;
    private String productId;
    private Integer productCount;
    private String address;
    private String cargoFirm;
    private Instant time;
}
