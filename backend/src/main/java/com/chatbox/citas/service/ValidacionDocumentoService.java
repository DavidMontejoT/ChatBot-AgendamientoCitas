package com.chatbox.citas.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ValidacionDocumentoService {

    /**
     * Valida cédula de ciudadanía colombiana usando algoritmo Módulo 10
     */
    public boolean validarCC(String cedula) {
        if (cedula == null || cedula.length() < 8 || cedula.length() > 10) {
            log.debug("CC inválida: longitud incorrecta {}", cedula != null ? cedula.length() : 0);
            return false;
        }

        if (!cedula.matches("\\d+")) {
            log.debug("CC inválida: contiene caracteres no numéricos");
            return false;
        }

        // Algoritmo Módulo 10
        int suma = 0;
        for (int i = 0; i < cedula.length() - 1; i++) {
            int digito = Integer.parseInt(cedula.charAt(i) + "");

            if (i % 2 == 0) {
                digito = digito * 2;
                if (digito >= 10) {
                    digito = digito - 9;
                }
            }
            suma += digito;
        }

        int ultimoDigito = Integer.parseInt(cedula.charAt(cedula.length() - 1) + "");
        int digitoVerificador = (10 - (suma % 10)) % 10;

        boolean valido = ultimoDigito == digitoVerificador;
        log.debug("CC {} válida: {}", cedula, valido);
        return valido;
    }

    /**
     * Valida Tarjeta de Identidad colombiana
     */
    public boolean validarTI(String ti) {
        boolean valido = ti != null && ti.matches("\\d{6,12}");
        log.debug("TI {} válida: {}", ti, valido);
        return valido;
    }

    /**
     * Valida Registro Civil
     */
    public boolean validarRC(String rc) {
        boolean valido = rc != null && rc.matches("\\d{6,12}");
        log.debug("RC {} válido: {}", rc, valido);
        return valido;
    }
}
