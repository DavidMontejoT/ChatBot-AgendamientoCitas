package com.chatbox.citas.service.whatsapp;

import com.chatbox.citas.constants.WhatsAppConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service responsible for managing conversation state and timeouts
 * Handles conversation lifecycle, expiration, and cleanup
 */
@Slf4j
@Service
public class ConversationStateService {

    // Active conversations with timestamps
    private final ConcurrentHashMap<String, ConversacionState> conversaciones = new ConcurrentHashMap<>();

    // Processed message IDs for deduplication (expire after 5 minutes)
    private final ConcurrentHashMap<String, Long> mensajesProcesados = new ConcurrentHashMap<>();

    /**
     * Get or create a conversation state for a phone number
     */
    public ConversacionState getOrCreateConversacion(String telefono) {
        ConversacionState estado = conversaciones.computeIfAbsent(
            telefono,
            k -> new ConversacionState(EstadoConversacion.MENU)
        );
        estado.updateActivity();
        return estado;
    }

    /**
     * Get existing conversation state without updating activity
     */
    public Optional<ConversacionState> getConversacion(String telefono) {
        return Optional.ofNullable(conversaciones.get(telefono));
    }

    /**
     * Remove a conversation state
     */
    public void removeConversacion(String telefono) {
        conversaciones.remove(telefono);
    }

    /**
     * Check if a message has already been processed
     * @return true if the message should be processed (not duplicate), false otherwise
     */
    public boolean shouldProcessMessage(String messageId) {
        Long tiempoProcesado = mensajesProcesados.get(messageId);
        long ahora = System.currentTimeMillis();

        if (tiempoProcesado != null && (ahora - tiempoProcesado) < WhatsAppConstants.MESSAGE_EXPIRE_MS) {
            log.info("⚠️ Mensaje duplicado ignorado: {}", messageId);
            return false;
        }

        // Mark message as processed
        mensajesProcesados.put(messageId, ahora);
        return true;
    }

    /**
     * Clean up expired conversations
     * @return number of conversations removed
     */
    public int limpiarConversacionesExpiradas() {
        int antes = conversaciones.size();
        int timeoutMinutes = (int) (WhatsAppConstants.CONVERSATION_TIMEOUT_MS / 60000);

        conversaciones.entrySet().removeIf(entry ->
            entry.getValue().isExpired(timeoutMinutes)
        );

        int despues = conversaciones.size();
        int eliminadas = antes - despues;

        if (eliminadas > 0) {
            log.info("Limpieza de conversaciones: {} eliminadas, {} activas", eliminadas, despues);
        }

        return eliminadas;
    }

    /**
     * Clean up old processed messages
     */
    public void limpiarMensajesProcesados() {
        long ahora = System.currentTimeMillis();
        mensajesProcesados.entrySet().removeIf(entry ->
            (ahora - entry.getValue()) > WhatsAppConstants.MESSAGE_EXPIRE_MS
        );
    }

    /**
     * Get count of active conversations
     */
    public int getActiveConversationsCount() {
        return conversaciones.size();
    }

    /**
     * Get count of processed messages in memory
     */
    public int getProcessedMessagesCount() {
        return mensajesProcesados.size();
    }

    // ==================== CONVERSATION STATE CLASSES ====================

    /**
     * Enum for conversation states
     */
    public enum EstadoConversacion {
        MENU,                               // Step 1
        ESPERANDO_TIPO_DOC,                 // Step 2
        ESPERANDO_NUMERO_DOC,               // Step 3
        ESPERANDO_NOMBRE,                   // Step 4
        ESPERANDO_TELEFONO_PRINCIPAL,       // Step 5
        ESPERANDO_TELEFONO_SECUNDARIO,      // Step 6
        ESPERANDO_DIRECCION,                // Step 7
        ESPERANDO_FECHA_NACIMIENTO,         // Step 8
        ESPERANDO_EPS,                      // Step 9
        ESPERANDO_TIPO_CITA,                // Step 10
        ESPERANDO_FECHA_CITA,               // Step 11
        ESPERANDO_SELECCION_DOCTOR,         // Step 12 - Doctor selection
        ESPERANDO_EMAIL,                    // Step 13 - Patient email
        CONFIRMACION_FINAL                  // Step 14 - Final confirmation
    }

    /**
     * Class to store conversation state with timestamp and history
     */
    public static class ConversacionState {
        private EstadoConversacion estado;
        private LocalDateTime lastActivity;

        // Patient fields
        private String tipoIdentificacion;
        private String numeroIdentificacion;
        private String nombre;
        private String telefonoPrincipal;
        private String telefonoSecundario;
        private String direccion;
        private LocalDate fechaNacimiento;
        private String eps;
        private String email;

        // Appointment fields
        private String tipoCita;
        private LocalDate fechaCita;
        private String horaCita;
        private String doctor;
        private List<OpcionDoctor> opcionesDoctor;

        // Navigation stack for "back" functionality
        private final Stack<EstadoConversacion> historialEstados = new Stack<>();

        public ConversacionState(EstadoConversacion estado) {
            this.estado = estado;
            this.lastActivity = LocalDateTime.now();
        }

        public void updateActivity() {
            this.lastActivity = LocalDateTime.now();
        }

        public boolean isExpired(int timeoutMinutes) {
            return lastActivity.plusMinutes(timeoutMinutes).isBefore(LocalDateTime.now());
        }

        public void guardarEstadoEnHistorial() {
            if (estado != EstadoConversacion.MENU) {
                historialEstados.push(estado);
            }
        }

        public EstadoConversacion volverEstadoAnterior() {
            return historialEstados.isEmpty() ?
                EstadoConversacion.MENU : historialEstados.pop();
        }

        // Getters and setters
        public EstadoConversacion getEstado() { return estado; }
        public void setEstado(EstadoConversacion estado) { this.estado = estado; }

        public String getTipoIdentificacion() { return tipoIdentificacion; }
        public void setTipoIdentificacion(String tipoIdentificacion) { this.tipoIdentificacion = tipoIdentificacion; }

        public String getNumeroIdentificacion() { return numeroIdentificacion; }
        public void setNumeroIdentificacion(String numeroIdentificacion) { this.numeroIdentificacion = numeroIdentificacion; }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getTelefonoPrincipal() { return telefonoPrincipal; }
        public void setTelefonoPrincipal(String telefonoPrincipal) { this.telefonoPrincipal = telefonoPrincipal; }

        public String getTelefonoSecundario() { return telefonoSecundario; }
        public void setTelefonoSecundario(String telefonoSecundario) { this.telefonoSecundario = telefonoSecundario; }

        public String getDireccion() { return direccion; }
        public void setDireccion(String direccion) { this.direccion = direccion; }

        public LocalDate getFechaNacimiento() { return fechaNacimiento; }
        public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

        public String getEps() { return eps; }
        public void setEps(String eps) { this.eps = eps; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getTipoCita() { return tipoCita; }
        public void setTipoCita(String tipoCita) { this.tipoCita = tipoCita; }

        public LocalDate getFechaCita() { return fechaCita; }
        public void setFechaCita(LocalDate fechaCita) { this.fechaCita = fechaCita; }

        public String getHoraCita() { return horaCita; }
        public void setHoraCita(String horaCita) { this.horaCita = horaCita; }

        public String getDoctor() { return doctor; }
        public void setDoctor(String doctor) { this.doctor = doctor; }

        public List<OpcionDoctor> getOpcionesDoctor() { return opcionesDoctor; }
        public void setOpcionesDoctor(List<OpcionDoctor> opcionesDoctor) { this.opcionesDoctor = opcionesDoctor; }
    }

    /**
     * Record for doctor selection options
     */
    public record OpcionDoctor(String doctor, String hora, String especialidad) {}
}
