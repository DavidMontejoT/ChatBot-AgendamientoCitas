package com.chatbox.citas.service;

import com.chatbox.citas.model.Paciente;
import com.chatbox.citas.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PacienteService {

    private final PacienteRepository pacienteRepository;

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

    public Optional<Paciente> buscarPorTelefono(String telefono) {
        return pacienteRepository.findByTelefono(telefono);
    }

    public Paciente guardar(Paciente paciente) {
        return pacienteRepository.save(paciente);
    }
}
