package com.alura.hackatonAlura.churn;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import com.alura.hackatonAlura.security.JwtUtil;

@WebMvcTest(controllers = ChurnController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChurnValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChurnService churnService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void whenInvalidFields_thenReturns400WithErrorsMap() throws Exception {
        Map<String, Object> payload = java.util.Map.ofEntries(
            java.util.Map.entry("gender", "male"), // invalid (case-sensitive)
            java.util.Map.entry("SeniorCitizen", -1), // invalid (<0)
            java.util.Map.entry("Partner", "Yes"),
            java.util.Map.entry("Dependents", "No"),
            java.util.Map.entry("tenure", -5), // invalid (<0)
            java.util.Map.entry("PhoneService", "Yes"),
            java.util.Map.entry("MultipleLines", "No"),
            java.util.Map.entry("InternetService", "DSL"),
            java.util.Map.entry("OnlineSecurity", "Yes"),
            java.util.Map.entry("OnlineBackup", "No"),
            java.util.Map.entry("DeviceProtection", "No"),
            java.util.Map.entry("TechSupport", "No"),
            java.util.Map.entry("StreamingTV", "No"),
            java.util.Map.entry("StreamingMovies", "No"),
            java.util.Map.entry("Contract", "One year"),
            java.util.Map.entry("PaperlessBilling", "Yes"),
            java.util.Map.entry("PaymentMethod", "Electronic check"),
            java.util.Map.entry("MonthlyCharges", -3.5), // invalid (<0)
            java.util.Map.entry("TotalCharges", 0.0)
        );
        mockMvc.perform(post("/api/churn/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.gender").exists())
                .andExpect(jsonPath("$.errors.seniorCitizen").exists())
                .andExpect(jsonPath("$.errors.tenure").exists())
                .andExpect(jsonPath("$.errors.monthlyCharges").exists());
    }

    // 'plan' ya no es requerido; se elimina la prueba de validación asociada.
}
