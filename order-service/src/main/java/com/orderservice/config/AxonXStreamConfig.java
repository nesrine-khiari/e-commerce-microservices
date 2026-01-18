package com.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import com.thoughtworks.xstream.XStream;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.xml.XStreamSerializer;

@Configuration
public class AxonXStreamConfig {
    
    @Bean
    @Primary  // <-- Ajoutez cette annotation
    public Serializer serializer() {
        XStream xStream = new XStream();
        xStream.allowTypesByWildcard(new String[] {
            "com.core.**",
            "com.orderservice.**",
            "com.paymentservice.**",
            "org.axonframework.**",
            "java.**",
            "javax.**",
            "sun.**",
            "com.sun.**",
            "[*]"
        });
        
        return XStreamSerializer.builder()
                .xStream(xStream)
                .build();
    }
}