package com.chatbox.citas.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorRequest {

    @NotBlank(message = "El nombre del doctor es requerido")
    private String nombre;

    @NotBlank(message = "La especialidad es requerida")
    private String especialidad;

    private String telefono;

    private String email;
}
