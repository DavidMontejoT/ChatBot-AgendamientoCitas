package com.chatbox.citas.service;

import com.chatbox.citas.dto.CitaRequest;
import com.chatbox.citas.dto.CitaRequestCompleto;
import com.chatbox.citas.dto.CitaResponse;
import com.chatbox.citas.model.Cita;
import com.chatbox.citas.model.Cita.EstadoCita;
import com.chatbox.citas.model.Doctor;
import com.chatbox.citas.model.Paciente;
import com.chatbox.citas.repository.CitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CitaService {

    private final CitaRepository citaRepository;
    private final PacienteService pacienteService;
    private final DoctorService doctorService;

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

    public List<CitaResponse> obtenerTodasLasCitas() {
        return citaRepository.findAllByOrderByFechaHoraDesc()
                .stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
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

    // Nuevo método para crear cita con información completa del paciente
    public CitaResponse crearCitaCompleta(CitaRequestCompleto request) {
        Paciente paciente = pacienteService.obtenerOCrearPacienteCompleto(
                request.getNombrePaciente(),
                request.getTipoIdentificacion(),
                request.getNumeroIdentificacion(),
                request.getTelefono(),
                request.getTelefono2(),
                request.getEmail(),
                request.getDireccion(),
                request.getFechaNacimiento(),
                request.getEps()
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

    /**
     * Obtiene los horarios disponibles para los doctores en una fecha específica
     * Retorna una lista de objetos con doctor y hora disponibles
     */
    public List<Object> obtenerHorariosDisponibles(LocalDate fecha) {
        List<Object> disponibilidad = new java.util.ArrayList<>();
        var doctores = doctorService.obtenerActivos();

        // Horarios de trabajo: 6:00 AM a 5:00 PM
        int[] horas = {6, 7, 8, 9, 10, 11, 14, 15, 16, 17};

        for (int hora : horas) {
            for (Doctor doctor : doctores) {
                boolean disponible = isDoctorDisponible(doctor.getNombre(), fecha, hora);
                disponibilidad.add(new DisponibilidadDoctor(
                    doctor.getNombre(),
                    String.format("%02d:00", hora),
                    disponible,
                    doctor.getEspecialidad()
                ));
            }
        }

        return disponibilidad;
    }

    /**
     * Verifica si un doctor está disponible en una fecha y hora específicas
     */
    public boolean isDoctorDisponible(String nombreDoctor, LocalDate fecha, int hora) {
        List<Cita> citas = citaRepository.findCitaPorDoctorYFechaHora(
            nombreDoctor, fecha, hora
        );
        return citas.isEmpty(); // Si no hay citas, el doctor está disponible
    }

    private record DisponibilidadDoctor(String doctor, String hora, boolean disponible, String especialidad) {}
}
