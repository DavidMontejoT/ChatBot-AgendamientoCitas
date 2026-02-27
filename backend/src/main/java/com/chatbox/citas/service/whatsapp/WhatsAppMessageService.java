package com.chatbox.citas.service.whatsapp;

import com.chatbox.citas.config.WhatsAppConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Service responsible for WhatsApp API communication
 * Handles all message sending operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppMessageService {

    private final WhatsAppConfig config;
    private final WebClient.Builder webClientBuilder;

    /**
     * Send a text message to a WhatsApp number
     * @param telefono Phone number
     * @param mensaje Message content
     */
    public void enviarMensaje(String telefono, String mensaje) {
        try {
            String telefonoFormateado = formatearTelefono(telefono);

            String requestBody = String.format(
                "{\"messaging_product\": \"whatsapp\", \"to\": \"%s\", \"type\": \"text\", \"text\": {\"body\": \"%s\"}}",
                telefonoFormateado,
                escaparJson(mensaje)
            );

            WebClient webClient = webClientBuilder
                .baseUrl(config.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + config.getApiToken())
                .build();

            String response = webClient.post()
                .uri(config.getApiUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(
                    status -> status.is4xxClientError() || status.is5xxServerError(),
                    clientResponse -> {
                        log.error("Error enviando mensaje a WhatsApp: {}", clientResponse.statusCode());
                        return Mono.empty();
                    }
                )
                .bodyToMono(String.class)
                .block();

            log.info("Mensaje enviado a {}: {}", telefono, response);

        } catch (Exception e) {
            log.error("Error enviando mensaje de WhatsApp: {}", e.getMessage(), e);
            throw new RuntimeException("Error enviando mensaje a WhatsApp", e);
        }
    }

    /**
     * Format phone number to international format
     * @param telefono Phone number
     * @return Formatted phone number with + prefix
     */
    private String formatearTelefono(String telefono) {
        // If already starts with +, return as is
        if (telefono.startsWith("+")) {
            return telefono;
        }

        // Detect country prefix and add corresponding +
        // Common prefixes for Spanish-speaking countries:
        // 57 = Colombia, 52 = Mexico, 51 = Peru, 56 = Chile, etc.
        String[] prefijosPais = {"57", "52", "51", "56", "54", "58", "34", "39"};

        for (String prefijo : prefijosPais) {
            if (telefono.startsWith(prefijo)) {
                return "+" + telefono;
            }
        }

        // If no known prefix detected, assume missing +
        return "+" + telefono;
    }

    /**
     * Escape special characters for JSON
     * @param texto Text to escape
     * @return Escaped text
     */
    private String escaparJson(String texto) {
        return texto.replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r");
    }
}
