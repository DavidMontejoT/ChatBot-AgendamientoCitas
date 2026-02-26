package com.chatbox.citas.service;

import com.chatbox.citas.model.Doctor;
import com.chatbox.citas.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public List<Doctor> obtenerTodos() {
        return doctorRepository.findAll();
    }

    public List<Doctor> obtenerActivos() {
        return doctorRepository.findByEstado(Doctor.EstadoDoctor.ACTIVO);
    }

    public List<Doctor> obtenerPorEspecialidad(String especialidad) {
        return doctorRepository.findByEspecialidadContainingIgnoreCase(especialidad);
    }

    public List<Doctor> obtenerActivosPorEspecialidad(String especialidad) {
        return doctorRepository.findByEstadoAndEspecialidadContainingIgnoreCase(
            Doctor.EstadoDoctor.ACTIVO,
            especialidad
        );
    }

    public Optional<Doctor> obtenerPorId(Long id) {
        return doctorRepository.findById(id);
    }

    public Doctor guardar(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    public Doctor actualizar(Long id, Doctor doctorActualizado) {
        Doctor doctor = doctorRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Doctor no encontrado"));

        doctor.setNombre(doctorActualizado.getNombre());
        doctor.setEspecialidad(doctorActualizado.getEspecialidad());
        doctor.setTelefono(doctorActualizado.getTelefono());
        doctor.setEmail(doctorActualizado.getEmail());
        doctor.setEstado(doctorActualizado.getEstado());

        return doctorRepository.save(doctor);
    }

    public void eliminar(Long id) {
        Doctor doctor = doctorRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Doctor no encontrado"));

        // Soft delete: cambiar estado a INACTIVO en lugar de borrar
        doctor.setEstado(Doctor.EstadoDoctor.INACTIVO);
        doctorRepository.save(doctor);
    }

    public void eliminarPermanentemente(Long id) {
        if (!doctorRepository.existsById(id)) {
            throw new RuntimeException("Doctor no encontrado");
        }
        doctorRepository.deleteById(id);
    }
}
