package com.chatbox.citas.constants;

/**
 * Constants for WhatsApp integration
 * Centralizes all magic numbers and hard-coded strings
 */
public final class WhatsAppConstants {

    // Default doctor
    public static final String DOCTOR_POR_DEFECTO = "Dr. Disponible";

    // Clinic name
    public static final String NOMBRE_CLINICA = "Sociedad Urológica del Cauca";

    // Timeouts (in milliseconds)
    public static final long CONVERSATION_TIMEOUT_MS = 300000L; // 5 minutes
    public static final long MESSAGE_EXPIRE_MS = 300000L; // 5 minutes
    public static final int CLEANUP_INTERVAL_MINUTES = 5;

    // Phone
    public static final String TELEFONO_WHATSAPP = "573013188696";

    // Date/Time formats
    public static final String FORMATO_FECHA_PATTERN = "dd/MM/yyyy";
    public static final String FORMATO_HORA_PATTERN = "HH:mm";
    public static final String FORMATO_FECHA_INPUT = "dd-MM-yyyy";

    // Message templates
    public static final String MENU_TEMPLATE = """
        🏥 *Sociedad Urológica del Cauca*

        Selecciona una opción:

        1️⃣ Agendar Cita
        2️⃣ Cirugía y Procedimientos

        _Comandos disponibles: ATRÁS, CANCELAR, INICIO_
        """;

    public static final String CONFIRMACION_CITA_TEMPLATE = """
        ¡Hola %s! ✅ Tu cita ha sido agendada correctamente.

        📅 Fecha: %s
        ⏰ Hora: %s
        👨‍⚕️ Doctor: %s

        Te enviaremos recordatorios antes de tu cita. ¡No olvides asistir!
        """;

    public static final String RECORDATORIO_CITA_TEMPLATE = """
        ¡Hola %s! ⏰ Recordatorio de cita

        📅 Fecha: %s
        ⏰ Hora: %s
        👨‍⚕️ Doctor: %s

        %s
        """;

    public static final String CONFIRMACION_CANCELACION_TEMPLATE = """
        Tu cita del %s a las %s ha sido cancelada. Si deseas reagendar, contáctanos.
        """;

    // Validation messages
    public static final String MENSAJE_OPCION_INVALIDA = "⚠️ Opción inválida.";
    public static final String MENSAJE_ERROR_GENERAL = "❌ Hubo un error. Por favor intenta nuevamente.";

    private WhatsAppConstants() {
        // Utility class - prevent instantiation
    }
}
