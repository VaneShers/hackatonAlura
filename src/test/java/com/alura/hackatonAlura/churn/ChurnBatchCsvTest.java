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
                String csv = "gender,SeniorCitizen,Partner,Dependents,tenure,PhoneService,MultipleLines,InternetService,OnlineSecurity,OnlineBackup,DeviceProtection,TechSupport,StreamingTV,StreamingMovies,Contract,PaperlessBilling,PaymentMethod,MonthlyCharges,TotalCharges\n" +
                                "Female,0,Yes,No,24,Yes,No,DSL,Yes,No,No,No,No,No,One year,Yes,Electronic check,29.85,1889.50\n" +
                                "Male,1,No,No,2,Yes,Yes,Fiber optic,No,No,No,No,Yes,Yes,Month-to-month,Yes,Electronic check,75.50,\n";
        MockMultipartFile file = new MockMultipartFile(
                "file", "sample.csv", "text/csv", csv.getBytes()
        );

        given(churnService.predict(any(ChurnRequest.class)))
                .willReturn(new ChurnPredictionResponse("Va a cancelar", 0.76, java.util.List.of("Contract","tenure","OnlineSecurity"), java.time.Instant.now()));

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
                String badCsv = "gender,SeniorCitizen,Partner,Dependents,tenure,PhoneService,MultipleLines,InternetService,OnlineSecurity,OnlineBackup,DeviceProtection,TechSupport,StreamingTV,StreamingMovies,Contract,PaperlessBilling,PaymentMethod,MonthlyCharges,TotalCharges\n" +
                                "Female,x,Yes,No,24,Yes,No,DSL,Yes,No,No,No,No,No,One year,Yes,Electronic check,29.85,1889.50\n"; // invalid integer in SeniorCitizen
        MockMultipartFile file = new MockMultipartFile(
                "file", "bad.csv", "text/csv", badCsv.getBytes()
        );

        mockMvc.perform(multipart("/api/churn/predict/batch/csv")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }
}