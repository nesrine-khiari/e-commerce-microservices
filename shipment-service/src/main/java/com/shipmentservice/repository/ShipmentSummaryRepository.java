package com.shipmentservice.repository;

import com.shipmentservice.model.ShipmentModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentSummaryRepository extends JpaRepository<ShipmentModel, String> {

}
