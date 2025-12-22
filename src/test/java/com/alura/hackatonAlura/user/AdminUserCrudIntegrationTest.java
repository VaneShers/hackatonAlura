package com.alura.hackatonAlura.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminUserCrudIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private String loginAdminAndGetToken() throws Exception {
        Map<String, Object> login = Map.of(
                "email", "admin@local",
                "password", "Admin123!"
        );
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }

    @Test
    void adminCanCRUDUsers_andSelfDeleteIsBlocked() throws Exception {
        String token = loginAdminAndGetToken();

        // Create a USER
        Map<String, Object> create = Map.of(
                "email", "cruduser@test.com",
                "password", "Secret123!",
                "fullName", "Crud User",
                "role", "USER"
        );

        String location = mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("^/api/users/\\d+$")))
                .andReturn().getResponse().getHeader("Location");

        long createdId = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

        // Update role to ADMIN
        Map<String, Object> changeRole = Map.of("role", "ADMIN");
        mockMvc.perform(put("/api/admin/users/" + createdId + "/role")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeRole)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles").value("ADMIN"));

        // Fetch my own profile to get admin id
        String meBody = mockMvc.perform(get("/api/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long adminId = objectMapper.readTree(meBody).get("id").asLong();

        // Attempt to delete my own admin account -> 400
        mockMvc.perform(delete("/api/admin/users/" + adminId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Admins cannot delete their own account"));

        // Delete the created user -> 204
        mockMvc.perform(delete("/api/admin/users/" + createdId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // Confirm deleted -> 404
        mockMvc.perform(get("/api/users/" + createdId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
