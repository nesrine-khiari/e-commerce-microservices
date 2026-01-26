package com.productservice.aggregate;

import com.core.event.StockUpdatedEvent;
import com.productservice.client.OrderServiceClient;
import com.productservice.command.CreateProductCommand;
import com.productservice.command.UpdateStockCommand;
import com.productservice.event.ProductCreatedEvent;
import jakarta.persistence.Transient;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;
@Aggregate
@NoArgsConstructor
@Slf4j  // Ajoutez Lombok Slf4j pour de meilleurs logs
public class Product {

    @AggregateIdentifier
    private String id;  // ⚠️ C'est l'identifiant principal

    private BigDecimal price;
    private Integer stock;
    private String name;
    private String description;

    @Autowired
    @Transient  // Ne pas persister ce champ
    private transient OrderServiceClient orderServiceClient;

    @CommandHandler
    public Product(CreateProductCommand command) {
        log.info("🔵 Creating product: {}", command.getId());

        apply(new ProductCreatedEvent(
                command.getId(),
                command.getPrice(),
                command.getStock(),
                command.getName(),
                command.getDescription()
        ));
    }

    @CommandHandler
    public void handle(UpdateStockCommand command) {
        log.info("📉 Updating stock for product: {}", command.getProductid());
        log.info("   - Current stock: {}", this.stock);
        log.info("   - Requested quantity: {}", command.getNumber());

        // ⚠️ Vérification importante
        if (this.stock == null) {
            log.error("❌ Product stock is null - Product not properly initialized!");
            throw new IllegalStateException("Product not found or not initialized: " + command.getProductid());
        }

        if (this.stock < command.getNumber()) {
            log.error("❌ Insufficient stock! Available: {}, Requested: {}",
                    this.stock, command.getNumber());
            throw new IllegalStateException(
                    String.format("Stock insuffisant! Disponible: %d, Demandé: %d",
                            this.stock, command.getNumber())
            );
        }

        log.info("✅ Stock check passed, applying StockUpdatedEvent");

        apply(new StockUpdatedEvent(
                command.getProductid(),
                command.getNumber()
        ));
    }

    @EventSourcingHandler
    public void on(ProductCreatedEvent evt) {
        log.info("✅ Applying ProductCreatedEvent: {}", evt.getId());
        this.id = evt.getId();
        this.price = evt.getPrice();
        this.stock = evt.getStock();
        this.name = evt.getName();
        this.description = evt.getDescription();
        log.info("   - Initial stock set to: {}", this.stock);
    }

    @EventSourcingHandler
    public void on(StockUpdatedEvent evt) {
        log.info("✅ Applying StockUpdatedEvent for product: {}", evt.getProductid());
        log.info("   - Stock before: {}", this.stock);
        log.info("   - Quantity to reduce: {}", evt.getNumber());

        this.stock = this.stock - evt.getNumber();

        log.info("   - Stock after: {}", this.stock);

        // ⭐ NOTIFICATION DE RUPTURE DE STOCK (AJOUT MINIMAL)
        if (this.stock <= 0) {
            log.warn("⚠️ RUPTURE DE STOCK détectée pour le produit: {}", evt.getProductid());

            try {
                if (orderServiceClient != null) {
                    orderServiceClient.notifyOutOfStock(evt.getProductid());
                    log.info("✅ Order Service notifié de la rupture de stock");
                } else {
                    log.warn("⚠️ OrderServiceClient non disponible - notification ignorée");
                }
            } catch (Exception e) {
                log.error("❌ Échec de notification à Order Service: {}", e.getMessage());
                // On ne bloque pas le processus si la notification échoue
            }
        }
    }
}