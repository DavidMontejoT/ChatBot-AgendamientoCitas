package com.chatbox.citas.controller;

import com.chatbox.citas.dto.WhatsAppMessageRequest;
import com.chatbox.citas.service.EmailService;
import com.chatbox.citas.service.whatsapp.WhatsAppOrchestratorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/whatsapp")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}")
public class WhatsAppController {

    private final WhatsAppOrchestratorService whatsAppOrchestrator;
    private final EmailService emailService;

    @PostMapping("/enviar")
    public ResponseEntity<?> enviarMensaje(@Valid @RequestBody WhatsAppMessageRequest request) {
        whatsAppOrchestrator.enviarMensaje(request.getFrom(), request.getMessage());
        return ResponseEntity.ok().body("{\"message\": \"Mensaje enviado correctamente\"}");
    }

    @GetMapping("/webhook")
    public ResponseEntity<String> verificarWebhook(
            @RequestParam(value = "hub.mode", required = false) String mode,
            @RequestParam(value = "hub.verify_token", required = false) String token,
            @RequestParam(value = "hub.challenge", required = false) String challenge) {

        log.info("📥 Webhook verification request - Mode: {}, Token: {}, Challenge: {}", mode, token, challenge);

        if ("subscribe".equals(mode) && whatsAppOrchestrator.verificarToken(token)) {
            log.info("✅ Webhook verification successful");
            return ResponseEntity.ok()
                    .header("Content-Type", "text/plain")
                    .body(challenge);
        }

        log.warn("❌ Webhook verification failed - Mode: {}, Token valid: {}", mode, whatsAppOrchestrator.verificarToken(token));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> recibirWebhook(@RequestBody String payload) {
        log.info("📥 Webhook POST request received");
        log.debug("Payload: {}", payload);
        whatsAppOrchestrator.procesarWebhook(payload);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/test-email")
    public ResponseEntity<?> testEmail(@RequestBody TestEmailRequest request) {
        log.info("🧪 Test email request to: {}", request.getEmail());

        try {
            java.time.LocalDateTime ahora = java.time.LocalDateTime.now();
            emailService.enviarConfirmacionCita(
                request.getEmail(),
                request.getNombre() != null ? request.getNombre() : "Paciente Prueba",
                "PRIMERA VEZ",
                "Dr. Santiago",
                ahora.plusHours(24)
            );

            return ResponseEntity.ok("{\"message\": \"Email de prueba enviado a " + request.getEmail() + "\"}");
        } catch (Exception e) {
            log.error("Error enviando email de prueba: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    public static class TestEmailRequest {
        private String email;
        private String nombre;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
    }
}
