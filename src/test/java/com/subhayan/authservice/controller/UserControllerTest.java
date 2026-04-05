package com.subhayan.authservice.controller;

import com.subhayan.authservice.security.JwtAuthFilter;
import com.subhayan.authservice.security.RateLimiterFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = UserController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {RateLimiterFilter.class, JwtAuthFilter.class})
})
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "USER")
    void getUserDetail_withUserRole_returns200() throws Exception {
        mockMvc.perform(get("/api/user/userDetail"))
                .andExpect(status().isOk())
                .andExpect(content().string("This is you, User"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserDetail_withAdminRole_returns403() throws Exception {
        mockMvc.perform(get("/api/user/userDetail"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserDetail_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(get("/api/user/userDetail"))
                .andExpect(status().isUnauthorized());
    }
}
