package com.alura.hackatonAlura.domain.customer.service;

import com.alura.hackatonAlura.domain.customer.modelo.Customer;
import com.alura.hackatonAlura.domain.customer.modelo.CustomerQuery;

public class ResultHandler {
    private double threshold;

    public ResultHandler(double threshold) {
        if (threshold < 0 || threshold > 1) {
            throw new IllegalArgumentException("El threshold invalido");
        }
        this.threshold = 0.6;
    }

    public CustomerQuery deduction(Customer customer) {

        double probabilidad = 0;
        String prevision;

        if (probabilidad >= threshold) {prevision = "Va a cancelar";
        } else { prevision = "Va a continuar";}

        return new CustomerQuery(prevision, probabilidad);

    }
}
