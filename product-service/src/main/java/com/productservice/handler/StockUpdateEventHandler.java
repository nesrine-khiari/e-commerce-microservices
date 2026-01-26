package com.productservice.handler;

import com.core.event.OrderCreatedEvent;
import com.productservice.command.UpdateStockCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;
@Component
@ProcessingGroup("product")
@RequiredArgsConstructor
@Slf4j
public class StockUpdateEventHandler {

    private final CommandGateway commandGateway;

    @EventHandler
    public void handle(OrderCreatedEvent event) {
        log.info("🟡 Received OrderCreatedEvent:");
        log.info("   - OrderId: {}", event.getOrderId());
        log.info("   - ProductId: {}", event.getProductId());
        log.info("   - Quantity: {}", event.getNumber());

        try {
            // ⚠️ IMPORTANT: productid doit correspondre à l'ID du Product aggregate
            UpdateStockCommand command = new UpdateStockCommand(
                    event.getProductId(),  // Ceci doit matcher Product.id
                    event.getNumber()
            );

            log.info("📤 Sending UpdateStockCommand for product: {}", event.getProductId());

            commandGateway.send(command)
                    .thenAccept(result -> {
                        log.info("✅ Stock successfully updated for product: {}", event.getProductId());
                    })
                    .exceptionally(ex -> {
                        log.error("❌ Failed to update stock for product {}: {}",
                                event.getProductId(), ex.getMessage());
                        return null;
                    });

        } catch (Exception e) {
            log.error("❌ Error handling OrderCreatedEvent", e);
            // Selon votre logique métier, vous pourriez vouloir :
            // - Relancer l'exception pour retry
            // - Publier un événement d'échec
            // - etc.
        }
    }
}