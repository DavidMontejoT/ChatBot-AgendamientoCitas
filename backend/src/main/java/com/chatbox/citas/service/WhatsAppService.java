package com.chatbox.citas.service;

import com.chatbox.citas.config.WhatsAppConfig;
import com.chatbox.citas.dto.CitaRequest;
import com.chatbox.citas.dto.CitaRequestCompleto;
import com.chatbox.citas.model.Doctor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppService {

    private final WhatsAppConfig config;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    private final CitaService citaService;
    private final PacienteService pacienteService;
    private final DoctorService doctorService;
    private final ValidacionDocumentoService validacionDocumentoService;
    private final ValidacionDatosService validacionDatosService;
    private final EmailService emailService;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    // Estado de conversaciones activas con timestamp
    private final ConcurrentHashMap<String, ConversacionState> conversaciones = new ConcurrentHashMap<>();

    // Enum para estados de conversación
    private enum EstadoConversacion {
        MENU,                               // Paso 1
        ESPERANDO_TIPO_DOC,                 // Paso 2
        ESPERANDO_NUMERO_DOC,               // Paso 3
        ESPERANDO_NOMBRE,                   // Paso 4
        ESPERANDO_TELEFONO_PRINCIPAL,       // Paso 5
        ESPERANDO_TELEFONO_SECUNDARIO,      // Paso 6
        ESPERANDO_DIRECCION,                // Paso 7
        ESPERANDO_FECHA_NACIMIENTO,         // Paso 8
        ESPERANDO_EPS,                      // Paso 9
        ESPERANDO_TIPO_CITA,                // Paso 10
        ESPERANDO_FECHA_CITA,               // Paso 11
        ESPERANDO_SELECCION_HORARIO,        // Paso 12 (eliminado, reemplazado por seleccion de doctor)
        ESPERANDO_SELECCION_DOCTOR,         // Paso 12 - Selección de doctor
        ESPERANDO_EMAIL,                   // Paso 13 - Email del paciente (nuevo)
        CONFIRMACION_FINAL                  // Paso 14 - Confirmación final (renombrado)
    }

    // Doctor por defecto asignado automáticamente
    private static final String DOCTOR_POR_DEFECTO = "Dr. Disponible";

    // Clase para guardar estado de conversación con timestamp de última actividad
    private static class ConversacionState {
        EstadoConversacion estado;
        LocalDateTime lastActivity;

        // Campos del paciente
        private String tipoIdentificacion;
        private String numeroIdentificacion;
        private String nombre;
        private String telefonoPrincipal;
        private String telefonoSecundario;
        private String direccion;
        private LocalDate fechaNacimiento;
        private String eps;
        private String email;

        // Campos de la cita
        private String tipoCita;
        private LocalDate fechaCita;
        private String horaCita;
        private String doctor;
        private java.util.List<OpcionDoctor> opcionesDoctor; // Para almacenar las opciones de doctor+hora

        // Stack para navegación "atrás"
        private final Stack<EstadoConversacion> historialEstados = new Stack<>();

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

        void guardarEstadoEnHistorial() {
            if (estado != EstadoConversacion.MENU) {
                historialEstados.push(estado);
            }
        }

        EstadoConversacion volverEstadoAnterior() {
            return historialEstados.isEmpty() ?
                EstadoConversacion.MENU : historialEstados.pop();
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

        // COMANDOS GLOBALES (funcionan en cualquier estado)
        if (mensajeNormalizado.equals("ATRÁS") || mensajeNormalizado.equals("VOLVER")) {
            if (estado.estado != EstadoConversacion.MENU) {
                EstadoConversacion anterior = estado.volverEstadoAnterior();
                estado.estado = anterior;
                enviarMensaje(telefono, "↩️ Volviendo al paso anterior...");
                reenviarPromptActual(telefono, estado);
            } else {
                mostrarMenu(telefono);
            }
            return;
        }

        if (mensajeNormalizado.equals("CANCELAR")) {
            conversaciones.remove(telefono);
            enviarMensaje(telefono, "❌ Proceso cancelado. Envía cualquier mensaje para iniciar.");
            return;
        }

        if (mensajeNormalizado.equals("INICIO") || mensajeNormalizado.equals("MENU")) {
            estado.estado = EstadoConversacion.MENU;
            mostrarMenu(telefono);
            return;
        }

        // Procesar según estado actual
        switch (estado.estado) {
            case MENU:
                procesarMenu(telefono, mensajeNormalizado, estado);
                break;

            case ESPERANDO_TIPO_DOC:
                procesarTipoDocumento(telefono, mensajeNormalizado, estado);
                break;

            case ESPERANDO_NUMERO_DOC:
                procesarNumeroDocumento(telefono, mensajeNormalizado, estado);
                break;

            case ESPERANDO_NOMBRE:
                procesarNombre(telefono, mensaje, estado);
                break;

            case ESPERANDO_TELEFONO_PRINCIPAL:
                procesarTelefonoPrincipal(telefono, mensajeNormalizado, estado);
                break;

            case ESPERANDO_TELEFONO_SECUNDARIO:
                procesarTelefonoSecundario(telefono, mensajeNormalizado, estado);
                break;

            case ESPERANDO_DIRECCION:
                procesarDireccion(telefono, mensaje, estado);
                break;

            case ESPERANDO_FECHA_NACIMIENTO:
                procesarFechaNacimiento(telefono, mensajeNormalizado, estado);
                break;

            case ESPERANDO_EPS:
                procesarEPS(telefono, mensaje, estado);
                break;

            case ESPERANDO_TIPO_CITA:
                procesarTipoCita(telefono, mensajeNormalizado, estado);
                break;

            case ESPERANDO_FECHA_CITA:
                procesarFechaCita(telefono, mensajeNormalizado, estado);
                break;

            case ESPERANDO_SELECCION_HORARIO:
                procesarSeleccionHorario(telefono, mensajeNormalizado, estado);
                break;

            case ESPERANDO_SELECCION_DOCTOR:
                procesarSeleccionDoctor(telefono, mensajeNormalizado, estado);
                break;

            case ESPERANDO_EMAIL:
                procesarEmail(telefono, mensajeNormalizado, estado);
                break;

            case CONFIRMACION_FINAL:
                procesarConfirmacionFinal(telefono, mensajeNormalizado, estado);
                break;
        }
    }

    private void mostrarMenu(String telefono) {
        String menu = """
            🏥 *Sociedad Urológica del Cauca*

            Selecciona una opción:

            1️⃣ Agendar Cita
            2️⃣ Cirugía y Procedimientos

            _Comandos disponibles: ATRÁS, CANCELAR, INICIO_
            """;

        enviarMensaje(telefono, menu);
    }

    // ========================================
    // PROCESADORES DE CADA PASO DEL CHATBOT
    // ========================================

    private void procesarMenu(String telefono, String mensaje, ConversacionState estado) {
        if (mensaje.contains("1") || mensaje.contains("CITA") || mensaje.contains("AGENDAR")) {
            estado.guardarEstadoEnHistorial();
            estado.estado = EstadoConversacion.ESPERANDO_TIPO_DOC;
            enviarMensaje(telefono, """
                📄 Vamos a iniciar el agendamiento de tu cita.

                Primero, selecciona tu tipo de documento:

                📋 CC - Cédula de Ciudadanía
                📋 TI - Tarjeta de Identidad
                📋 RC - Registro Civil

                Responde con las siglas (CC, TI o RC)
                """);
        } else if (mensaje.contains("2") || mensaje.contains("CIRUGÍA") || mensaje.contains("PROCEDIMIENTOS")) {
            enviarMensaje(telefono, """
                👨‍⚕️ Un especialista te contactará pronto para darte información sobre cirugías y procedimientos.

                Horario de atención: Lunes a Viernes de 9:00 AM a 6:00 PM
                Teléfono: 3013188696
                """);
            conversaciones.remove(telefono);
        } else {
            mostrarMenu(telefono);
        }
    }

    private void procesarTipoDocumento(String telefono, String mensaje, ConversacionState estado) {
        if (mensaje.equals("CC") || mensaje.equals("TI") || mensaje.equals("RC")) {
            estado.tipoIdentificacion = mensaje;
            estado.guardarEstadoEnHistorial();
            estado.estado = EstadoConversacion.ESPERANDO_NUMERO_DOC;
            enviarMensaje(telefono, String.format("📝 Escribe tu número de %s sin puntos ni guiones:", mensaje));
        } else {
            enviarMensaje(telefono, "⚠️ Opción inválida. Responde CC, TI o RC");
        }
    }

    private void procesarNumeroDocumento(String telefono, String mensaje, ConversacionState estado) {
        String numeroDoc = mensaje.replaceAll("[\\.\\s\\-]", "").trim();

        boolean valido = switch (estado.tipoIdentificacion) {
            case "CC" -> validacionDocumentoService.validarCC(numeroDoc);
            case "TI" -> validacionDocumentoService.validarTI(numeroDoc);
            case "RC" -> validacionDocumentoService.validarRC(numeroDoc);
            default -> false;
        };

        if (!valido) {
            enviarMensaje(telefono, "⚠️ Número de documento inválido. Verifica y vuelve a intentarlo");
            return;
        }

        // Verificar si paciente existe
        var pacienteOpt = pacienteService.buscarPorNumeroIdentificacion(numeroDoc);

        if (pacienteOpt.isPresent()) {
            var p = pacienteOpt.get();
            estado.nombre = p.getNombre();
            estado.direccion = p.getDireccion();
            estado.fechaNacimiento = p.getFechaNacimiento();
            estado.eps = p.getEps();
            estado.numeroIdentificacion = numeroDoc;

            enviarMensaje(telefono, "✅ Hemos encontrado tu información previa. Vamos a verificar algunos datos...");
            estado.guardarEstadoEnHistorial();
            estado.estado = EstadoConversacion.ESPERANDO_TELEFONO_PRINCIPAL;
            enviarMensaje(telefono, String.format("📱 Confirma tu teléfono principal o escribe uno nuevo (10 dígitos):\nActual: %s",
                p.getTelefono() != null ? p.getTelefono() : "No registrado"));
        } else {
            estado.numeroIdentificacion = numeroDoc;
            estado.guardarEstadoEnHistorial();
            estado.estado = EstadoConversacion.ESPERANDO_NOMBRE;
            enviarMensaje(telefono, "👤 Escribe tu nombre completo:");
        }
    }

    private void procesarNombre(String telefono, String mensaje, ConversacionState estado) {
        String nombre = mensaje.trim();
        if (nombre.length() < 3) {
            enviarMensaje(telefono, "⚠️ Por favor escribe tu nombre completo (mínimo 3 caracteres)");
            return;
        }

        estado.nombre = nombre;
        estado.guardarEstadoEnHistorial();
        estado.estado = EstadoConversacion.ESPERANDO_TELEFONO_PRINCIPAL;
        enviarMensaje(telefono, """
            📱 Escribe tu teléfono principal (10 dígitos):

            Formato: 300 XXX XXXX
            """);
    }

    private void procesarTelefonoPrincipal(String telefono, String mensaje, ConversacionState estado) {
        String telefonoLimpio = validacionDatosService.formatearTelefono(mensaje);

        if (!validacionDatosService.validarTelefonoColombiano(telefonoLimpio)) {
            enviarMensaje(telefono, "⚠️ Teléfono inválido. Debe ser un número colombiano de 10 dígitos que empiece con 3");
            return;
        }

        estado.telefonoPrincipal = telefonoLimpio;
        estado.guardarEstadoEnHistorial();
        estado.estado = EstadoConversacion.ESPERANDO_TELEFONO_SECUNDARIO;
        enviarMensaje(telefono, """
            📱 Escribe un teléfono secundario de contacto (opcional):

            Formato: 300 XXX XXXX
            O escribe OMITIR para continuar
            """);
    }

    private void procesarTelefonoSecundario(String telefono, String mensaje, ConversacionState estado) {
        String telefonoLimpio = validacionDatosService.formatearTelefono(mensaje);

        if (mensaje.equals("OMITIR") || mensaje.equals("SALTAR")) {
            estado.telefonoSecundario = null;
        } else if (validacionDatosService.validarTelefonoColombiano(telefonoLimpio)) {
            estado.telefonoSecundario = telefonoLimpio;
        } else {
            enviarMensaje(telefono, "⚠️ Teléfono inválido o escribe OMITIR para continuar");
            return;
        }

        estado.guardarEstadoEnHistorial();
        estado.estado = EstadoConversacion.ESPERANDO_DIRECCION;
        enviarMensaje(telefono, """
            📍 Escribe tu dirección completa:

            Ejemplo: Calle 123 #45-67, Barrio Centro
            """);
    }

    private void procesarDireccion(String telefono, String mensaje, ConversacionState estado) {
        String direccion = mensaje.trim();
        if (direccion.length() < 10) {
            enviarMensaje(telefono, "⚠️ Por favor escribe una dirección más completa (mínimo 10 caracteres)");
            return;
        }

        estado.direccion = direccion;
        estado.guardarEstadoEnHistorial();
        estado.estado = EstadoConversacion.ESPERANDO_FECHA_NACIMIENTO;
        enviarMensaje(telefono, """
            📅 Escribe tu fecha de nacimiento:

            Formato: dd-mm-yyyy
            Ejemplo: 15-06-1990

            ⚠️ Debes ser mayor de 18 años
            """);
    }

    private void procesarFechaNacimiento(String telefono, String mensaje, ConversacionState estado) {
        LocalDate fechaNac = validacionDatosService.validarFechaNacimiento(mensaje);

        if (fechaNac == null) {
            enviarMensaje(telefono, "⚠️ Fecha inválida. Debes ser mayor de 18 años. Usa el formato: dd-mm-yyyy");
            return;
        }

        estado.fechaNacimiento = fechaNac;
        estado.guardarEstadoEnHistorial();
        estado.estado = EstadoConversacion.ESPERANDO_EPS;
        enviarMensaje(telefono, """
            🏥 Escribe tu EPS (Entidad Promotora de Salud):

            Ejemplo: EPS Sura, Coomeva, Salud Total, etc.
            """);
    }

    private void procesarEPS(String telefono, String mensaje, ConversacionState estado) {
        String eps = mensaje.trim();
        if (eps.length() < 3) {
            enviarMensaje(telefono, "⚠️ Por favor escribe el nombre de tu EPS (mínimo 3 caracteres)");
            return;
        }

        estado.eps = eps;
        estado.guardarEstadoEnHistorial();
        estado.estado = EstadoConversacion.ESPERANDO_TIPO_CITA;
        enviarMensaje(telefono, """
            👨‍⚕️ ¿Qué tipo de cita necesitas?

            1️⃣ PRIMERA VEZ
            2️⃣ CONTROL

            Responde con el número de opción
            """);
    }

    private void procesarTipoCita(String telefono, String mensaje, ConversacionState estado) {
        if (mensaje.equals("1")) {
            estado.tipoCita = "PRIMERA VEZ";
        } else if (mensaje.equals("2")) {
            estado.tipoCita = "CONTROL";
        } else {
            enviarMensaje(telefono, "⚠️ Responde 1 para PRIMERA VEZ o 2 para CONTROL");
            return;
        }

        estado.guardarEstadoEnHistorial();
        estado.estado = EstadoConversacion.ESPERANDO_FECHA_CITA;
        enviarMensaje(telefono, """
            📅 ¿Para qué fecha deseas la cita?

            Formato: dd-mm-yyyy
            Ejemplo: 15-03-2026

            ⚠️ La fecha debe ser futura
            """);
    }

    private void procesarFechaCita(String telefono, String mensaje, ConversacionState estado) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            LocalDate fechaCita = LocalDate.parse(mensaje, formatter);

            if (fechaCita.isBefore(LocalDate.now())) {
                enviarMensaje(telefono, "⚠️ La fecha debe ser futura. Por favor selecciona otra fecha");
                return;
            }

            // Verificar que no sea domingo
            if (fechaCita.getDayOfWeek().name().equals("SUNDAY")) {
                enviarMensaje(telefono, "⚠️ No atendemos domingos. Por favor selecciona otra fecha");
                return;
            }

            estado.fechaCita = fechaCita;
            estado.guardarEstadoEnHistorial();
            estado.estado = EstadoConversacion.ESPERANDO_SELECCION_HORARIO;

            // Mostrar horarios disponibles
            String horarios = obtenerHorariosDisponibles(fechaCita);
            enviarMensaje(telefono, String.format("""
                ⏰ Selecciona una hora para tu cita del %s:

                %s

                Responde con el número de la hora deseada
                """, fechaCita.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), horarios));
        } catch (DateTimeParseException e) {
            enviarMensaje(telefono, "⚠️ Fecha inválida. Usa el formato: dd-mm-yyyy (ejemplo: 15-03-2026)");
        }
    }

    private void procesarSeleccionHorario(String telefono, String mensaje, ConversacionState estado) {
        try {
            // La validación de la fecha ya se hizo en el paso anterior
            // Ahora consultamos disponibilidad de doctores para esa fecha
            // Usar directamente el servicio en lugar de hacer llamada HTTP
            List<Object> disponibilidadList = citaService.obtenerHorariosDisponibles(estado.fechaCita);

            // Convertir a JsonNode
            ObjectMapper mapper = new ObjectMapper();
            JsonNode disponibilidadArray = mapper.valueToTree(disponibilidadList);

            if (disponibilidadArray.size() == 0) {
                enviarMensaje(telefono, "⚠️ No hay doctores disponibles para esta fecha. Por favor selecciona otra fecha.");
                estado.estado = EstadoConversacion.ESPERANDO_FECHA_CITA;
                return;
            }

            // Mostrar opciones disponibles (doctor + hora)
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("✅ Estas son las citas más próximas en la Sociedad Urológica del Cauca para el %s:\n\n",
                estado.fechaCita.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))));

            int opcion = 1;
            var opciones = new java.util.ArrayList<OpcionDoctor>();

            for (JsonNode item : disponibilidadArray) {
                String doctor = item.get("doctor").asText();
                String hora = item.get("hora").asText();
                boolean disponible = item.get("disponible").asBoolean();
                String especialidad = item.get("especialidad").asText();

                if (disponible) {
                    sb.append(String.format("%d. Dr. %s - %s - %s\n",
                        opcion, doctor, especialidad, hora));
                    opciones.add(new OpcionDoctor(doctor, hora, especialidad));
                    opcion++;
                }
            }

            if (opciones.isEmpty()) {
                enviarMensaje(telefono, "⚠️ No hay doctores disponibles para esta fecha. Por favor selecciona otra fecha.");
                estado.estado = EstadoConversacion.ESPERANDO_FECHA_CITA;
                return;
            }

            sb.append("\nPara regresar al menú anterior digite 'Atrás' o 'Volver'\n");
            sb.append(String.format("\nResponde con el número (1-%d) para seleccionar:", opciones.size()));

            // Guardar opciones en el estado
            estado.opcionesDoctor = opciones;
            estado.guardarEstadoEnHistorial();
            estado.estado = EstadoConversacion.ESPERANDO_SELECCION_DOCTOR;
            enviarMensaje(telefono, sb.toString());

        } catch (Exception e) {
            log.error("Error consultando disponibilidad: {}", e.getMessage(), e);
            enviarMensaje(telefono, "⚠️ Error al consultar disponibilidad. Por favor intenta nuevamente.");
        }
    }

    private void procesarSeleccionDoctor(String telefono, String mensaje, ConversacionState estado) {
        try {
            int opcion = Integer.parseInt(mensaje);

            if (estado.opcionesDoctor == null || estado.opcionesDoctor.isEmpty()) {
                enviarMensaje(telefono, "⚠️ Error: no hay opciones disponibles. Por favor inicia nuevamente.");
                conversaciones.remove(telefono);
                return;
            }

            if (opcion < 1 || opcion > estado.opcionesDoctor.size()) {
                enviarMensaje(telefono,
                    String.format("⚠️ Opción inválida. Responde un número entre 1 y %d",
                    estado.opcionesDoctor.size()));
                return;
            }

            OpcionDoctor seleccion = estado.opcionesDoctor.get(opcion - 1);
            estado.doctor = seleccion.doctor;
            estado.horaCita = seleccion.hora;

            // Pedir email del paciente
            estado.guardarEstadoEnHistorial();
            estado.estado = EstadoConversacion.ESPERANDO_EMAIL;
            enviarMensaje(telefono, """
                📧 Para enviarte la confirmación de tu cita, por favor proporciona tu correo electrónico:

                Ejemplo: tu.email@gmail.com

                _Escribe OMITIR si no tienes correo electrónico_
                """);

        } catch (NumberFormatException e) {
            enviarMensaje(telefono, "⚠️ Responde con el número de opción");
        }
    }

    private void procesarEmail(String telefono, String mensaje, ConversacionState estado) {
        String email = mensaje.trim();

        if (email.equalsIgnoreCase("OMITIR") || email.equalsIgnoreCase("SALTAR")) {
            estado.email = "";
            // Mostrar resumen y pedir confirmación
            String resumen = generarResumenCita(estado);
            estado.guardarEstadoEnHistorial();
            estado.estado = EstadoConversacion.CONFIRMACION_FINAL;
            enviarMensaje(telefono, resumen);
            return;
        }

        // Validación básica de email
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            enviarMensaje(telefono, "⚠️ Email inválido. Por favor ingresa un email válido o escribe OMITIR");
            return;
        }

        estado.email = email;

        // Mostrar resumen y pedir confirmación
        String resumen = generarResumenCita(estado);
        estado.guardarEstadoEnHistorial();
        estado.estado = EstadoConversacion.CONFIRMACION_FINAL;
        enviarMensaje(telefono, resumen);
    }

    private void procesarConfirmacionFinal(String telefono, String mensaje, ConversacionState estado) {
        if (mensaje.equals("SI") || mensaje.equals("SÍ") || mensaje.equals("1") || mensaje.equals("CONFIRMAR")) {
            crearCitaCompleto(telefono, estado);
            conversaciones.remove(telefono);
        } else if (mensaje.equals("NO") || mensaje.equals("2") || mensaje.equals("CANCELAR")) {
            conversaciones.remove(telefono);
            enviarMensaje(telefono, "❌ Proceso cancelado. Envía cualquier mensaje para iniciar");
        } else {
            enviarMensaje(telefono, "⚠️ Responde SI para confirmar o NO para cancelar");
        }
    }

    // ========================================
    // MÉTODOS AUXILIARES
    // ========================================

    private void reenviarPromptActual(String telefono, ConversacionState estado) {
        switch (estado.estado) {
            case ESPERANDO_TIPO_DOC:
                enviarMensaje(telefono, "📋 Responde CC, TI o RC");
                break;
            case ESPERANDO_NUMERO_DOC:
                enviarMensaje(telefono, "📝 Escribe tu número de documento:");
                break;
            case ESPERANDO_NOMBRE:
                enviarMensaje(telefono, "👤 Escribe tu nombre completo:");
                break;
            case ESPERANDO_TELEFONO_PRINCIPAL:
                enviarMensaje(telefono, "📱 Escribe tu teléfono principal (10 dígitos):");
                break;
            case ESPERANDO_TELEFONO_SECUNDARIO:
                enviarMensaje(telefono, "📱 Escribe teléfono secundario o OMITIR:");
                break;
            case ESPERANDO_DIRECCION:
                enviarMensaje(telefono, "📍 Escribe tu dirección completa:");
                break;
            case ESPERANDO_FECHA_NACIMIENTO:
                enviarMensaje(telefono, "📅 Escribe tu fecha de nacimiento (dd-mm-yyyy):");
                break;
            case ESPERANDO_EPS:
                enviarMensaje(telefono, "🏥 Escribe tu EPS:");
                break;
            case ESPERANDO_TIPO_CITA:
                enviarMensaje(telefono, "👨‍⚕️ 1. PRIMERA VEZ o 2. CONTROL:");
                break;
            case ESPERANDO_FECHA_CITA:
                enviarMensaje(telefono, "📅 Escribe la fecha de la cita (dd-mm-yyyy):");
                break;
            case ESPERANDO_SELECCION_HORARIO:
                String horarios = obtenerHorariosDisponibles(estado.fechaCita);
                enviarMensaje(telefono, "⏰ " + horarios);
                break;
            default:
                mostrarMenu(telefono);
        }
    }

    private String obtenerHorariosDisponibles(LocalDate fecha) {
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

    private String generarResumenCita(ConversacionState estado) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
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
            estado.tipoIdentificacion,
            estado.numeroIdentificacion,
            estado.nombre,
            estado.telefonoPrincipal,
            estado.telefonoSecundario != null ? "📱 Teléfono 2: " + estado.telefonoSecundario : "",
            estado.direccion,
            estado.fechaNacimiento.format(formatter),
            estado.eps,
            estado.tipoCita,
            estado.fechaCita.format(formatter),
            estado.horaCita,
            estado.doctor
        );
    }

    private void crearCitaCompleto(String telefono, ConversacionState estado) {
        try {
            // Convertir fechaCita (LocalDate) y horaCita (String) a LocalDateTime
            DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalDateTime fechaHora = estado.fechaCita.atTime(
                java.time.LocalTime.parse(estado.horaCita, horaFormatter)
            );

            if (fechaHora.isBefore(LocalDateTime.now())) {
                enviarMensaje(telefono, "⚠️ La fecha y hora deben ser futuras. Por favor inicia nuevamente.");
                return;
            }

            CitaRequestCompleto request = new CitaRequestCompleto();
            request.setNombrePaciente(estado.nombre);
            request.setTipoIdentificacion(estado.tipoIdentificacion);
            request.setNumeroIdentificacion(estado.numeroIdentificacion);
            request.setTelefono(estado.telefonoPrincipal);
            request.setTelefono2(estado.telefonoSecundario);
            request.setDireccion(estado.direccion);
            request.setFechaNacimiento(estado.fechaNacimiento);
            request.setEps(estado.eps);
            request.setTipoCita(estado.tipoCita);
            request.setFechaHora(fechaHora);
            request.setDoctor(estado.doctor);
            request.setEmail(estado.email);

            citaService.crearCitaCompleta(request);

            // Enviar confirmación por WhatsApp
            enviarConfirmacionCita(
                telefono,
                estado.nombre,
                estado.fechaCita.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                estado.horaCita,
                estado.doctor
            );

            // Enviar confirmación por Email si el paciente proporcionó email
            if (estado.email != null && !estado.email.isBlank()) {
                try {
                    emailService.enviarConfirmacionCita(
                        estado.email,
                        estado.nombre,
                        estado.tipoCita,
                        estado.doctor,
                        fechaHora
                    );
                    log.info("📧 Email de confirmación enviado a {}", estado.email);
                } catch (Exception e) {
                    log.error("Error enviando email: {}", e.getMessage(), e);
                    // No fallar el flujo si hay error con email
                }
            }

            log.info("✅ Cita completa creada para {} via WhatsApp Sofia", estado.nombre);

        } catch (Exception e) {
            log.error("Error creando cita: {}", e.getMessage(), e);
            enviarMensaje(telefono, "❌ Hubo un error al crear tu cita. Por favor intenta nuevamente escribiendo cualquier mensaje.");
        }
    }

    // Mantener el método antiguo para compatibilidad (solo si se usa en otros lugares)
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
        // Si ya empieza con +, retornar tal cual
        if (telefono.startsWith("+")) {
            return telefono;
        }

        // Detectar prefijo de país y agregar el + correspondiente
        // Prefijos comunes de países hispanohablantes:
        // 57 = Colombia, 52 = México, 51 = Perú, 56 = Chile, etc.
        String[] prefijosPais = {"57", "52", "51", "56", "54", "58", "34", "39"};

        for (String prefijo : prefijosPais) {
            if (telefono.startsWith(prefijo)) {
                return "+" + telefono;
            }
        }

        // Si no detectamos prefijo conocido, asumimos que falta el +
        return "+" + telefono;
    }

    private String escaparJson(String texto) {
        return texto.replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private record OpcionDoctor(String doctor, String hora, String especialidad) {}
}
