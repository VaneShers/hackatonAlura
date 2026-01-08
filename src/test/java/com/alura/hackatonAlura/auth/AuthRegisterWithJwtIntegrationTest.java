package com.alura.hackatonAlura.auth;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthRegisterWithJwtIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void registerEndpointAcceptsJwtHeaderAndCreatesUser() throws Exception {
        // Register a first user and login to obtain JWT
        Map<String, Object> firstUser = Map.of(
                "email", "jwtseed@test.com",
                "password", "Secret123!",
                "fullName", "Seed User"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstUser)))
                .andExpect(status().isCreated());

        Map<String, Object> login = Map.of(
                "email", "jwtseed@test.com",
                "password", "Secret123!"
        );

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn().getResponse().getContentAsString();

        String jwt = objectMapper.readTree(loginResponse).get("token").asText();

        // Attempt to register a new user while sending Authorization header with Bearer JWT
        Map<String, Object> secondUser = Map.of(
                "email", "newjwt@test.com",
                "password", "Secret123!",
                "fullName", "New Jwt User"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwt)
                        .content(objectMapper.writeValueAsString(secondUser)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("^/api/users/\\d+$")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("newjwt@test.com"))
                .andExpect(jsonPath("$.fullName").value("New Jwt User"))
                .andExpect(jsonPath("$.roles").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }
}
