package com.subhayan.authservice.controller;

import com.subhayan.authservice.dto.DtoAuthLogin.*;
import com.subhayan.authservice.service.UserLogin;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthLogin {
    private final UserLogin userLogin;

    public AuthLogin(UserLogin userLogin) {
        this.userLogin = userLogin;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = userLogin.loginUser(loginRequest);
        return ResponseEntity.ok(authResponse);
    }
}
