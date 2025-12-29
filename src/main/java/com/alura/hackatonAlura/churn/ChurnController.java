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

    public ChurnController(ChurnService churnService) {
        this.churnService = churnService;
    }

    @PostMapping(path = "/predict", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ChurnPredictionResponse predict(@Valid @RequestBody ChurnRequest request) {
        log.info("/predict recibido: plan={}, retrasos={}, tiempoMeses={}, uso=...",
                request.getPlan(), request.getRetrasosPago(), request.getTiempoContratoMeses());
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
            List<ChurnRequest> reqs = parser.getRecords().stream().map(this::toRequest).toList();
            log.info("/predict/batch/csv procesado: {} filas", reqs.size());
            return predictBatch(reqs);
        }
    }

    private ChurnRequest toRequest(CSVRecord r) {
        try {
            ChurnRequest c = new ChurnRequest();
            c.setTiempoContratoMeses(Integer.parseInt(r.get("tiempo_contrato_meses")));
            c.setRetrasosPago(Integer.parseInt(r.get("retrasos_pago")));
            c.setUsoMensual(Double.parseDouble(r.get("uso_mensual")));
            c.setPlan(r.get("plan"));
            return c;
        } catch (Exception ex) {
            String msg = "CSV fila " + r.getRecordNumber() + ": " + ex.getMessage();
            log.warn(msg);
            throw new IllegalArgumentException(msg, ex);
        }
    }
}
