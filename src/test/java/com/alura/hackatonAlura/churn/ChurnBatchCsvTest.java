package com.alura.hackatonAlura.churn;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = ChurnController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChurnBatchCsvTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChurnService churnService;

    // Mock security util if present in context
    @MockBean
    private com.alura.hackatonAlura.security.JwtUtil jwtUtil;

    @Test
    void uploadValidCsvReturnsAggregates() throws Exception {
        String csv = "tiempo_contrato_meses,retrasos_pago,uso_mensual,plan\n" +
                "12,2,14.5,Premium\n" +
                "6,0,8.0,Basic\n";
        MockMultipartFile file = new MockMultipartFile(
                "file", "sample.csv", "text/csv", csv.getBytes()
        );

        given(churnService.predict(any(ChurnRequest.class)))
                .willReturn(new ChurnPredictionResponse("Va a cancelar", 0.76, java.util.List.of("retrasos_pago","plan","tiempo_contrato_meses"), java.time.Instant.now()));

        mockMvc.perform(multipart("/api/churn/predict/batch/csv")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.cancelaciones").value(2))
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void uploadInvalidCsvReturns400() throws Exception {
        String badCsv = "tiempo_contrato_meses,retrasos_pago,uso_mensual,plan\n" +
                "x,2,14.5,Premium\n"; // invalid integer
        MockMultipartFile file = new MockMultipartFile(
                "file", "bad.csv", "text/csv", badCsv.getBytes()
        );

        mockMvc.perform(multipart("/api/churn/predict/batch/csv")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }
}