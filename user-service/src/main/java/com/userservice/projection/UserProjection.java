package com.userservice.projection;

import com.core.event.UserCreatedEvent;
import com.userservice.model.UserModel;
import com.userservice.query.GetUsersQuery;
import com.userservice.repository.UserProjectionRepository;
import lombok.RequiredArgsConstructor;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@ProcessingGroup("user-projection")
public class UserProjection {
    private final UserProjectionRepository userProjectionRepository;

    @EventHandler
    public void on(UserCreatedEvent userCreatedEvent) {
        UserModel userModel = new UserModel(
                userCreatedEvent.getId(),
                userCreatedEvent.getName()
        );
        userProjectionRepository.save(userModel);
    }

    @QueryHandler
    public List<UserModel> handle(GetUsersQuery getUsersQuery) {
        return userProjectionRepository.findAll();
    }
}
