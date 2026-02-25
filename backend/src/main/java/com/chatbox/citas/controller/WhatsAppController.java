package com.chatbox.citas.controller;

import com.chatbox.citas.dto.WhatsAppMessageRequest;
import com.chatbox.citas.service.WhatsAppService;
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

    private final WhatsAppService whatsAppService;

    @PostMapping("/enviar")
    public ResponseEntity<?> enviarMensaje(@Valid @RequestBody WhatsAppMessageRequest request) {
        whatsAppService.enviarMensaje(request.getFrom(), request.getMessage());
        return ResponseEntity.ok().body("{\"message\": \"Mensaje enviado correctamente\"}");
    }

    @GetMapping("/webhook")
    public ResponseEntity<String> verificarWebhook(
            @RequestParam(value = "hub.verify_token", required = false) String token,
            @RequestParam(value = "hub.challenge", required = false) String challenge) {

        log.info("📥 Webhook verification request - Token: {}, Challenge: {}", token, challenge);

        if (whatsAppService.verificarToken(token)) {
            log.info("✅ Webhook verification successful");
            return ResponseEntity.ok()
                    .header("Content-Type", "text/plain")
                    .body(challenge);
        }

        log.warn("❌ Webhook verification failed - Invalid token");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> recibirWebhook(@RequestBody String payload) {
        log.info("📥 Webhook POST request received");
        log.debug("Payload: {}", payload);
        whatsAppService.procesarWebhook(payload);
        return ResponseEntity.ok().build();
    }
}
