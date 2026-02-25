package com.chatbox.citas.service;

import com.chatbox.citas.config.WhatsAppConfig;
import com.chatbox.citas.dto.CitaRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppService {

    private final WhatsAppConfig config;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    private final CitaService citaService;
    private static final String VERIFY_TOKEN = "chatbox_verify_token_2024";
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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
        }
    }

    public void enviarConfirmacionCita(String telefono, String nombrePaciente, String fecha, String hora, String doctor) {
        String mensaje = String.format(
                "¡Hola %s! ✅ Tu cita ha sido agendada correctamente.\n\n📅 Fecha: %s\n⏰ Hora: %s\n👨‍⚕️ Doctor: %s\n\nTe enviaremos recordatorios antes de tu cita. ¡No olvides asistir!",
                nombrePaciente, fecha, hora, doctor
        );
        enviarMensaje(telefono, mensaje);
    }

    public void enviarRecordatorio(String telefono, String nombrePaciente, String fecha, String hora, String doctor, int horasAntes) {
        String mensaje = String.format(
                "¡Hola %s! ⏰ Recordatorio de cita\n\n📅 Fecha: %s\n⏰ Hora: %s\n👨‍⚕️ Doctor: %s\n\n%s",
                nombrePaciente,
                fecha,
                hora,
                doctor,
                horasAntes == 24 ? "Tu cita es mañana. ¡Te esperamos!" : "Tu cita es en 1 hora. ¡Te esperamos pronto!"
        );
        enviarMensaje(telefono, mensaje);
    }

    public void enviarConfirmacionCancelacion(String telefono, String fecha, String hora) {
        String mensaje = String.format(
                "Tu cita del %s a las %s ha sido cancelada. Si deseas reagendar, contáctanos.",
                fecha, hora
        );
        enviarMensaje(telefono, mensaje);
    }

    public boolean verificarToken(String token) {
        return VERIFY_TOKEN.equals(token);
    }

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
                        String from = message.path("from").asText();
                        String text = message.path("text").path("body").asText();

                        log.info("Mensaje recibido de {}: {}", from, text);

                        procesarMensajeRecibido(from, text);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error procesando webhook: {}", e.getMessage(), e);
        }
    }

    private void procesarMensajeRecibido(String telefono, String mensaje) {
        log.info("Procesando mensaje de {}: {}", telefono, mensaje);

        String mensajeNormalizado = mensaje.trim().toUpperCase();

        // Formato estructurado: CITA: Nombre|Doctor|Fecha|Hora
        if (mensajeNormalizado.startsWith("CITA:")) {
            procesarCitaEstructurada(telefono, mensajeNormalizado);
            return;
        }

        // Palabras clave para mostrar instrucciones
        if (mensajeNormalizado.contains("CITA") || mensajeNormalizado.contains("AGENDAR")) {
            enviarMensaje(telefono,
                    "¡Hola! 👋 Para agendar una cita rápidamente, usa este formato:\n\n" +
                            "CITA: Tu Nombre|Doctor|dd/mm/yyyy|hh:mm\n\n" +
                            "Ejemplo: CITA: Juan Pérez|Dr. García|26/02/2026|15:30\n\n" +
                            "O visita nuestro portal web para agendar.");
        } else {
            enviarMensaje(telefono,
                    "Gracias por tu mensaje. Para agendar una cita, por favor usa la palabra 'cita' " +
                            "o visita nuestro portal web.");
        }
    }

    private void procesarCitaEstructurada(String telefono, String mensaje) {
        try {
            // Eliminar "CITA:" y dividir por "|"
            String contenido = mensaje.substring(5).trim();
            String[] partes = contenido.split("\\|");

            if (partes.length != 4) {
                enviarMensaje(telefono,
                        "⚠️ Formato incorrecto. Usa:\nCITA: Nombre|Doctor|dd/mm/yyyy|hh:mm\n\n" +
                        "Ejemplo: CITA: Juan Pérez|Dr. García|26/02/2026|15:30");
                return;
            }

            String nombre = partes[0].trim();
            String doctor = partes[1].trim();
            String fecha = partes[2].trim();
            String hora = partes[3].trim();

            // Parsear fecha y hora
            LocalDateTime fechaHora;
            try {
                fechaHora = LocalDateTime.parse(fecha + " " + hora, FORMATO_FECHA_HORA);
            } catch (DateTimeParseException e) {
                enviarMensaje(telefono,
                        "⚠️ Fecha u hora inválida. Usa formato dd/mm/yyyy y hh:mm\n" +
                        "Ejemplo: CITA: Juan Pérez|Dr. García|26/02/2026|15:30");
                return;
            }

            // Validar que la fecha sea futura
            if (fechaHora.isBefore(LocalDateTime.now())) {
                enviarMensaje(telefono,
                        "⚠️ La fecha debe ser futura. Por favor selecciona una fecha y hora posterior a ahora.");
                return;
            }

            // Crear la cita
            CitaRequest request = new CitaRequest();
            request.setNombrePaciente(nombre);
            request.setTelefono(telefono);
            request.setEmail(""); // Email opcional
            request.setDoctor(doctor);
            request.setFechaHora(fechaHora);

            citaService.crearCita(request);

            // Enviar confirmación
            String fechaFormateada = fechaHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String horaFormateada = fechaHora.format(DateTimeFormatter.ofPattern("HH:mm"));
            enviarConfirmacionCita(telefono, nombre, fechaFormateada, horaFormateada, doctor);

            log.info("Cita creada exitosamente para {} via WhatsApp", nombre);

        } catch (Exception e) {
            log.error("Error procesando cita estructurada: {}", e.getMessage(), e);
            enviarMensaje(telefono,
                    "❌ Hubo un error al procesar tu cita. Por favor intenta nuevamente o contacta al servicio.");
        }
    }

    private String formatearTelefono(String telefono) {
        if (!telefono.startsWith("+")) {
            return "+52" + telefono;
        }
        return telefono;
    }

    private String escaparJson(String texto) {
        return texto.replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
