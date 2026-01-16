package com.accountservice.service;

import lombok.AllArgsConstructor;
import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.TrackingEventProcessor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ReplayService {
    private EventProcessingConfiguration configuration;

    public void replay() {
        configuration.eventProcessor("com.accountservice.projection", TrackingEventProcessor.class).ifPresent(processor -> {
            processor.shutDown();
            processor.resetTokens();
            processor.start();
        });
    }
}
