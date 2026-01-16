package com.accountservice.configuration;

import com.accountservice.interceptor.MessageInterceptor;
import org.axonframework.commandhandling.CommandBus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventProcessorConfig {

    @Autowired
    public void configMessageInterceptor(CommandBus commandBus, MessageInterceptor messageInterceptor) {
        commandBus.registerHandlerInterceptor(messageInterceptor);
    }
}
