package com.chatbox.citas.repository;

import com.chatbox.citas.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    List<Doctor> findByEstado(com.chatbox.citas.model.Doctor.EstadoDoctor estado);

    List<Doctor> findByEspecialidadContainingIgnoreCase(String especialidad);

    List<Doctor> findByEstadoAndEspecialidadContainingIgnoreCase(
        com.chatbox.citas.model.Doctor.EstadoDoctor estado,
        String especialidad
    );
}
