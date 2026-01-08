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
            "  \"gender\": \"Female\",\n" +
            "  \"SeniorCitizen\": 0,\n" +
            "  \"Partner\": \"Yes\",\n" +
            "  \"Dependents\": \"No\",\n" +
            "  \"tenure\": 24,\n" +
            "  \"PhoneService\": \"Yes\",\n" +
            "  \"MultipleLines\": \"No\",\n" +
            "  \"InternetService\": \"DSL\",\n" +
            "  \"OnlineSecurity\": \"Yes\",\n" +
            "  \"OnlineBackup\": \"No\",\n" +
            "  \"DeviceProtection\": \"No\",\n" +
            "  \"TechSupport\": \"No\",\n" +
            "  \"StreamingTV\": \"No\",\n" +
            "  \"StreamingMovies\": \"No\",\n" +
            "  \"Contract\": \"One year\",\n" +
            "  \"PaperlessBilling\": \"Yes\",\n" +
            "  \"PaymentMethod\": \"Electronic check\",\n" +
            "  \"MonthlyCharges\": 29.85,\n" +
            "  \"TotalCharges\": 1889.50\n" +
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