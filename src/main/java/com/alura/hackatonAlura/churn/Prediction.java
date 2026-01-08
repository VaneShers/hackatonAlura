package com.alura.hackatonAlura.churn;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "predictions")
@Getter
@Setter
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
    private String riskLevel;

    @Column(nullable = false)
    private Integer tiempoContratoMeses;

    @Column(nullable = false)
    private Integer retrasosPago;

    @Column(nullable = false)
    private Double usoMensual;

    // 'plan' ya no es obligatorio; se permite nulo para compatibilidad histórica
    private String plan;

    @Column(nullable = false)
    private String source; // DS | heuristic

}