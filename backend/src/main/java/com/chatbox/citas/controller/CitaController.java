package com.chatbox.citas.controller;

import com.chatbox.citas.dto.CitaRequest;
import com.chatbox.citas.dto.CitaResponse;
import com.chatbox.citas.service.CitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}")
public class CitaController {

    private final CitaService citaService;

    @PostMapping
    public ResponseEntity<CitaResponse> crearCita(@Valid @RequestBody CitaRequest request) {
        CitaResponse response = citaService.crearCita(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/todas")
    public ResponseEntity<List<CitaResponse>> obtenerTodasLasCitas() {
        List<CitaResponse> response = citaService.obtenerTodasLasCitas();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CitaResponse> obtenerCita(@PathVariable Long id) {
        CitaResponse response = citaService.obtenerCitaPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/paciente/{telefono}")
    public ResponseEntity<List<CitaResponse>> obtenerCitasPorPaciente(@PathVariable String telefono) {
        List<CitaResponse> response = citaService.obtenerCitasPorTelefono(telefono);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<CitaResponse> cancelarCita(@PathVariable Long id) {
        CitaResponse response = citaService.cancelarCita(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("API funcionando correctamente");
    }
}
