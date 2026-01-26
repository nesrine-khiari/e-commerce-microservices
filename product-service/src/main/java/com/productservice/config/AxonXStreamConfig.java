package com.productservice.config;

import com.core.event.OrderCreatedEvent;
import com.core.event.StockUpdatedEvent;
import com.productservice.command.UpdateStockCommand;
import com.thoughtworks.xstream.XStream;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.xml.XStreamSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration

public class AxonXStreamConfig {
    @Bean
    @Primary
    public Serializer serializer() {
        XStream xStream = new XStream();

        xStream.allowTypesByWildcard(new String[] {
                "com.core.**",           // ⚠️ Vos events et commands partagés
                "com.productservice.**",
                "com.orderservice.**",
                "org.axonframework.**",
                "java.**",
                "javax.**"
        });

        // Explicitly allow your shared classes
        xStream.allowTypes(new Class[] {
                OrderCreatedEvent.class,
                StockUpdatedEvent.class,
                UpdateStockCommand.class
        });

        return XStreamSerializer.builder()
                .xStream(xStream)
                .build();
    }
}
