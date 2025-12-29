package com.alura.hackatonAlura.churn;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ChurnService {

    private final RestTemplate restTemplate;
    private final String dsUrl;
    private final PredictionRepository predictionRepository;

    private final AtomicInteger totalEvaluados = new AtomicInteger(0);
    private final AtomicInteger churnCount = new AtomicInteger(0);
    private static final Logger log = LoggerFactory.getLogger(ChurnService.class);

    public ChurnService(RestTemplate restTemplate,
                        @Value("${churn.ds.url:}") String dsUrl,
                        PredictionRepository predictionRepository) {
        this.restTemplate = restTemplate;
        this.dsUrl = dsUrl == null ? "" : dsUrl.trim();
        this.predictionRepository = predictionRepository;
    }

    public ChurnPredictionResponse predict(ChurnRequest req) {
        ChurnPredictionResponse res = null;
        if (!dsUrl.isEmpty()) {
            try {
                res = callDsService(req);
            } catch (RestClientException | IllegalArgumentException ex) {
                log.warn("Fallo al invocar DS: {}. Usando heurístico.", ex.getMessage());
                res = heuristicFallback(req);
            }
        } else {
            res = heuristicFallback(req);
        }
        totalEvaluados.incrementAndGet();
        if ("Va a cancelar".equalsIgnoreCase(res.getPrevision())) {
            churnCount.incrementAndGet();
        }
        // Persist
        Prediction p = new Prediction();
        p.setCreatedAt(Instant.now());
        p.setPrevision(res.getPrevision());
        p.setProbabilidad(res.getProbabilidad());
        p.setTiempoContratoMeses(req.getTiempoContratoMeses());
        p.setRetrasosPago(req.getRetrasosPago());
        p.setUsoMensual(req.getUsoMensual());
        p.setPlan(req.getPlan());
        p.setSource(res.getTopFeatures() != null ? (dsUrl.isEmpty() ? "heuristic" : "DS") : (dsUrl.isEmpty() ? "heuristic" : "DS"));
        predictionRepository.save(p);
        log.info("Predicción: label={}, prob={}, source={}", res.getPrevision(), String.format("%.3f", res.getProbabilidad()), p.getSource());
        return res;
    }

    public Map<String, Object> stats() {
        int total = totalEvaluados.get();
        double tasa = total == 0 ? 0.0 : ((double) churnCount.get()) / total;
        return Map.of(
                "total_evaluados", total,
                "tasa_churn", tasa
        );
    }

    private ChurnPredictionResponse callDsService(ChurnRequest req) {
        // Contract: send nested features object for flexibility
        Map<String, Object> payload = Map.of(
                "features", Map.of(
                        "tiempo_contrato_meses", req.getTiempoContratoMeses(),
                        "retrasos_pago", req.getRetrasosPago(),
                        "uso_mensual", req.getUsoMensual(),
                        "plan", req.getPlan()
                )
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        Map<?,?> response = restTemplate.postForObject(dsUrl, entity, Map.class);
        if (response == null) throw new IllegalArgumentException("Respuesta vacía del servicio DS");
        Object prev = response.get("prevision");
        Object prob = response.get("probabilidad");
        Object tf = response.get("top_features");
        if (!(prev instanceof String) || !(prob instanceof Number)) {
            throw new IllegalArgumentException("Formato inválido del servicio DS");
        }
        List<String> top = null;
        if (tf instanceof List<?>) {
            top = ((List<?>) tf).stream().filter(x -> x instanceof String).map(x -> (String) x).limit(3).toList();
        }
        if (top == null || top.isEmpty()) {
            top = List.of("plan","retrasos_pago","tiempo_contrato_meses");
        }
        return new ChurnPredictionResponse((String) prev,
                ((Number) prob).doubleValue(), top, Instant.now());
    }

    private ChurnPredictionResponse heuristicFallback(ChurnRequest req) {
        // Simple logistic-like heuristic for MVP
        double planRisk = switch (req.getPlan().toLowerCase()) {
            case "basic" -> 0.15;
            case "standard" -> 0.10;
            case "premium" -> 0.05;
            default -> 0.12; // unknown
        };
        double cRetrasos = 0.08 * req.getRetrasosPago();
        double cTiempo = -0.03 * req.getTiempoContratoMeses();
        double cUso = -0.02 * req.getUsoMensual();
        double cPlan = planRisk;
        double z = -1.0 + cRetrasos + cTiempo + cUso + cPlan;
        double p = 1.0 / (1.0 + Math.exp(-z));
        String label = p >= 0.5 ? "Va a cancelar" : "Va a continuar";
        // Explainability: order by absolute contribution
        java.util.Map<String, Double> contrib = new java.util.HashMap<>();
        contrib.put("retrasos_pago", Math.abs(cRetrasos));
        contrib.put("tiempo_contrato_meses", Math.abs(cTiempo));
        contrib.put("uso_mensual", Math.abs(cUso));
        contrib.put("plan", Math.abs(cPlan));
        List<String> top = contrib.entrySet().stream()
            .sorted((a,b) -> Double.compare(b.getValue(), a.getValue()))
            .map(Map.Entry::getKey)
            .limit(3).toList();
        return new ChurnPredictionResponse(label, p, top, Instant.now());
    }
}
