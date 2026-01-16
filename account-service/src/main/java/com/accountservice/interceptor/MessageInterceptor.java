package com.accountservice.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.InterceptorChain;
import org.axonframework.messaging.MessageHandlerInterceptor;
import org.axonframework.messaging.unitofwork.UnitOfWork;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MessageInterceptor implements MessageHandlerInterceptor<CommandMessage<?>> {

	@Override
	public Object handle(UnitOfWork<? extends CommandMessage<?>> unitOfWork, InterceptorChain interceptorChain) throws Exception {
		CommandMessage<?> message = unitOfWork.getMessage();
		log.warn(message.getCommandName());
		log.warn(message.getIdentifier());
		log.warn(message.getPayloadType().getName());
		log.warn(message.getMetaData().toString());

		return interceptorChain.proceed();
	}
}
