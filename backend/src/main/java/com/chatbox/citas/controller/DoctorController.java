package com.chatbox.citas.controller;

import com.chatbox.citas.dto.DoctorRequest;
import com.chatbox.citas.dto.DoctorResponse;
import com.chatbox.citas.mapper.DoctorMapper;
import com.chatbox.citas.model.Doctor;
import com.chatbox.citas.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctores")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}")
public class DoctorController {

    private final DoctorService doctorService;
    private final DoctorMapper doctorMapper;

    @GetMapping
    public ResponseEntity<List<DoctorResponse>> obtenerTodos() {
        List<Doctor> doctores = doctorService.obtenerTodos();
        List<DoctorResponse> respuestas = doctorMapper.toResponseList(doctores);
        return ResponseEntity.ok(respuestas);
    }

    @GetMapping("/activos")
    public ResponseEntity<List<DoctorResponse>> obtenerActivos() {
        List<Doctor> doctores = doctorService.obtenerActivos();
        List<DoctorResponse> respuestas = doctorMapper.toResponseList(doctores);
        return ResponseEntity.ok(respuestas);
    }

    @GetMapping("/especialidad/{especialidad}")
    public ResponseEntity<List<DoctorResponse>> obtenerPorEspecialidad(
        @PathVariable String especialidad
    ) {
        List<Doctor> doctores = doctorService.obtenerActivosPorEspecialidad(especialidad);
        List<DoctorResponse> respuestas = doctorMapper.toResponseList(doctores);
        return ResponseEntity.ok(respuestas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> obtenerPorId(@PathVariable Long id) {
        Doctor doctor = doctorService.obtenerPorId(id)
            .orElseThrow(() -> new RuntimeException("Doctor no encontrado"));
        return ResponseEntity.ok(doctorMapper.toResponse(doctor));
    }

    @PostMapping
    public ResponseEntity<DoctorResponse> crear(@Valid @RequestBody DoctorRequest request) {
        Doctor doctor = doctorMapper.toEntity(request);
        Doctor guardado = doctorService.guardar(doctor);
        return ResponseEntity.ok(doctorMapper.toResponse(guardado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DoctorResponse> actualizar(
        @PathVariable Long id,
        @Valid @RequestBody DoctorRequest request
    ) {
        Doctor doctorActualizado = doctorMapper.toEntityForUpdate(request);
        Doctor actualizado = doctorService.actualizar(id, doctorActualizado);
        return ResponseEntity.ok(doctorMapper.toResponse(actualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        doctorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
