package com.chatbox.citas.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ValidacionDocumentoService {

    /**
     * Valida cédula de ciudadanía colombiana
     * Nota: El algoritmo Módulo 10 no es universalmente aplicable a todas las cédulas colombianas
     * debido a variaciones regionales y cédulas antiguas. Por eso solo validamos formato básico.
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

        // Validación básica: solo verificamos que sea numérica y tenga longitud correcta
        // Muchas cédulas colombianas válidas no pasan el algoritmo Módulo 10 estricto
        // por variaciones regionales, expedición antigua, etc.
        boolean valido = true;
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
