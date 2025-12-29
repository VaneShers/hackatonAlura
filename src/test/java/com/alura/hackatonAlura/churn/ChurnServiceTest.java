package com.alura.hackatonAlura.churn;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

public class ChurnServiceTest {

    @Test
    void fallbackProducesProbabilityAndLabel() {
        RestTemplate rt = new RestTemplate();
        PredictionRepository repo = Mockito.mock(PredictionRepository.class);
        ChurnService service = new ChurnService(rt, "", repo);
        ChurnRequest req = new ChurnRequest();
        req.setTiempoContratoMeses(12);
        req.setRetrasosPago(2);
        req.setUsoMensual(14.5);
        req.setPlan("Premium");

        ChurnPredictionResponse res = service.predict(req);
        assertNotNull(res);
        assertTrue(res.getProbabilidad() >= 0.0 && res.getProbabilidad() <= 1.0);
        assertNotNull(res.getPrevision());
    }
}
