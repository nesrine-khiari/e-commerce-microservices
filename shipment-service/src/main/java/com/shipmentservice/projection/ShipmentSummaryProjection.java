package com.shipmentservice.projection;

import com.shipmentservice.command.CreateShipmentCommand;
import com.shipmentservice.model.ShipmentModel;
import com.shipmentservice.query.FindShipmentsQuery;
import com.shipmentservice.repository.ShipmentSummaryRepository;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.Timestamp;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class ShipmentSummaryProjection {

    private final ShipmentSummaryRepository shipmentSummaryRepository;

    public ShipmentSummaryProjection(ShipmentSummaryRepository shipmentSummaryRepository) {
        this.shipmentSummaryRepository = shipmentSummaryRepository;
    }

    @EventHandler
    public void on(CreateShipmentCommand event, @Timestamp Instant issuedAt) {
        shipmentSummaryRepository.save(new ShipmentModel(event.getId(), event.getProductId(), event.getProductCount(), event.getAddress(), event.getCargoFirm(), issuedAt));
    }

    @QueryHandler
    public List<ShipmentModel> handle(FindShipmentsQuery query) {
        return shipmentSummaryRepository.findAll(
                PageRequest.of(query.getOffset(), query.getLimit(), Sort.by("cardId").ascending())
        ).getContent();
    }
}
