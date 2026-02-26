package com.chatbox.citas.dto;

import com.chatbox.citas.model.Doctor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponse {

    private Long id;
    private String nombre;
    private String especialidad;
    private String telefono;
    private String email;
    private Doctor.EstadoDoctor estado;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
}
