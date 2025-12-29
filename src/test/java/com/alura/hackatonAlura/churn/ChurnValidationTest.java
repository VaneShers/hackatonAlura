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
        Map<String, Object> payload = Map.of(
                "tiempo_contrato_meses", -1,
                "retrasos_pago", -2,
                "uso_mensual", -3.5,
                "plan", "Gold"
        );
        mockMvc.perform(post("/api/churn/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.tiempoContratoMeses").exists())
                .andExpect(jsonPath("$.errors.retrasosPago").exists())
                .andExpect(jsonPath("$.errors.usoMensual").exists())
                .andExpect(jsonPath("$.errors.plan").value("Valor inválido: use Basic/Standard/Premium"));
    }

        @Test
        void whenMissingPlan_thenReturns400WithPlanMessage() throws Exception {
        Map<String, Object> payload = Map.of(
            "tiempo_contrato_meses", 12,
            "retrasos_pago", 2,
            "uso_mensual", 14.5
        );
        mockMvc.perform(post("/api/churn/predict")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.plan").value("Debe ser un texto no vacío"));
        }
}
