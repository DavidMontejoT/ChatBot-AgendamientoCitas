package com.chatbox.citas.service.email;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Service for sending emails via Brevo HTTP API
 * Alternative to SMTP that works in platforms with port restrictions (like Render free tier)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BrevoEmailApiService {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${brevo.api.key:}")
    private String brevoApiKey;

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    /**
     * Check if Brevo API is configured
     */
    public boolean isConfigured() {
        boolean configurado = brevoApiKey != null && !brevoApiKey.isBlank();
        if (!configurado) {
            log.warn("⚠️ Brevo API NO configurada - Verifica variable BREVO_API_KEY");
        }
        return configurado;
    }

    /**
     * Send email via Brevo HTTP API
     */
    public void sendEmail(
        String toEmail,
        String toName,
        String subject,
        String htmlContent,
        String fromEmail,
        String fromName
    ) {
        if (!isConfigured()) {
            log.warn("⚠️ Email NO enviado - Brevo API no configurada");
            return;
        }

        if (toEmail == null || toEmail.isBlank()) {
            log.warn("No se envía email: dirección vacía");
            return;
        }

        try {
            log.info("📧 Enviando email via Brevo API a: {}", toEmail);

            // Build request body according to Brevo API v3 specification
            Map<String, Object> requestBody = Map.of(
                "sender", Map.of(
                    "email", fromEmail,
                    "name", fromName
                ),
                "to", List.of(
                    Map.of(
                        "email", toEmail,
                        "name", toName != null ? toName : toEmail
                    )
                ),
                "subject", subject,
                "htmlContent", htmlContent
            );

            // Create headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey);

            // Create request entity
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // Send request
            ResponseEntity<String> response = restTemplate.exchange(
                BREVO_API_URL,
                HttpMethod.POST,
                requestEntity,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Email enviado exitosamente a {} via Brevo API - Response: {}",
                    toEmail, response.getStatusCode());
            } else {
                log.warn("⚠️ Respuesta inesperada de Brevo API: {}", response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ Error enviando email via Brevo API a {}: {}",
                toEmail, e.getMessage(), e);
            throw new RuntimeException("Error enviando email via Brevo API", e);
        }
    }

    /**
     * Send HTML email (convenience method)
     */
    public void sendHtmlEmail(
        String toEmail,
        String toName,
        String subject,
        String htmlContent
    ) {
        sendEmail(
            toEmail,
            toName,
            subject,
            htmlContent,
            "davidmontejotorres5@gmail.com", // from email
            "Sociedad Urológica del Cauca"    // from name
        );
    }
}
