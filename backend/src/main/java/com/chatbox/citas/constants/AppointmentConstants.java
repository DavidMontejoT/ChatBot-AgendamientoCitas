package com.chatbox.citas.constants;

/**
 * Constants for Appointment management
 * Centralizes appointment-related configuration
 */
public final class AppointmentConstants {

    // Appointment durations (in minutes)
    public static final int DURACION_CITA_PRIMERA_VEZ = 30;
    public static final int DURACION_CITA_CONTROL = 20;
    public static final int DURACION_CITA_POR_DEFECTO = 30;

    // Appointment types
    public static final String TIPO_CITA_PRIMERA_VEZ = "PRIMERA VEZ";
    public static final String TIPO_CITA_CONTROL = "CONTROL";

    // Business hours
    public static final String HORA_INICIO = "08:00";
    public static final String HORA_FIN = "17:00";
    public static final int HORA_INICIO_HORAS = 8;
    public static final int HORA_FIN_HORAS = 17;

    // Available time slots
    public static final String[] HORARIOS_DISPONIBLES = {
        "08:00", "09:00", "10:00", "11:00", "14:00", "15:00", "16:00", "17:00"
    };

    // Reminder timing (in hours)
    public static final int RECORDATORIO_24H = 24;
    public static final int RECORDATORIO_1H = 1;

    private AppointmentConstants() {
        // Utility class - prevent instantiation
    }
}
