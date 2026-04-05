package com.subhayan.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.subhayan.authservice.dto.AdminUpdateRequest;
import com.subhayan.authservice.dto.PagedUserResponse;
import com.subhayan.authservice.dto.UserDetailsResponse;
import com.subhayan.authservice.entity.Role;
import com.subhayan.authservice.entity.Salutation;
import com.subhayan.authservice.security.JwtAuthFilter;
import com.subhayan.authservice.security.RateLimiterFilter;
import com.subhayan.authservice.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AdminController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {RateLimiterFilter.class, JwtAuthFilter.class})
})
@Import(TestSecurityConfig.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService adminService;

    private ObjectMapper objectMapper;
    private UUID userId;
    private UserDetailsResponse userDetailsResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        userId = UUID.randomUUID();
        userDetailsResponse = new UserDetailsResponse(
                userId, Salutation.MR, "John", "Doe",
                "john@example.com", "+1234567890",
                LocalDate.of(1990, 1, 1), Role.USER,
                LocalDateTime.of(2024, 1, 1, 0, 0)
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void queryUsers_withAdminRole_returns200() throws Exception {
        PagedUserResponse pagedResponse = new PagedUserResponse(
                List.of(userDetailsResponse), 0, 10, 1
        );

        when(adminService.queryUsers(isNull(), eq(0), eq(10))).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/admin/user/query")
                        .param("page", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").isArray())
                .andExpect(jsonPath("$.users[0].email").value("john@example.com"))
                .andExpect(jsonPath("$.totalUsers").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void queryUsers_withUserRole_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/user/query"))
                .andExpect(status().isForbidden());
    }

    @Test
    void queryUsers_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/user/query"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_withAdminRole_returns200() throws Exception {
        when(adminService.getUserDetailsById(userId)).thenReturn(userDetailsResponse);

        mockMvc.perform(get("/api/admin/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_withNonExistentUser_returns500() throws Exception {
        UUID unknownId = UUID.randomUUID();
        when(adminService.getUserDetailsById(unknownId))
                .thenThrow(new RuntimeException("User with id " + unknownId + " not found"));

        mockMvc.perform(get("/api/admin/user/{userId}", unknownId))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_withAdminRole_returns200() throws Exception {
        AdminUpdateRequest updateRequest = new AdminUpdateRequest(
                null, "UpdatedName", null, null, null, null, null
        );

        UserDetailsResponse updatedResponse = new UserDetailsResponse(
                userId, Salutation.MR, "UpdatedName", "Doe",
                "john@example.com", "+1234567890",
                LocalDate.of(1990, 1, 1), Role.USER,
                LocalDateTime.of(2024, 1, 1, 0, 0)
        );

        when(adminService.updateUser(eq(userId), any(AdminUpdateRequest.class))).thenReturn(updatedResponse);

        mockMvc.perform(patch("/api/admin/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("UpdatedName"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_withAdminRole_returns204() throws Exception {
        mockMvc.perform(delete("/api/admin/user/{userId}", userId))
                .andExpect(status().isNoContent());

        verify(adminService).deleteUser(userId);
    }
}
