package com.subhayan.authservice.controller;


import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@SecurityRequirement(name = "Bearer Token")
public class UserController {

    @GetMapping("/userDetail")
    public ResponseEntity<String> getUser() {
        return ResponseEntity.ok("This is you, User");
    }
}
