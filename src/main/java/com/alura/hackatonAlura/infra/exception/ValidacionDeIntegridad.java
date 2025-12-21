package com.alura.hackatonAlura.infra.exception;

public class ValidacionDeIntegridad extends RuntimeException {
    public ValidacionDeIntegridad(String string) {
        super(string);
    }
}