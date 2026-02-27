package com.chatbox.citas.service.whatsapp;

import com.chatbox.citas.config.WhatsAppConfig;
import com.chatbox.citas.constants.WhatsAppConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Orchestrator service for WhatsApp integration
 * Coordinates all WhatsApp-related services and handles webhook processing
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppOrchestratorService {

    private final WhatsAppConfig config;
    private final ConversationStateService conversationStateService;
    private final WhatsAppMessageService messageService;
    private final WhatsAppTemplateService templateService;
    private final WhatsAppFlowService flowService;
    private final ObjectMapper objectMapper;

    /**
     * Verify webhook token
     * @param token Token to verify
     * @return true if token is valid
     */
    public boolean verificarToken(String token) {
        return config.getVerifyToken().equals(token);
    }

    /**
     * Process incoming webhook payload from WhatsApp
     * @param payload JSON payload from WhatsApp webhook
     */
    public void procesarWebhook(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);

            JsonNode entry = root.path("entry");
            if (entry.isArray() && entry.size() > 0) {
                JsonNode changes = entry.get(0).path("changes");
                if (changes.isArray() && changes.size() > 0) {
                    JsonNode value = changes.get(0).path("value");
                    JsonNode messages = value.path("messages");

                    if (messages.isArray() && messages.size() > 0) {
                        JsonNode message = messages.get(0);
                        String messageId = message.path("id").asText();
                        String from = message.path("from").asText();
                        String text = message.path("text").path("body").asText();

                        // Check if message was already processed (deduplication)
                        if (!conversationStateService.shouldProcessMessage(messageId)) {
                            return;
                        }

                        // Clean up old processed messages
                        conversationStateService.limpiarMensajesProcesados();

                        log.info("Mensaje recibido de {}: {}", from, text);

                        // Process the message through the flow service
                        flowService.procesarMensaje(from, text);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error procesando webhook: {}", e.getMessage(), e);
            throw new RuntimeException("Error procesando webhook de WhatsApp", e);
        }
    }

    /**
     * Send appointment confirmation message
     */
    public void enviarConfirmacionCita(String telefono, String nombrePaciente, String fecha, String hora, String doctor) {
        String mensaje = templateService.generarConfirmacionCita(nombrePaciente, fecha, hora, doctor);
        messageService.enviarMensaje(telefono, mensaje);
    }

    /**
     * Send appointment reminder message
     */
    public void enviarRecordatorio(String telefono, String nombrePaciente, String fecha, String hora, String doctor, int horasAntes) {
        String mensaje = templateService.generarRecordatorioCita(nombrePaciente, fecha, hora, doctor, horasAntes);
        messageService.enviarMensaje(telefono, mensaje);
    }

    /**
     * Send cancellation confirmation message
     */
    public void enviarConfirmacionCancelacion(String telefono, String fecha, String hora) {
        String mensaje = templateService.generarConfirmacionCancelacion(fecha, hora);
        messageService.enviarMensaje(telefono, mensaje);
    }

    /**
     * Send a generic message
     */
    public void enviarMensaje(String telefono, String mensaje) {
        messageService.enviarMensaje(telefono, mensaje);
    }

    /**
     * Clean up expired conversations (should be called periodically)
     */
    public int limpiarConversacionesExpiradas() {
        return conversationStateService.limpiarConversacionesExpiradas();
    }

    /**
     * Get statistics about current state
     */
    public WhatsAppStats getStats() {
        return new WhatsAppStats(
            conversationStateService.getActiveConversationsCount(),
            conversationStateService.getProcessedMessagesCount()
        );
    }

    /**
     * Record for WhatsApp statistics
     */
    public record WhatsAppStats(int activeConversations, int processedMessages) {}
}
