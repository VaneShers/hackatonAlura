package com.alura.hackatonAlura.churn;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = ChurnController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChurnStatsSliceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChurnService churnService;

    @MockBean
    private com.alura.hackatonAlura.security.JwtUtil jwtUtil;

    @Test
    void getStatsReturnsTotalsAndRate() throws Exception {
        given(churnService.stats()).willReturn(java.util.Map.of(
                "total_evaluados", 5,
                "tasa_churn", 0.4
        ));

        mockMvc.perform(get("/api/churn/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_evaluados").value(5))
                .andExpect(jsonPath("$.tasa_churn").value(0.4));
    }
}