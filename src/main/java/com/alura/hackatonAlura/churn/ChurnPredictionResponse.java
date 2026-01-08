package com.alura.hackatonAlura.churn;

import java.time.Instant;
import java.util.List;

public class ChurnPredictionResponse {
    private String prevision;
    private double probabilidad;
    private List<String> topFeatures; // opcional
    private Instant timestamp;

    // Nuevo formato enriquecido
    public static class Metadata {
        public String model_version;
        public Instant timestamp;
    }
    public static class PredictionInfo {
        public double churn_probability;
        public int will_churn; // 0/1
        public String risk_level; // Bajo/Medio/Alto (o en inglés)
        public double confidence_score;
    }
    public static class BusinessLogic {
        public String suggested_action;
    }

    private Metadata metadata;
    private PredictionInfo prediction;
    private BusinessLogic business_logic;

    public ChurnPredictionResponse() {}

    public ChurnPredictionResponse(String prevision, double probabilidad, List<String> topFeatures, Instant timestamp) {
        this.prevision = prevision;
        this.probabilidad = probabilidad;
        this.topFeatures = topFeatures;
        this.timestamp = timestamp;
    }

    public String getPrevision() { return prevision; }
    public void setPrevision(String prevision) { this.prevision = prevision; }

    public double getProbabilidad() { return probabilidad; }
    public void setProbabilidad(double probabilidad) { this.probabilidad = probabilidad; }

    public List<String> getTopFeatures() { return topFeatures; }
    public void setTopFeatures(List<String> topFeatures) { this.topFeatures = topFeatures; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public Metadata getMetadata() { return metadata; }
    public void setMetadata(Metadata metadata) { this.metadata = metadata; }
    public PredictionInfo getPrediction() { return prediction; }
    public void setPrediction(PredictionInfo prediction) { this.prediction = prediction; }
    public BusinessLogic getBusiness_logic() { return business_logic; }
    public void setBusiness_logic(BusinessLogic business_logic) { this.business_logic = business_logic; }
}
