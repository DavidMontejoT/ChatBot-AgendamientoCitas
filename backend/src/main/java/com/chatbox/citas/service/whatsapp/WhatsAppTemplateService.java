package com.chatbox.citas.service.whatsapp;

import com.chatbox.citas.constants.WhatsAppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Service responsible for message formatting and templates
 * Provides consistent message formatting across the application
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppTemplateService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern(WhatsAppConstants.FORMATO_FECHA_PATTERN);
    private static final DateTimeFormatter FORMATO_FECHA_INPUT = DateTimeFormatter.ofPattern(WhatsAppConstants.FORMATO_FECHA_INPUT);

    /**
     * Generate main menu message
     */
    public String generarMenu() {
        return WhatsAppConstants.MENU_TEMPLATE;
    }

    /**
     * Generate appointment confirmation message
     */
    public String generarConfirmacionCita(String nombrePaciente, String fecha, String hora, String doctor) {
        return String.format(
            WhatsAppConstants.CONFIRMACION_CITA_TEMPLATE,
            nombrePaciente, fecha, hora, doctor
        );
    }

    /**
     * Generate appointment reminder message
     */
    public String generarRecordatorioCita(String nombrePaciente, String fecha, String hora, String doctor, int horasAntes) {
        String mensajeExtra = horasAntes == 24
            ? "Tu cita es mañana. ¡Te esperamos!"
            : "Tu cita es en 1 hora. ¡Te esperamos pronto!";

        return String.format(
            WhatsAppConstants.RECORDATORIO_CITA_TEMPLATE,
            nombrePaciente,
            fecha,
            hora,
            doctor,
            mensajeExtra
        );
    }

    /**
     * Generate cancellation confirmation message
     */
    public String generarConfirmacionCancelacion(String fecha, String hora) {
        return String.format(
            WhatsAppConstants.CONFIRMACION_CANCELACION_TEMPLATE,
            fecha,
            hora
        );
    }

    /**
     * Generate appointment summary message
     */
    public String generarResumenCita(ConversationStateService.ConversacionState estado) {
        return String.format("""
            ✅ *Resumen de tu Cita*

            📌 *Datos del Paciente:*
            📋 %s: %s
            👤 Nombre: %s
            📱 Teléfono: %s
            %s
            📍 Dirección: %s
            📅 Fecha Nacimiento: %s
            🏥 EPS: %s

            📌 *Datos de la Cita:*
            👨‍⚕️ Tipo: %s
            📅 Fecha: %s
            ⏰ Hora: %s
            👨‍⚕️ Doctor: %s

            ---
            ¿Confirmas esta cita?

            1️⃣ SÍ - Confirmar
            2️⃣ NO - Cancelar

            Responde con el número de opción
            """,
            estado.getTipoIdentificacion(),
            estado.getNumeroIdentificacion(),
            estado.getNombre(),
            estado.getTelefonoPrincipal(),
            estado.getTelefonoSecundario() != null ? "📱 Teléfono 2: " + estado.getTelefonoSecundario() : "",
            estado.getDireccion(),
            estado.getFechaNacimiento().format(FORMATO_FECHA),
            estado.getEps(),
            estado.getTipoCita(),
            estado.getFechaCita().format(FORMATO_FECHA),
            estado.getHoraCita(),
            estado.getDoctor()
        );
    }

    /**
     * Generate prompt for current conversation state
     */
    public String generarPromptActual(ConversationStateService.EstadoConversacion estado) {
        return switch (estado) {
            case ESPERANDO_TIPO_DOC -> "📋 Responde CC, TI o RC";
            case ESPERANDO_NUMERO_DOC -> "📝 Escribe tu número de documento:";
            case ESPERANDO_NOMBRE -> "👤 Escribe tu nombre completo:";
            case ESPERANDO_TELEFONO_PRINCIPAL -> "📱 Escribe tu teléfono principal (10 dígitos):";
            case ESPERANDO_TELEFONO_SECUNDARIO -> "📱 Escribe teléfono secundario o OMITIR:";
            case ESPERANDO_DIRECCION -> "📍 Escribe tu dirección completa:";
            case ESPERANDO_FECHA_NACIMIENTO -> "📅 Escribe tu fecha de nacimiento (dd-mm-yyyy):";
            case ESPERANDO_EPS -> "🏥 Escribe tu EPS:";
            case ESPERANDO_TIPO_CITA -> "👨‍⚕️ 1. PRIMERA VEZ o 2. CONTROL:";
            case ESPERANDO_FECHA_CITA -> "📅 Escribe la fecha de la cita (dd-mm-yyyy):";
            case ESPERANDO_SELECCION_DOCTOR -> "⏰ Responde con el número de doctor seleccionado";
            default -> "Enviando menú principal...";
        };
    }

    /**
     * Generate document type prompt
     */
    public String generarPromptTipoDocumento() {
        return """
            📄 Vamos a iniciar el agendamiento de tu cita.

            Primero, selecciona tu tipo de documento:

            📋 CC - Cédula de Ciudadanía
            📋 TI - Tarjeta de Identidad
            📋 RC - Registro Civil

            Responde con las siglas (CC, TI o RC)
            """;
    }

    /**
     * Generate document number prompt
     */
    public String generarPromptNumeroDocumento(String tipoDoc) {
        return String.format("📝 Escribe tu número de %s sin puntos ni guiones:", tipoDoc);
    }

    /**
     * Generate name prompt
     */
    public String generarPromptNombre() {
        return """
            📱 Escribe tu teléfono principal (10 dígitos):

            Formato: 300 XXX XXXX
            """;
    }

    /**
     * Generate primary phone prompt
     */
    public String generarPromptTelefonoPrincipal() {
        return """
            📱 Escribe tu teléfono principal (10 dígitos):

            Formato: 300 XXX XXXX
            """;
    }

    /**
     * Generate secondary phone prompt
     */
    public String generarPromptTelefonoSecundario() {
        return """
            📱 Escribe un teléfono secundario de contacto (opcional):

            Formato: 300 XXX XXXX
            O escribe OMITIR para continuar
            """;
    }

    /**
     * Generate address prompt
     */
    public String generarPromptDireccion() {
        return """
            📍 Escribe tu dirección completa:

            Ejemplo: Calle 123 #45-67, Barrio Centro
            """;
    }

    /**
     * Generate birthdate prompt
     */
    public String generarPromptFechaNacimiento() {
        return """
            📅 Escribe tu fecha de nacimiento:

            Formato: dd-mm-yyyy
            Ejemplo: 15-06-1990

            ⚠️ Debes ser mayor de 18 años
            """;
    }

    /**
     * Generate EPS prompt
     */
    public String generarPromptEPS() {
        return """
            🏥 Escribe tu EPS (Entidad Promotora de Salud):

            Ejemplo: EPS Sura, Coomeva, Salud Total, etc.
            """;
    }

    /**
     * Generate appointment type prompt
     */
    public String generarPromptTipoCita() {
        return """
            👨‍⚕️ ¿Qué tipo de cita necesitas?

            1️⃣ PRIMERA VEZ
            2️⃣ CONTROL

            Responde con el número de opción
            """;
    }

    /**
     * Generate appointment date prompt
     */
    public String generarPromptFechaCita() {
        return """
            📅 ¿Para qué fecha deseas la cita?

            Formato: dd-mm-yyyy
            Ejemplo: 15-03-2026

            ⚠️ La fecha debe ser futura
            """;
    }

    /**
     * Generate available hours message
     */
    public String generarHorariosDisponibles() {
        return """
            1️⃣ 08:00 AM
            2️⃣ 09:00 AM
            3️⃣ 10:00 AM
            4️⃣ 11:00 AM
            5️⃣ 02:00 PM
            6️⃣ 03:00 PM
            7️⃣ 04:00 PM
            8️⃣ 05:00 PM
            """;
    }

    /**
     * Generate email prompt
     */
    public String generarPromptEmail() {
        return """
            📧 Para enviarte la confirmación de tu cita, por favor proporciona tu correo electrónico:

            Ejemplo: tu.email@gmail.com

            _Escribe OMITIR si no tienes correo electrónico_
            """;
    }

    /**
     * Generate surgery info message
     */
    public String generarInfoCirugia() {
        return """
            👨‍⚕️ Un especialista te contactará pronto para darte información sobre cirugías y procedimientos.

            Horario de atención: Lunes a Viernes de 9:00 AM a 6:00 PM
            Teléfono: 3013188696
            """;
    }

    /**
     * Generate doctor selection message
     */
    public String generarSeleccionDoctor(
        java.time.LocalDate fechaCita,
        java.util.List<ConversationStateService.OpcionDoctor> opciones
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("✅ Estas son las citas más próximas en la Sociedad Urológica del Cauca para el %s:\n\n",
            fechaCita.format(FORMATO_FECHA_INPUT)));

        int opcion = 1;
        for (ConversationStateService.OpcionDoctor item : opciones) {
            sb.append(String.format("%d. Dr. %s - %s - %s\n",
                opcion++, item.doctor(), item.especialidad(), item.hora()));
        }

        sb.append("\nPara regresar al menú anterior digite 'Atrás' o 'Volver'\n");
        sb.append(String.format("\nResponde con el número (1-%d) para seleccionar:", opciones.size()));

        return sb.toString();
    }
}
