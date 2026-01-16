package com.userservice.repository;

import com.userservice.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProjectionRepository extends JpaRepository<UserModel, String> {
}
