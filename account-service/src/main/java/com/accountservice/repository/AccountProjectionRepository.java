package com.accountservice.repository;

import com.accountservice.model.AccountModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountProjectionRepository extends JpaRepository<AccountModel, String> {
    Optional<AccountModel> findByUserid(String id);
}
