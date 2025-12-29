package com.alura.hackatonAlura.churn;

import java.time.Instant;
import java.util.List;

public class ChurnPredictionResponse {
    private String prevision;
    private double probabilidad;
    private List<String> topFeatures; // opcional
    private Instant timestamp;

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
}
