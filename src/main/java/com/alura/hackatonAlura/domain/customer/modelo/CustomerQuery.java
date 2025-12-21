package com.alura.hackatonAlura.domain.customer.modelo;


import lombok.Getter;


@Getter
public class CustomerQuery {
    //Datos definidos en salida que se esperan obtener
    private String prevision;
    private double probabilidad;


    public CustomerQuery(String prevision, double probabilidad){
        if (prevision == null || prevision.isBlank()) throw new IllegalArgumentException("Es necesario una prevision");
        if (probabilidad < 0 || probabilidad > 1) throw new IllegalArgumentException("Probabilidad invalida");


        this.prevision = prevision;
        this.probabilidad = probabilidad;
    }
}
