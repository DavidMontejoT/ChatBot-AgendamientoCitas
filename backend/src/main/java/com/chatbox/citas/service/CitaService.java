package com.chatbox.citas.service;

import com.chatbox.citas.dto.CitaRequest;
import com.chatbox.citas.dto.CitaResponse;
import com.chatbox.citas.model.Cita;
import com.chatbox.citas.model.Cita.EstadoCita;
import com.chatbox.citas.model.Paciente;
import com.chatbox.citas.repository.CitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CitaService {

    private final CitaRepository citaRepository;
    private final PacienteService pacienteService;

    public CitaResponse crearCita(CitaRequest request) {
        Paciente paciente = pacienteService.obtenerOCrearPaciente(
                request.getNombrePaciente(),
                request.getTelefono(),
                request.getEmail()
        );

        Cita cita = new Cita();
        cita.setPaciente(paciente);
        cita.setFechaHora(request.getFechaHora());
        cita.setDoctor(request.getDoctor());
        cita.setEstado(EstadoCita.PROGRAMADA);
        cita.setRecordatorio24hEnviado(false);
        cita.setRecordatorio1hEnviado(false);

        Cita citaGuardada = citaRepository.save(cita);
        return mapearAResponse(citaGuardada);
    }

    public List<CitaResponse> obtenerCitasPorTelefono(String telefono) {
        return citaRepository.findByPaciente_TelefonoOrderByFechaHoraDesc(telefono)
                .stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    public CitaResponse obtenerCitaPorId(Long id) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        return mapearAResponse(cita);
    }

    public CitaResponse cancelarCita(Long id) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        cita.setEstado(EstadoCita.CANCELADA);
        Cita citaCancelada = citaRepository.save(cita);
        return mapearAResponse(citaCancelada);
    }

    public List<Cita> obtenerCitasParaRecordatorio24h(LocalDateTime inicio, LocalDateTime fin) {
        return citaRepository.findCitasParaRecordatorio24h(inicio, fin);
    }

    public List<Cita> obtenerCitasParaRecordatorio1h(LocalDateTime inicio, LocalDateTime fin) {
        return citaRepository.findCitasParaRecordatorio1h(inicio, fin);
    }

    public void marcarRecordatorioEnviado(Cita cita, int horas) {
        if (horas == 24) {
            cita.setRecordatorio24hEnviado(true);
        } else if (horas == 1) {
            cita.setRecordatorio1hEnviado(true);
        }
        citaRepository.save(cita);
    }

    private CitaResponse mapearAResponse(Cita cita) {
        return new CitaResponse(
                cita.getId(),
                cita.getPaciente().getNombre(),
                cita.getPaciente().getTelefono(),
                cita.getPaciente().getEmail(),
                cita.getFechaHora(),
                cita.getDoctor(),
                cita.getEstado(),
                cita.getCreadoEn()
        );
    }
}
