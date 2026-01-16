package com.userservice.configuration;

import org.axonframework.messaging.Message;
import org.axonframework.messaging.interceptors.LoggingInterceptor;
import org.axonframework.queryhandling.QueryBus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventProcessorConfig {

    /**
     * Configure logging interceptor for QueryBus
     * This is called after all beans are created, avoiding circular dependency
     */
    @Autowired
    public void configureLoggingInterceptorFor(QueryBus queryBus) {
        // Create the interceptor inline instead of as a separate bean
        LoggingInterceptor<Message<?>> loggingInterceptor = new LoggingInterceptor<>();

        queryBus.registerDispatchInterceptor(loggingInterceptor);
        queryBus.registerHandlerInterceptor(loggingInterceptor);
    }
}