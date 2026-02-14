package com.subhayan.authservice.controller;

import com.subhayan.authservice.dto.DtoAuthRegister.*;
import com.subhayan.authservice.service.UserRegister;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthRegister {
    private final UserRegister userRegister;

    public AuthRegister(UserRegister userRegister) {
        this.userRegister = userRegister;
    }

    @PostMapping("/register")
    public ResponseEntity<UserRegisterResponseDTO> registerUser(@Valid @RequestBody UserRegisterRequestDTO userRegisterRequestDTO) {
        UserRegisterResponseDTO userRegisterCreated = userRegister.registerUser(userRegisterRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(userRegisterCreated);
    }
}
