package com.productservice.aggregate;

import com.core.event.StockUpdatedEvent;
import com.productservice.command.CreateProductCommand;
import com.productservice.command.UpdateStockCommand;
import com.productservice.event.ProductCreatedEvent;
import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import java.math.BigDecimal;
import java.util.UUID;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate
@NoArgsConstructor
public class Product {
    @AggregateIdentifier
    private String id;
    private String productid;
    private BigDecimal price;
    private Integer stock;
    private String name;
    private String description;

    @CommandHandler
    public Product(CreateProductCommand adProductCommand) {
        apply(new ProductCreatedEvent(
                adProductCommand.getId(),
                adProductCommand.getPrice(),
                adProductCommand.getStock(),
                adProductCommand.getName(),
                adProductCommand.getDescription()
        ));
    }

    @CommandHandler
    public void handle(UpdateStockCommand command) {
        System.out.println("📉 Updating stock for product: {}");
        System.out.println( command.getProductid());

        if (this.stock < command.getNumber()) {
            throw new IllegalStateException("Stock insuffisant!");
        }

        apply(new StockUpdatedEvent(
                command.getProductid(),
                command.getNumber()
        ));
    }

    @EventSourcingHandler
    public void on(ProductCreatedEvent evt) {
        System.out.println("✅ Product created event: {}");
        System.out.println( evt.getId());
        this.id = evt.getId();
        this.price = evt.getPrice();
        this.stock = evt.getStock();
        this.name = evt.getName();
        this.description = evt.getDescription();
    }

    @EventSourcingHandler
    public void on(StockUpdatedEvent evt) {
        System.out.println ("✅ Stock updated event for: {}");
        System.out.println(evt.getProductid());
        this.stock = this.stock - evt.getNumber();
    }

}
