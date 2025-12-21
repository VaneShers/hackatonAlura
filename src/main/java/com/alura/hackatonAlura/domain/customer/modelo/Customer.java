package com.alura.hackatonAlura.domain.customer.modelo;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
public class Customer {
    //Datos definidos en entrada que se esperan obtener

    private int tiempoContratoMeses;
    private int retrasosPago;
    private double usoMensual;
    //En caso que plan sea solo premium = Boolean
    private String plan;

    public Customer(int tiempoContratoMeses, int retrasosPago, double usoMensual, String plan) {

        if (tiempoContratoMeses < 0) throw new IllegalArgumentException("Tiempo de contrato invalido");
        if (retrasosPago < 0) throw new IllegalArgumentException("Retrasos de pago invalido");
        if (usoMensual < 0) throw new IllegalArgumentException("Uso mensual invalido");
        if (plan == null || plan.isBlank()) throw new IllegalArgumentException("El plan es necesario");


        this.tiempoContratoMeses = tiempoContratoMeses;
        this.retrasosPago = retrasosPago;
        this.usoMensual = usoMensual;
        this.plan = plan;
    }

}




