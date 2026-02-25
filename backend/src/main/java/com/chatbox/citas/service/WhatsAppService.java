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
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppService {

    private final WhatsAppConfig config;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    private final CitaService citaService;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    // Estado de conversaciones activas con timestamp
    private final ConcurrentHashMap<String, ConversacionState> conversaciones = new ConcurrentHashMap<>();

    // Enum para estados de conversación
    private enum EstadoConversacion {
        MENU,
        ESPERANDO_NOMBRE,
        ESPERANDO_FECHA,
        ESPERANDO_HORA,
        ESPERANDO_DOCTOR
    }

    // Clase para guardar estado de conversación con timestamp de última actividad
    private static class ConversacionState {
        EstadoConversacion estado;
        String nombre;
        String fecha;
        String hora;
        LocalDateTime lastActivity;

        ConversacionState(EstadoConversacion estado) {
            this.estado = estado;
            this.lastActivity = LocalDateTime.now();
        }

        void updateActivity() {
            this.lastActivity = LocalDateTime.now();
        }

        boolean isExpired(int timeoutMinutes) {
            return lastActivity.plusMinutes(timeoutMinutes).isBefore(LocalDateTime.now());
        }
    }

    public boolean verificarToken(String token) {
        return config.getVerifyToken().equals(token);
    }

    // Limpieza automática de conversaciones expiradas
    public void limpiarConversacionesExpiradas() {
        int antes = conversaciones.size();
        conversaciones.entrySet().removeIf(entry ->
            entry.getValue().isExpired(config.getConversationTimeoutMinutes())
        );
        int despues = conversaciones.size();
        if (antes > despues) {
            log.info("Limpieza de conversaciones: {} eliminadas, {} activas", antes - despues, despues);
        }
    }

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

        // Obtener o crear estado de conversación
        ConversacionState estado = conversaciones.computeIfAbsent(telefono, k -> new ConversacionState(EstadoConversacion.MENU));

        // Actualizar timestamp de actividad
        estado.updateActivity();

        // Limpiar conversaciones expiradas periódicamente
        limpiarConversacionesExpiradas();

        // Procesar según estado actual
        switch (estado.estado) {
            case MENU:
                if (mensajeNormalizado.contains("1") || mensajeNormalizado.contains("APARTAR") || mensajeNormalizado.contains("CITA")) {
                    estado.estado = EstadoConversacion.ESPERANDO_NOMBRE;
                    enviarMensaje(telefono, "¡Perfecto! 👍\n\n¿Cuál es tu nombre completo?");
                } else if (mensajeNormalizado.contains("2") || mensajeNormalizado.contains("ASESOR") || mensajeNormalizado.contains("HABLAR")) {
                    enviarMensaje(telefono, "👨‍💼 Un asesor te contactará pronto.\n\nHorario de atención: Lunes a Viernes de 9:00 AM a 6:00 PM");
                    conversaciones.remove(telefono);
                } else {
                    mostrarMenu(telefono);
                }
                break;

            case ESPERANDO_NOMBRE:
                estado.nombre = mensaje.trim();
                estado.estado = EstadoConversacion.ESPERANDO_FECHA;
                enviarMensaje(telefono, String.format("Gracias %s 👋\n\n¿Para qué día deseas la cita?\n\nEscribe la fecha en formato: dd/mm/yyyy\nEjemplo: 27/02/2026", estado.nombre));
                break;

            case ESPERANDO_FECHA:
                if (validarFecha(mensaje.trim())) {
                    estado.fecha = mensaje.trim();
                    estado.estado = EstadoConversacion.ESPERANDO_HORA;
                    enviarMensaje(telefono, "Perfecto 📅\n\n¿A qué hora deseas la cita?\n\nEscribe la hora en formato: hh:mm\nEjemplo: 10:00");
                } else {
                    enviarMensaje(telefono, "⚠️ Fecha inválida o pasada. Por favor usa el formato dd/mm/yyyy y verifica que sea una fecha futura.\n\nEjemplo: 27/02/2026");
                }
                break;

            case ESPERANDO_HORA:
                if (validarHora(mensaje.trim())) {
                    estado.hora = mensaje.trim();
                    estado.estado = EstadoConversacion.ESPERANDO_DOCTOR;
                    enviarMensaje(telefono, "⏰ Hora registrada\n\n¿Con qué doctor deseas agendar?\n\nEscribe el nombre del doctor.");
                } else {
                    enviarMensaje(telefono, "⚠️ Hora inválida. Por favor usa el formato hh:mm (24 horas)\n\nEjemplo: 10:00 o 15:30");
                }
                break;

            case ESPERANDO_DOCTOR:
                String doctor = mensaje.trim();
                crearCitaCompleta(telefono, estado.nombre, doctor, estado.fecha, estado.hora);
                conversaciones.remove(telefono);
                break;
        }
    }

    private void mostrarMenu(String telefono) {
        String menu = "🏥 *Sistema de Citas Médicas*\n\n" +
                "Selecciona una opción:\n\n" +
                "1️⃣ Apartar cita\n" +
                "2️⃣ Hablar con un asesor\n\n" +
                "Responde con el número o el nombre de la opción";

        enviarMensaje(telefono, menu);
    }

    private boolean validarFecha(String fecha) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            LocalDateTime fechaParsed = LocalDateTime.parse(fecha + " 00:00", formatter);
            return fechaParsed.isAfter(LocalDateTime.now().minusDays(1));
        } catch (Exception e) {
            return false;
        }
    }

    private boolean validarHora(String hora) {
        try {
            String[] partes = hora.split(":");
            if (partes.length != 2) return false;
            int h = Integer.parseInt(partes[0]);
            int m = Integer.parseInt(partes[1]);
            return h >= 0 && h <= 23 && m >= 0 && m <= 59;
        } catch (Exception e) {
            return false;
        }
    }

    private void crearCitaCompleta(String telefono, String nombre, String doctor, String fecha, String hora) {
        try {
            LocalDateTime fechaHora = LocalDateTime.parse(fecha + " " + hora, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

            if (fechaHora.isBefore(LocalDateTime.now())) {
                enviarMensaje(telefono, "⚠️ La fecha y hora deben ser futuras. Por favor inicia nuevamente.");
                return;
            }

            CitaRequest request = new CitaRequest();
            request.setNombrePaciente(nombre);
            request.setTelefono(telefono);
            request.setEmail("");
            request.setDoctor(doctor);
            request.setFechaHora(fechaHora);

            citaService.crearCita(request);
            enviarConfirmacionCita(telefono, nombre, fecha, hora, doctor);

            log.info("Cita creada exitosamente para {} via WhatsApp conversacional", nombre);

        } catch (Exception e) {
            log.error("Error creando cita: {}", e.getMessage(), e);
            enviarMensaje(telefono, "❌ Hubo un error al crear tu cita. Por favor intenta nuevamente escribiendo cualquier mensaje.");
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
