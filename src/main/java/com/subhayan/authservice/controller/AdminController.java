package com.subhayan.authservice.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@SecurityRequirement(name = "Bearer Token")
public class AdminController {

    @GetMapping("/queryUsers")
    public ResponseEntity<String> getAllUsers() {
        return ResponseEntity.ok("All Users for admin");
    }
}
