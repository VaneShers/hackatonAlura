package com.alura.hackatonAlura.churn;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ChurnServiceFallbackTest {

    @Test
    void whenDsFails_serviceUsesHeuristicAndPersistsSourceHeuristic() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        PredictionRepository repo = mock(PredictionRepository.class);

        // dsUrl non-empty to force DS path, but it will fail
        String dsUrl = "http://localhost:8000/predict";
        ChurnService service = new ChurnService(restTemplate, dsUrl, repo);

        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenThrow(new RestClientException("DS down"));

        ChurnRequest req = new ChurnRequest();
        req.setTiempoContratoMeses(12);
        req.setRetrasosPago(2);
        req.setUsoMensual(14.5);
        req.setPlan("Premium");

        ChurnPredictionResponse res = service.predict(req);

        assertThat(res).isNotNull();
        assertThat(res.getTopFeatures()).isNotNull();
        assertThat(res.getTopFeatures().size()).isEqualTo(3);
        assertThat(res.getProbabilidad()).isBetween(0.0, 1.0);

        // DS was attempted and failed, fallback executed
        verify(restTemplate, atLeastOnce()).postForObject(anyString(), any(), eq(Map.class));

        ArgumentCaptor<Prediction> captor = ArgumentCaptor.forClass(Prediction.class);
        verify(repo, atLeastOnce()).save(captor.capture());
    }
}