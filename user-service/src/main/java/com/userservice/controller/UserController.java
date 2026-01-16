package com.userservice.controller;


import com.userservice.dto.DepositDTO;
import com.userservice.dto.UserDto;
import com.userservice.model.UserModel;
import com.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
public class UserController {


    private final UserService userService;

    @PostMapping
    public void handle(@RequestBody UserDto userDto) {
        userService.create(userDto);
    }

    @GetMapping
    public CompletableFuture<List<UserModel>> getAll() {
        return userService.getAll();
    }

    @PostMapping("/deposit")
    public void deposit(@RequestBody DepositDTO transactionDTO) {
        userService.depositMoney(transactionDTO);
    }
}
