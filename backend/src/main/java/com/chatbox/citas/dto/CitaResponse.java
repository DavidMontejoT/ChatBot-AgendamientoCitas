package com.chatbox.citas.dto;

import com.chatbox.citas.model.Cita.EstadoCita;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CitaResponse {

    private Long id;
    private String nombrePaciente;
    private String telefono;
    private String email;
    private LocalDateTime fechaHora;
    private String doctor;
    private EstadoCita estado;
    private LocalDateTime creadoEn;
}
