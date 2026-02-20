package com.subhayan.authservice.controller;

import com.subhayan.authservice.dto.UserDetailsResponse;
import com.subhayan.authservice.service.AdminService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@SecurityRequirement(name = "Bearer Token")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/queryUsers")
    public ResponseEntity<String> getAllUsers() {
        return ResponseEntity.ok("All Users for admin");
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserDetailsResponse> adminGetUserById(@PathVariable UUID userId) {
        return ResponseEntity.ok(adminService.getUserDetailsById(userId));
    }
}
