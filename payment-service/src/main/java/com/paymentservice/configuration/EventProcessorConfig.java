package com.paymentservice.configuration;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.interceptors.LoggingInterceptor;
import org.axonframework.queryhandling.QueryBus;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventProcessorConfig {

    /**
     * Bean unique du LoggingInterceptor utilisé pour tous les bus.
     */
    @Bean
    public LoggingInterceptor<Message<?>> loggingInterceptor() {
        return new LoggingInterceptor<>();
    }

    /**
     * ApplicationRunner qui s'exécute après le démarrage de Spring
     * pour enregistrer le LoggingInterceptor sur tous les bus.
     */
    @Bean
    public ApplicationRunner registerLoggingInterceptors(
            CommandBus commandBus,
            EventBus eventBus,
            QueryBus queryBus,
            EventProcessingConfigurer eventProcessingConfigurer,
            LoggingInterceptor<Message<?>> loggingInterceptor
    ) {
        return args -> {
            // CommandBus
            commandBus.registerDispatchInterceptor(loggingInterceptor);
            commandBus.registerHandlerInterceptor(loggingInterceptor);

            // EventBus
            eventBus.registerDispatchInterceptor(loggingInterceptor);

            // QueryBus
            queryBus.registerDispatchInterceptor(loggingInterceptor);
            queryBus.registerHandlerInterceptor(loggingInterceptor);

            // EventProcessingConfigurer (pour tous les processors)
            eventProcessingConfigurer.registerDefaultHandlerInterceptor((config, processorName) -> loggingInterceptor);
        };
    }
}
