package com.chatbox.citas.constants;

/**
 * Constants for Email integration
 * Centralizes email-related configuration and templates
 */
public final class EmailConstants {

    // Clinic information
    public static final String NOMBRE_CLINICA = "Sociedad Urológica del Cauca";
    public static final String TELEFONO_CONTACTO = "3013188696";

    // Email subjects
    public static final String ASUNTO_CONFIRMACION_CITA = "Confirmación de Cita Médica";
    public static final String ASUNTO_RECORDATORIO_CITA = "⏰ Recordatorio de Cita Médica";
    public static final String ASUNTO_CANCELACION_CITA = "Cancelación de Cita";

    // Copyright
    public static final String COPYRIGHT = "© 2024 Sociedad Urológica del Cauca. Todos los derechos reservados.";

    // CSS colors for emails
    public static final String COLOR_PRIMARY = "#2c3e50";
    public static final String COLOR_SUCCESS = "#28a745";
    public static final String COLOR_WARNING = "#ffc107";
    public static final String COLOR_DANGER = "#e74c3c";
    public static final String COLOR_INFO = "#17a2b8";
    public static final String COLOR_LIGHT = "#ecf0f1";
    public static final String COLOR_BACKGROUND = "#f8f9fa";

    private EmailConstants() {
        // Utility class - prevent instantiation
    }
}
