package com.chatbox.citas.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CitaRequestCompleto {

    // Datos del paciente completos
    @NotBlank(message = "El nombre del paciente es requerido")
    private String nombrePaciente;

    @NotBlank(message = "El tipo de identificación es requerido")
    @Pattern(regexp = "CC|TI|RC", message = "Tipo debe ser CC, TI o RC")
    private String tipoIdentificacion;

    @NotBlank(message = "El número de identificación es requerido")
    private String numeroIdentificacion;

    @NotBlank(message = "El teléfono es requerido")
    private String telefono;

    private String telefono2;

    private String email;

    @NotBlank(message = "La dirección es requerida")
    private String direccion;

    @NotNull(message = "La fecha de nacimiento es requerida")
    private LocalDate fechaNacimiento;

    @NotBlank(message = "La EPS es requerida")
    private String eps;

    // Datos de la cita
    @NotNull(message = "La fecha y hora son requeridas")
    @Future(message = "La fecha debe ser futura")
    private LocalDateTime fechaHora;

    @NotBlank(message = "El nombre del doctor es requerido")
    private String doctor;

    private String tipoCita; // Primera vez, Control, Cirugía
}
