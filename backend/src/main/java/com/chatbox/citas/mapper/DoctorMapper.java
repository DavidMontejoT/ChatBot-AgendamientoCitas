package com.chatbox.citas.mapper;

import com.chatbox.citas.dto.DoctorRequest;
import com.chatbox.citas.dto.DoctorResponse;
import com.chatbox.citas.model.Doctor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for Doctor entities
 * Handles conversion between Doctor entity and DTOs
 */
@Component
public class DoctorMapper {

    /**
     * Map Doctor entity to DoctorResponse DTO
     */
    public DoctorResponse toResponse(Doctor doctor) {
        return new DoctorResponse(
            doctor.getId(),
            doctor.getNombre(),
            doctor.getEspecialidad(),
            doctor.getTelefono(),
            doctor.getEmail(),
            doctor.getEstado(),
            doctor.getCreadoEn(),
            doctor.getActualizadoEn()
        );
    }

    /**
     * Map list of Doctor entities to list of DoctorResponse DTOs
     */
    public List<DoctorResponse> toResponseList(List<Doctor> doctores) {
        return doctores.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Map DoctorRequest DTO to Doctor entity (for creation)
     */
    public Doctor toEntity(DoctorRequest request) {
        Doctor doctor = new Doctor();
        doctor.setNombre(request.getNombre());
        doctor.setEspecialidad(request.getEspecialidad());
        doctor.setTelefono(request.getTelefono());
        doctor.setEmail(request.getEmail());
        doctor.setEstado(Doctor.EstadoDoctor.ACTIVO);
        return doctor;
    }

    /**
     * Map DoctorRequest DTO to Doctor entity (for update)
     */
    public Doctor toEntityForUpdate(DoctorRequest request) {
        Doctor doctor = new Doctor();
        doctor.setNombre(request.getNombre());
        doctor.setEspecialidad(request.getEspecialidad());
        doctor.setTelefono(request.getTelefono());
        doctor.setEmail(request.getEmail());
        // Note: Estado is not updated via this mapper
        return doctor;
    }
}
