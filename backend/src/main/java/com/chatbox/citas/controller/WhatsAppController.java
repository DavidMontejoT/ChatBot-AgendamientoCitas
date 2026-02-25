package com.chatbox.citas.controller;

import com.chatbox.citas.dto.WhatsAppMessageRequest;
import com.chatbox.citas.service.WhatsAppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam(value = "hub.verify_token") String token,
            @RequestParam(value = "hub.challenge") String challenge) {

        if (whatsAppService.verificarToken(token)) {
            return ResponseEntity.ok()
                    .header("Content-Type", "text/plain")
                    .body(challenge);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> recibirWebhook(@RequestBody String payload) {
        whatsAppService.procesarWebhook(payload);
        return ResponseEntity.ok().build();
    }
}
