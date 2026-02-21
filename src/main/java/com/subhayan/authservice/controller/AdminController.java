package com.subhayan.authservice.controller;

import com.subhayan.authservice.dto.AdminUpdateRequest;
import com.subhayan.authservice.dto.PagedUserResponse;
import com.subhayan.authservice.dto.UserDetailsResponse;
import com.subhayan.authservice.entity.Role;
import com.subhayan.authservice.service.AdminService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@SecurityRequirement(name = "Bearer Token")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/user/query")
    public ResponseEntity<PagedUserResponse> queryUsers(
            @RequestParam(required = false) Role role, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(adminService.queryUsers(role, page, pageSize));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserDetailsResponse> adminGetUserById(@PathVariable UUID userId) {
        return ResponseEntity.ok(adminService.getUserDetailsById(userId));
    }

    @PatchMapping("/user/{userId}")
    public ResponseEntity<UserDetailsResponse> updateUser(@PathVariable UUID userId, @Valid @RequestBody AdminUpdateRequest adminUpdateRequest) {
        return ResponseEntity.ok(adminService.updateUser(userId, adminUpdateRequest));
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
