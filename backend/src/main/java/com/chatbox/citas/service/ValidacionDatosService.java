package com.chatbox.citas.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@Service
public class ValidacionDatosService {

    /**
     * Valida número de teléfono colombiano (formato: 3XX XXX XXXX)
     */
    public boolean validarTelefonoColombiano(String telefono) {
        if (telefono == null) {
            return false;
        }

        // Eliminar espacios y guiones
        String telefonoLimpio = telefono.replaceAll("[\\s\\-]", "");

        boolean valido = telefonoLimpio.matches("^3\\d{9}$");
        log.debug("Teléfono {} válido: {}", telefono, valido);
        return valido;
    }

    /**
     * Valida fecha de nacimiento y retorna fecha parseada
     * Retorna null si la fecha es inválida o el paciente es menor de edad
     */
    public LocalDate validarFechaNacimiento(String fechaStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            LocalDate fechaNacimiento = LocalDate.parse(fechaStr, formatter);
            LocalDate hoy = LocalDate.now();

            if (fechaNacimiento.isAfter(hoy)) {
                log.debug("Fecha de nacimiento inválida: está en el futuro");
                return null;
            }

            int edad = Period.between(fechaNacimiento, hoy).getYears();
            if (edad < 18) {
                log.debug("Fecha de nacimiento inválida: menor de edad ({} años)", edad);
                return null;
            }

            if (edad > 120) {
                log.debug("Fecha de nacimiento inválida: edad no realista ({} años)", edad);
                return null;
            }

            log.debug("Fecha de nacimiento válida: {} (edad: {} años)", fechaStr, edad);
            return fechaNacimiento;

        } catch (DateTimeParseException e) {
            log.debug("Error parseando fecha de nacimiento: {}", fechaStr);
            return null;
        }
    }

    /**
     * Formatea número de teléfono eliminando espacios y guiones
     */
    public String formatearTelefono(String telefono) {
        if (telefono == null) {
            return null;
        }
        return telefono.replaceAll("[\\s\\-]", "");
    }
}
