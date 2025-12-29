package com.alura.hackatonAlura.churn;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
class ChurnPredictIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void postPredictWithValidPayloadReturnsPrediction() throws Exception {
        String payload = "{\n" +
                "  \"tiempo_contrato_meses\": 12,\n" +
                "  \"retrasos_pago\": 2,\n" +
                "  \"uso_mensual\": 14.5,\n" +
                "  \"plan\": \"Premium\"\n" +
                "}";

        mockMvc.perform(post("/api/churn/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prevision", not(emptyOrNullString())))
                .andExpect(jsonPath("$.probabilidad").exists())
                .andExpect(jsonPath("$.probabilidad", notNullValue()))
                .andExpect(jsonPath("$.topFeatures").isArray());
    }
}