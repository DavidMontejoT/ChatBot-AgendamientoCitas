package com.chatbox.citas.controller;

import com.chatbox.citas.dto.DoctorRequest;
import com.chatbox.citas.dto.DoctorResponse;
import com.chatbox.citas.model.Doctor;
import com.chatbox.citas.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doctores")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    public ResponseEntity<List<DoctorResponse>> obtenerTodos() {
        List<Doctor> doctores = doctorService.obtenerTodos();
        List<DoctorResponse> respuestas = doctores.stream()
            .map(this::mapearAResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(respuestas);
    }

    @GetMapping("/activos")
    public ResponseEntity<List<DoctorResponse>> obtenerActivos() {
        List<Doctor> doctores = doctorService.obtenerActivos();
        List<DoctorResponse> respuestas = doctores.stream()
            .map(this::mapearAResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(respuestas);
    }

    @GetMapping("/especialidad/{especialidad}")
    public ResponseEntity<List<DoctorResponse>> obtenerPorEspecialidad(
        @PathVariable String especialidad
    ) {
        List<Doctor> doctores = doctorService.obtenerActivosPorEspecialidad(especialidad);
        List<DoctorResponse> respuestas = doctores.stream()
            .map(this::mapearAResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(respuestas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> obtenerPorId(@PathVariable Long id) {
        Doctor doctor = doctorService.obtenerPorId(id)
            .orElseThrow(() -> new RuntimeException("Doctor no encontrado"));
        return ResponseEntity.ok(mapearAResponse(doctor));
    }

    @PostMapping
    public ResponseEntity<DoctorResponse> crear(@Valid @RequestBody DoctorRequest request) {
        Doctor doctor = new Doctor();
        doctor.setNombre(request.getNombre());
        doctor.setEspecialidad(request.getEspecialidad());
        doctor.setTelefono(request.getTelefono());
        doctor.setEmail(request.getEmail());
        doctor.setEstado(Doctor.EstadoDoctor.ACTIVO);

        Doctor guardado = doctorService.guardar(doctor);
        return ResponseEntity.ok(mapearAResponse(guardado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DoctorResponse> actualizar(
        @PathVariable Long id,
        @Valid @RequestBody DoctorRequest request
    ) {
        Doctor doctorActualizado = new Doctor();
        doctorActualizado.setNombre(request.getNombre());
        doctorActualizado.setEspecialidad(request.getEspecialidad());
        doctorActualizado.setTelefono(request.getTelefono());
        doctorActualizado.setEmail(request.getEspecialidad());

        Doctor actualizado = doctorService.actualizar(id, doctorActualizado);
        return ResponseEntity.ok(mapearAResponse(actualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        doctorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private DoctorResponse mapearAResponse(Doctor doctor) {
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
}
