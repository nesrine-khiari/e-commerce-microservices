package com.userservice.service;

import com.core.event.UserDepositMoneyEvent;
import com.userservice.command.CreateUserCommand;
import com.userservice.dto.UserDto;
import com.userservice.dto.DepositDTO;
import com.userservice.model.UserModel;
import com.userservice.query.GetUsersQuery;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


@Service
@RequiredArgsConstructor
public class UserService {
    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;
    private final EventGateway eventGateway;

    public void depositMoney(DepositDTO transactionDTO) {
        eventGateway.publish(new UserDepositMoneyEvent(transactionDTO.getUserid(), transactionDTO.getAmount()));
    }

    public void create(UserDto userDto) {
        CreateUserCommand cmd = new CreateUserCommand(
                UUID.randomUUID().toString(),
                userDto.getName()
        );
        commandGateway.sendAndWait(cmd);
    }

    public CompletableFuture<List<UserModel>> getAll() {
        return queryGateway.query(new GetUsersQuery(), ResponseTypes.multipleInstancesOf(UserModel.class));
    }
}
