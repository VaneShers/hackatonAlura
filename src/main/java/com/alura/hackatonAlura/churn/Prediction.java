package com.alura.hackatonAlura.churn;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "predictions")
public class Prediction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private String prevision;

    @Column(nullable = false)
    private double probabilidad;

    @Column(nullable = false)
    private Integer tiempoContratoMeses;

    @Column(nullable = false)
    private Integer retrasosPago;

    @Column(nullable = false)
    private Double usoMensual;

    @Column(nullable = false)
    private String plan;

    @Column(nullable = false)
    private String source; // DS | heuristic

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getPrevision() { return prevision; }
    public void setPrevision(String prevision) { this.prevision = prevision; }

    public double getProbabilidad() { return probabilidad; }
    public void setProbabilidad(double probabilidad) { this.probabilidad = probabilidad; }

    public Integer getTiempoContratoMeses() { return tiempoContratoMeses; }
    public void setTiempoContratoMeses(Integer tiempoContratoMeses) { this.tiempoContratoMeses = tiempoContratoMeses; }

    public Integer getRetrasosPago() { return retrasosPago; }
    public void setRetrasosPago(Integer retrasosPago) { this.retrasosPago = retrasosPago; }

    public Double getUsoMensual() { return usoMensual; }
    public void setUsoMensual(Double usoMensual) { this.usoMensual = usoMensual; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
