package com.chatbox.citas.repository;

import com.chatbox.citas.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    Optional<Paciente> findByTelefono(String telefono);

    boolean existsByTelefono(String telefono);
}
