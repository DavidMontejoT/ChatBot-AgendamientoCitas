package com.chatbox.citas.service;

import com.chatbox.citas.model.Paciente;
import com.chatbox.citas.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    // Método existente (mantener para compatibilidad)
    public Paciente obtenerOCrearPaciente(String nombre, String telefono, String email) {
        return pacienteRepository.findByTelefono(telefono)
                .orElseGet(() -> {
                    Paciente nuevoPaciente = new Paciente();
                    nuevoPaciente.setNombre(nombre);
                    nuevoPaciente.setTelefono(telefono);
                    nuevoPaciente.setEmail(email);
                    return pacienteRepository.save(nuevoPaciente);
                });
    }

    // Nuevo método completo con todos los campos
    public Paciente obtenerOCrearPacienteCompleto(
        String nombre,
        String tipoIdentificacion,
        String numeroIdentificacion,
        String telefono,
        String telefono2,
        String email,
        String direccion,
        LocalDate fechaNacimiento,
        String eps
    ) {
        // Buscar por número de identificación (prioridad sobre teléfono)
        return pacienteRepository.findByNumeroIdentificacion(numeroIdentificacion)
                .orElseGet(() -> {
                    Paciente nuevoPaciente = new Paciente();
                    nuevoPaciente.setNombre(nombre);
                    nuevoPaciente.setTipoIdentificacion(tipoIdentificacion);
                    nuevoPaciente.setNumeroIdentificacion(numeroIdentificacion);
                    nuevoPaciente.setTelefono(telefono);
                    nuevoPaciente.setTelefono2(telefono2);
                    nuevoPaciente.setEmail(email);
                    nuevoPaciente.setDireccion(direccion);
                    nuevoPaciente.setFechaNacimiento(fechaNacimiento);
                    nuevoPaciente.setEps(eps);
                    return pacienteRepository.save(nuevoPaciente);
                });
    }

    public Optional<Paciente> buscarPorTelefono(String telefono) {
        return pacienteRepository.findByTelefono(telefono);
    }

    public Optional<Paciente> buscarPorNumeroIdentificacion(String numeroIdentificacion) {
        return pacienteRepository.findByNumeroIdentificacion(numeroIdentificacion);
    }

    public Paciente guardar(Paciente paciente) {
        return pacienteRepository.save(paciente);
    }
}
