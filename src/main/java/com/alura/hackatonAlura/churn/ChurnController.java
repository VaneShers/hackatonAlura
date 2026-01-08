package com.alura.hackatonAlura.churn;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping(path = "/api/churn", produces = MediaType.APPLICATION_JSON_VALUE)
public class ChurnController {

    private final ChurnService churnService;
    private static final Logger log = LoggerFactory.getLogger(ChurnController.class);

    public ChurnController(ChurnService churnService ) {
        this.churnService = churnService;
    }

    @PostMapping(path = "/predict", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ChurnPredictionResponse predict(@Valid @RequestBody ChurnRequest request) {
        // Log básico con campos clave
        log.info("/predict recibido: tenure={}, MonthlyCharges={}, TotalCharges={}",
                request.getTenure(), request.getMonthlyCharges(), request.getTotalCharges());
        return churnService.predict(request);
    }

    @GetMapping(path = "/stats")
    public Map<String, Object> stats() {
        return churnService.stats();
    }

    @PostMapping(path = "/predict/batch", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> predictBatch(@Valid @RequestBody List<ChurnRequest> requests) {
        log.info("/predict/batch recibido: {} elementos", requests.size());
        List<ChurnPredictionResponse> results = requests.stream().map(churnService::predict).toList();
        long cancela = results.stream().filter(r -> "Va a cancelar".equalsIgnoreCase(r.getPrevision())).count();
        log.info("/predict/batch resultado: total={}, cancelaciones={}", results.size(), cancela);
        return Map.of(
                "items", results,
                "total", results.size(),
                "cancelaciones", cancela
        );
    }

    @PostMapping(path = "/predict/batch/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> predictBatchCsv(@RequestPart("file") MultipartFile file) throws Exception {
        try (var reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             var parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim())) {
            var headerMap = parser.getHeaderMap();
            List<ChurnRequest> reqs = parser.getRecords().stream().map(r -> toRequest(r, headerMap)).toList();
            log.info("/predict/batch/csv procesado: {} filas", reqs.size());
            return predictBatch(reqs);
        }
    }

    @PostMapping(path = "/evaluate/batch/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> evaluateBatchCsv(@RequestPart("file") MultipartFile file) throws Exception {
        try (var reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             var parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim())) {
            var headerMap = parser.getHeaderMap();
            // Verificar columna Churn
            boolean hasChurn = headerMap.keySet().stream().anyMatch(h -> "churn".equalsIgnoreCase(h));
            if (!hasChurn) {
                throw new IllegalArgumentException("El CSV de evaluación debe incluir columna 'Churn' con valores 'Yes'/'No'.");
            }

            int tp = 0, tn = 0, fp = 0, fn = 0;
            int total = 0;
            for (CSVRecord r : parser.getRecords()) {
                total++;
                ChurnRequest req = toRequest(r, headerMap);
                String churnVal = getHeaderValue(r, headerMap, "Churn");
                int actual = parseChurnLabel(churnVal);
                ChurnPredictionResponse res = churnService.predict(req);
                int pred = ("Va a cancelar".equalsIgnoreCase(res.getPrevision())) ? 1 : 0;
                if (pred == 1 && actual == 1) tp++;
                else if (pred == 0 && actual == 0) tn++;
                else if (pred == 1 && actual == 0) fp++;
                else fn++;
            }

            double precision = (tp + fp) == 0 ? 0.0 : ((double) tp) / (tp + fp);
            double recall = (tp + fn) == 0 ? 0.0 : ((double) tp) / (tp + fn);
            double accuracy = total == 0 ? 0.0 : ((double) (tp + tn)) / total;
            double f1 = (precision + recall) == 0 ? 0.0 : (2 * precision * recall) / (precision + recall);

            log.info("/evaluate/batch/csv: total={}, tp={}, tn={}, fp={}, fn={}, acc={}", total, tp, tn, fp, fn, String.format("%.3f", accuracy));
            return Map.of(
                    "total", total,
                    "tp", tp,
                    "tn", tn,
                    "fp", fp,
                    "fn", fn,
                    "accuracy", accuracy,
                    "precision", precision,
                    "recall", recall,
                    "f1", f1
            );
        }
    }

    private ChurnRequest toRequest(CSVRecord r, java.util.Map<String, Integer> headerMap) {
        try {
            // Construir mapa de encabezados en minúsculas para tolerancia de caso
            java.util.Map<String, Integer> lower = new java.util.HashMap<>();
            for (var e : headerMap.entrySet()) {
                lower.put(e.getKey().toLowerCase(), e.getValue());
            }

            java.util.function.Function<String, String> getVal = (key) -> {
                Integer idx = lower.get(key.toLowerCase());
                if (idx == null) return null;
                String v = r.get(idx);
                return v;
            };

            ChurnRequest c = new ChurnRequest();
            // Strings (case-sensitive)
            c.setGender(reqOrThrow(getVal.apply("gender"), r));
            c.setPartner(reqOrThrow(getVal.apply("Partner"), r));
            c.setDependents(reqOrThrow(getVal.apply("Dependents"), r));
            c.setPhoneService(reqOrThrow(getVal.apply("PhoneService"), r));
            c.setMultipleLines(reqOrThrow(getVal.apply("MultipleLines"), r));
            c.setInternetService(reqOrThrow(getVal.apply("InternetService"), r));
            c.setOnlineSecurity(reqOrThrow(getVal.apply("OnlineSecurity"), r));
            c.setOnlineBackup(reqOrThrow(getVal.apply("OnlineBackup"), r));
            c.setDeviceProtection(reqOrThrow(getVal.apply("DeviceProtection"), r));
            c.setTechSupport(reqOrThrow(getVal.apply("TechSupport"), r));
            c.setStreamingTV(reqOrThrow(getVal.apply("StreamingTV"), r));
            c.setStreamingMovies(reqOrThrow(getVal.apply("StreamingMovies"), r));
            c.setContract(reqOrThrow(getVal.apply("Contract"), r));
            c.setPaperlessBilling(reqOrThrow(getVal.apply("PaperlessBilling"), r));
            c.setPaymentMethod(reqOrThrow(getVal.apply("PaymentMethod"), r));

            // Integer 0/1 for SeniorCitizen
            String sc = reqOrThrow(getVal.apply("SeniorCitizen"), r);
            c.setSeniorCitizen(Integer.parseInt(sc));
            // Tenure
            String ten = reqOrThrow(getVal.apply("tenure"), r);
            c.setTenure(Integer.parseInt(ten));

            // Numeric: MonthlyCharges
            String mc = reqOrThrow(getVal.apply("MonthlyCharges"), r);
            c.setMonthlyCharges(Double.parseDouble(mc));
            // Numeric: TotalCharges (option A: blank/null -> 0.0)
            String tc = getVal.apply("TotalCharges");
            if (tc == null || tc.isBlank()) {
                c.setTotalCharges(0.0);
            } else {
                c.setTotalCharges(Double.parseDouble(tc));
            }
            return c;
        } catch (Exception ex) {
            String msg = "CSV fila " + r.getRecordNumber() + ": " + ex.getMessage();
            log.warn(msg);
            throw new IllegalArgumentException(msg, ex);
        }
    }

    private String reqOrThrow(String v, CSVRecord r) {
        if (v == null) {
            throw new IllegalArgumentException("Falta columna requerida en CSV");
        }
        return v;
    }

    private String getHeaderValue(CSVRecord r, java.util.Map<String, Integer> headerMap, String key) {
        Integer idx = null;
        for (var e : headerMap.entrySet()) {
            if (e.getKey().equalsIgnoreCase(key)) { idx = e.getValue(); break; }
        }
        if (idx == null) return null;
        return r.get(idx);
    }

    private int parseChurnLabel(String v) {
        if (v == null) return 0;
        String s = v.trim();
        if ("Yes".equalsIgnoreCase(s)) return 1;
        if ("No".equalsIgnoreCase(s)) return 0;
        // tolerancia opcional
        if ("1".equals(s)) return 1;
        if ("0".equals(s)) return 0;
        return 0;
    }
}
