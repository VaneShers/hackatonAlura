package com.alura.hackatonAlura.churn;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = ChurnController.class)
class ChurnControllerTest {

    @Autowired
    private MockMvc mockMvc;

        @MockBean
        private ChurnService churnService;

        // Mock JwtUtil to satisfy security filter dependencies in the test context
    @MockBean
    private com.alura.hackatonAlura.security.JwtUtil jwtUtil;

    @Test
    void predictEndpointAcceptsValidPayload() throws Exception {
        given(churnService.predict(any(ChurnRequest.class)))
                .willReturn(new ChurnPredictionResponse("Va a continuar", 0.23, List.of(), Instant.now()));

        String body = "{" +
            "\"gender\":\"Female\"," +
            "\"SeniorCitizen\":0," +
            "\"Partner\":\"Yes\"," +
            "\"Dependents\":\"No\"," +
            "\"tenure\":24," +
            "\"PhoneService\":\"Yes\"," +
            "\"MultipleLines\":\"No\"," +
            "\"InternetService\":\"DSL\"," +
            "\"OnlineSecurity\":\"Yes\"," +
            "\"OnlineBackup\":\"No\"," +
            "\"DeviceProtection\":\"No\"," +
            "\"TechSupport\":\"No\"," +
            "\"StreamingTV\":\"No\"," +
            "\"StreamingMovies\":\"No\"," +
            "\"Contract\":\"One year\"," +
            "\"PaperlessBilling\":\"Yes\"," +
            "\"PaymentMethod\":\"Electronic check\"," +
            "\"MonthlyCharges\":29.85," +
            "\"TotalCharges\":1889.50" +
            "}";

        mockMvc.perform(post("/api/churn/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }
}
