package com.chatbox.citas.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CitaRequest {

    @NotBlank(message = "El nombre del paciente es requerido")
    private String nombrePaciente;

    @NotBlank(message = "El teléfono es requerido")
    private String telefono;

    private String email;

    @NotNull(message = "La fecha y hora son requeridas")
    @Future(message = "La fecha debe ser futura")
    private LocalDateTime fechaHora;

    @NotBlank(message = "El nombre del doctor es requerido")
    private String doctor;
}
