package com.alura.hackatonAlura.churn;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ChurnRequest {
    @JsonProperty("tiempo_contrato_meses")
    @NotNull @Min(0)
    private Integer tiempoContratoMeses;

    @JsonProperty("retrasos_pago")
    @NotNull @Min(0)
    private Integer retrasosPago;

    @JsonProperty("uso_mensual")
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true, message = "Debe ser un número no negativo")
    private Double usoMensual;

    @JsonProperty("plan")
    @NotBlank(message = "Debe ser un texto no vacío")
    @Pattern(regexp = "(?i)basic|standard|premium", message = "Valor inválido: use Basic/Standard/Premium")
    private String plan;

    public Integer getTiempoContratoMeses() { return tiempoContratoMeses; }
    public void setTiempoContratoMeses(Integer tiempoContratoMeses) { this.tiempoContratoMeses = tiempoContratoMeses; }

    public Integer getRetrasosPago() { return retrasosPago; }
    public void setRetrasosPago(Integer retrasosPago) { this.retrasosPago = retrasosPago; }

    public Double getUsoMensual() { return usoMensual; }
    public void setUsoMensual(Double usoMensual) { this.usoMensual = usoMensual; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }
}
