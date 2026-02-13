package com.subhayan.authservice.controller;

import com.subhayan.authservice.dto.DtoAuthRegister.*;
import com.subhayan.authservice.service.UserRegister;
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
    public ResponseEntity<UserRegisterResponseDTO> registerUser(@RequestBody UserRegisterRequestDTO userRegisterRequestDTO) {
        UserRegisterResponseDTO userRegisterCreated = userRegister.registerUser(userRegisterRequestDTO);
        return ResponseEntity.ok(userRegisterCreated);
    }
}
