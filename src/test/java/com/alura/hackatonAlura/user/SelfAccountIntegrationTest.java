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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SelfAccountIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void userCanViewAndUpdateOwnEmailAndPassword() throws Exception {
        // Register user
        Map<String, Object> register = Map.of(
                "email", "self@test.com",
                "password", "Secret123!",
                "fullName", "Self User"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        // Login
        Map<String, Object> login = Map.of(
                "email", "self@test.com",
                "password", "Secret123!"
        );
        String loginBody = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(loginBody).get("token").asText();

        // GET /api/me
        mockMvc.perform(get("/api/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("self@test.com"))
                .andExpect(jsonPath("$.roles").value("USER"));

        // Update email
        Map<String, Object> updateEmail = Map.of("email", "selfnew@test.com");
        mockMvc.perform(put("/api/me/email")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEmail)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("selfnew@test.com"));

        // Re-login with new email
        Map<String, Object> loginNewEmail = Map.of(
                "email", "selfnew@test.com",
                "password", "Secret123!"
        );
        String loginBody2 = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginNewEmail)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn().getResponse().getContentAsString();
        String token2 = objectMapper.readTree(loginBody2).get("token").asText();

        // Update password
        Map<String, Object> updatePwd = Map.of("password", "NewSecret123!");
        mockMvc.perform(put("/api/me/password")
                        .header("Authorization", "Bearer " + token2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePwd)))
                .andExpect(status().isNoContent());

        // Login with new password
        Map<String, Object> loginNewPwd = Map.of(
                "email", "selfnew@test.com",
                "password", "NewSecret123!"
        );
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginNewPwd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }
}
