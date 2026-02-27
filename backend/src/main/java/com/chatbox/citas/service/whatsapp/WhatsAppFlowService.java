package com.chatbox.citas.service.whatsapp;

import com.chatbox.citas.constants.WhatsAppConstants;
import com.chatbox.citas.dto.CitaRequestCompleto;
import com.chatbox.citas.service.CitaService;
import com.chatbox.citas.service.EmailService;
import com.chatbox.citas.service.PacienteService;
import com.chatbox.citas.service.ValidacionDatosService;
import com.chatbox.citas.service.ValidacionDocumentoService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for chatbot flow logic
 * Handles state transitions, input validation, and flow orchestration
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppFlowService {

    private final ConversationStateService conversationStateService;
    private final WhatsAppMessageService messageService;
    private final WhatsAppTemplateService templateService;
    private final CitaService citaService;
    private final PacienteService pacienteService;
    private final ValidacionDocumentoService validacionDocumentoService;
    private final ValidacionDatosService validacionDatosService;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern(WhatsAppConstants.FORMATO_HORA_PATTERN);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern(WhatsAppConstants.FORMATO_FECHA_PATTERN);

    /**
     * Process an incoming message based on current conversation state
     */
    public void procesarMensaje(String telefono, String mensaje) {
        log.info("Procesando mensaje de {}: {}", telefono, mensaje);

        String mensajeNormalizado = mensaje.trim().toUpperCase();

        // Get or create conversation state
        ConversationStateService.ConversacionState estado =
            conversationStateService.getOrCreateConversacion(telefono);

        // Clean up expired conversations periodically
        conversationStateService.limpiarConversacionesExpiradas();

        // Handle global commands (work in any state)
        if (procesarComandosGlobales(telefono, mensajeNormalizado, estado)) {
            return;
        }

        // Process based on current state
        procesarPorEstado(telefono, mensajeNormalizado, mensaje, estado);
    }

    /**
     * Process global commands that work in any state
     * @return true if a global command was processed, false otherwise
     */
    private boolean procesarComandosGlobales(
        String telefono,
        String mensajeNormalizado,
        ConversationStateService.ConversacionState estado
    ) {
        if (mensajeNormalizado.equals("ATRÁS") || mensajeNormalizado.equals("VOLVER")) {
            if (estado.getEstado() != ConversationStateService.EstadoConversacion.MENU) {
                ConversationStateService.EstadoConversacion anterior = estado.volverEstadoAnterior();
                estado.setEstado(anterior);
                messageService.enviarMensaje(telefono, "↩️ Volviendo al paso anterior...");
                reenviarPromptActual(telefono, estado);
            } else {
                mostrarMenu(telefono);
            }
            return true;
        }

        if (mensajeNormalizado.equals("CANCELAR")) {
            conversationStateService.removeConversacion(telefono);
            messageService.enviarMensaje(telefono, "❌ Proceso cancelado. Envía cualquier mensaje para iniciar.");
            return true;
        }

        if (mensajeNormalizado.equals("INICIO") || mensajeNormalizado.equals("MENU")) {
            estado.setEstado(ConversationStateService.EstadoConversacion.MENU);
            mostrarMenu(telefono);
            return true;
        }

        return false;
    }

    /**
     * Process message based on current conversation state
     */
    private void procesarPorEstado(
        String telefono,
        String mensajeNormalizado,
        String mensajeOriginal,
        ConversationStateService.ConversacionState estado
    ) {
        switch (estado.getEstado()) {
            case MENU -> procesarMenu(telefono, mensajeNormalizado, estado);
            case ESPERANDO_TIPO_DOC -> procesarTipoDocumento(telefono, mensajeNormalizado, estado);
            case ESPERANDO_NUMERO_DOC -> procesarNumeroDocumento(telefono, mensajeNormalizado, estado);
            case ESPERANDO_NOMBRE -> procesarNombre(telefono, mensajeOriginal, estado);
            case ESPERANDO_TELEFONO_PRINCIPAL -> procesarTelefonoPrincipal(telefono, mensajeNormalizado, estado);
            case ESPERANDO_TELEFONO_SECUNDARIO -> procesarTelefonoSecundario(telefono, mensajeNormalizado, estado);
            case ESPERANDO_DIRECCION -> procesarDireccion(telefono, mensajeOriginal, estado);
            case ESPERANDO_FECHA_NACIMIENTO -> procesarFechaNacimiento(telefono, mensajeNormalizado, estado);
            case ESPERANDO_EPS -> procesarEPS(telefono, mensajeOriginal, estado);
            case ESPERANDO_TIPO_CITA -> procesarTipoCita(telefono, mensajeNormalizado, estado);
            case ESPERANDO_FECHA_CITA -> procesarFechaCita(telefono, mensajeNormalizado, estado);
            case ESPERANDO_SELECCION_DOCTOR -> procesarSeleccionDoctor(telefono, mensajeNormalizado, estado);
            case ESPERANDO_EMAIL -> procesarEmail(telefono, mensajeNormalizado, estado);
            case CONFIRMACION_FINAL -> procesarConfirmacionFinal(telefono, mensajeNormalizado, estado);
        }
    }

    // ==================== FLOW PROCESSORS ====================

    private void procesarMenu(String telefono, String mensaje, ConversationStateService.ConversacionState estado) {
        if (mensaje.contains("1") || mensaje.contains("CITA") || mensaje.contains("AGENDAR")) {
            estado.guardarEstadoEnHistorial();
            estado.setEstado(ConversationStateService.EstadoConversacion.ESPERANDO_TIPO_DOC);
            messageService.enviarMensaje(telefono, templateService.generarPromptTipoDocumento());
        } else if (mensaje.contains("2") || mensaje.contains("CIRUGÍA") || mensaje.contains("PROCEDIMIENTOS")) {
            messageService.enviarMensaje(telefono, templateService.generarInfoCirugia());
            conversationStateService.removeConversacion(telefono);
        } else {
            mostrarMenu(telefono);
        }
    }

    private void procesarTipoDocumento(String telefono, String mensaje, ConversationStateService.ConversacionState estado) {
        if (mensaje.equals("CC") || mensaje.equals("TI") || mensaje.equals("RC")) {
            estado.setTipoIdentificacion(mensaje);
            estado.guardarEstadoEnHistorial();
            estado.setEstado(ConversationStateService.EstadoConversacion.ESPERANDO_NUMERO_DOC);
            messageService.enviarMensaje(telefono, templateService.generarPromptNumeroDocumento(mensaje));
        } else {
            messageService.enviarMensaje(telefono, "⚠️ Opción inválida. Responde CC, TI o RC");
        }
    }

    private void procesarNumeroDocumento(String telefono, String mensaje, ConversationStateService.ConversacionState estado) {
        String numeroDoc = mensaje.replaceAll("[\\.\\s\\-]", "").trim();

        boolean valido = switch (estado.getTipoIdentificacion()) {
            case "CC" -> validacionDocumentoService.validarCC(numeroDoc);
            case "TI" -> validacionDocumentoService.validarTI(numeroDoc);
            case "RC" -> validacionDocumentoService.validarRC(numeroDoc);
            default -> false;
        };

        if (!valido) {
            messageService.enviarMensaje(telefono, "⚠️ Número de documento inválido. Verifica y vuelve a intentarlo");
            return;
        }

        // Check if patient exists
        var pacienteOpt = pacienteService.buscarPorNumeroIdentificacion(numeroDoc);

        if (pacienteOpt.isPresent()) {
            var p = pacienteOpt.get();
            estado.setNombre(p.getNombre());
            estado.setDireccion(p.getDireccion());
            estado.setFechaNacimiento(p.getFechaNacimiento());
            estado.setEps(p.getEps());
            estado.setNumeroIdentificacion(numeroDoc);

            messageService.enviarMensaje(telefono, "✅ Hemos encontrado tu información previa. Vamos a verificar algunos datos...");
            estado.guardarEstadoEnHistorial();
            estado.setEstado(ConversationStateService.EstadoConversacion.ESPERANDO_TELEFONO_PRINCIPAL);
            messageService.enviarMensaje(telefono,
                String.format("📱 Confirma tu teléfono principal o escribe uno nuevo (10 dígitos):\nActual: %s",
                    p.getTelefono() != null ? p.getTelefono() : "No registrado"));
        } else {
            estado.setNumeroIdentificacion(numeroDoc);
            estado.guardarEstadoEnHistorial();
            estado.setEstado(ConversationStateService.EstadoConversacion.ESPERANDO_NOMBRE);
            messageService.enviarMensaje(telefono, "👤 Escribe tu nombre completo:");
        }
    }

    private void procesarNombre(String telefono, String mensaje, ConversationStateService.ConversacionState estado) {
        String nombre = mensaje.trim();
        if (nombre.length() < 3) {
            messageService.enviarMensaje(telefono, "⚠️ Por favor escribe tu nombre completo (mínimo 3 caracteres)");
            return;
        }

        estado.setNombre(nombre);
        estado.guardarEstadoEnHistorial();
        estado.setEstado(ConversationStateService.EstadoConversacion.ESPERANDO_TELEFONO_PRINCIPAL);
        messageService.enviarMensaje(telefono, templateService.generarPromptTelefonoPrincipal());
    }

    private void procesarTelefonoPrincipal(String telefono, String mensaje, ConversationStateService.ConversacionState estado) {
        String telefonoLimpio = validacionDatosService.formatearTelefono(mensaje);

        if (!validacionDatosService.validarTelefonoColombiano(telefonoLimpio)) {
            messageService.enviarMensaje(telefono,
                "⚠️ Teléfono inválido. Debe ser un número colombiano de 10 dígitos que empiece con 3");
            return;
        }

        estado.setTelefonoPrincipal(telefonoLimpio);
        estado.guardarEstadoEnHistorial();
        estado.setEstado(ConversationStateService.EstadoConversacion.ESPERANDO_TELEFONO_SECUNDARIO);
        messageService.enviarMensaje(telefono, templateService.generarPromptTelefonoSecundario());
    }

    private void procesarTelefonoSecundario(String telefono, String mensaje, ConversationStateService.ConversacionState estado) {
        String telefonoLimpio = validacionDatosService.formatearTelefono(mensaje);

        if (mensaje.equals("OMITIR") || mensaje.equals("SALTAR")) {
            estado.setTelefonoSecundario(null);
        } else if (validacionDatosService.validarTelefonoColombiano(telefonoLimpio)) {
            estado.setTelefonoSecundario(telefonoLimpio);
        } else {
            messageService.enviarMensaje(telefono, "⚠️ Teléfono inválido o escribe OMITIR para continuar");
            return;
        }

        estado.guardarEstadoEnHistorial();
        estado.setEstado(ConversationStateService.EstadoConversacion.ESPERANDO_DIRECCION);
        messageService.enviarMensaje(telefono, templateService.generarPromptDireccion());
    }

    private void procesarDireccion(String telefono, String mensaje, ConversationStateService.ConversacionState estado) {
        String direccion = mensaje.trim();
        if (direccion.length() < 10) {
            messageService.enviarMensaje(telefono,
                "⚠️ Por favor escribe una dirección más completa (mínimo 10 caracteres)");
            return;
        }

        estado.setDireccion(direccion);
        estado.guardarEstadoEnHistorial();
        estado.setEstado(ConversationStateService.EstadoConversacion.ESPERANDO_FECHA_NACIMIENTO);
        messageService.enviarMensaje(telefono, templateService.generarPromptFechaNacimiento());
    }

    private void procesarFechaNacimiento(String telefono, String mensaje, ConversationStateService.ConversacionState estado) {
        LocalDate fechaNac = validacionDatosService.validarFechaNacimiento(mensaje);

        if (fechaNac == null) {
            messageService.enviarMensaje(telefono,
                "⚠️ Fecha inválida. Debes ser mayor de 18 años. Usa el formato: dd-mm-yyyy");
            return;
        }

        estado.setFechaNacimiento(fechaNac);
        estado.guardarEstadoEnHistorial();
        estado.setEstado(ConversationStateService.EstadoConversacion.ESPERANDO_EPS);
        messageService.enviarMensaje(telefono, templateService.generarPromptEPS());
    }

    private void procesarEPS(String telefono, String mensaje, ConversationStateService.ConversacionState estado) {
        String eps = mensaje.trim();
        if (eps.length() < 3) {
            messageService.enviarMensaje(telefono, "⚠️ Por favor escribe el nombre de tu EPS (mínimo 3 caracteres)");
            return;
        }

        estado.setEps(eps);
        estado.guardarEstadoEnHistorial();
        estado.setEstado(ConversationStateService.EstadoConversacion.ESPERANDO_TIPO_CITA);
        messageService.enviarMensaje(telefono, templateService.generarPromptTipoCita());
    }

    private void procesarTipoCita(String telefono, String mensaje, ConversationStateService.ConversacionState estado) {
        if (mensaje.equals("1")) {
            estado.setTipoCita("PRIMERA VEZ");
        } else if (mensaje.equals("2")) {
            estado.setTipoCita("CONTROL");
        } else {
            messageService.enviarMensaje(telefono, "⚠️ Responde 1 para PRIMERA VEZ o 2 para CONTROL");
            return;
        }

        estado.guardarEstadoEnHistorial();
        estado.setEstado(ConversationStateService.EstadoConversacion.ESPERANDO_FECHA_CITA);
        messageService.enviarMensaje(telefono, templateService.generarPromptFechaCita());
    }

    private void procesarFechaCita(String telefono, String mensaje, ConversationStateService.ConversacionState estado) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            LocalDate fechaCita = LocalDate.parse(mensaje, formatter);

            if (fechaCita.isBefore(LocalDate.now())) {
                messageService.enviarMensaje(telefono,
                    "⚠️ La fecha debe ser futura. Por favor selecciona otra fecha");
                return;
            }

            if (fechaCita.getDayOfWeek().name().equals("SUNDAY")) {
                messageService.enviarMensaje(telefono, "⚠️ No atendemos domingos. Por favor selecciona otra fecha");
                return;
            }

            estado.setFechaCita(fechaCita);
            estado.guardarEstadoEnHistorial();
            estado.setEstado(ConversationStateService.EstadoConversacion.ESPERANDO_SELECCION_DOCTOR);

            // Show available doctors
            mostrarOpcionesDoctor(telefono, fechaCita, estado);

        } catch (DateTimeParseException e) {
            messageService.enviarMensaje(telefono,
                "⚠️ Fecha inválida. Usa el formato: dd-mm-yyyy (ejemplo: 15-03-2026)");
        }
    }

    private void mostrarOpcionesDoctor(
        String telefono,
        LocalDate fechaCita,
        ConversationStateService.ConversacionState estado
    ) {
        try {
            List<Object> disponibilidadList = citaService.obtenerHorariosDisponibles(fechaCita);
            JsonNode disponibilidadArray = objectMapper.valueToTree(disponibilidadList);

            if (disponibilidadArray.isEmpty()) {
                messageService.enviarMensaje(telefono,
                    "⚠️ No hay doctores disponibles para esta fecha. Por favor selecciona otra fecha.");
                estado.setEstado(ConversationStateService.EstadoConversacion.ESPERANDO_FECHA_CITA);
                return;
            }

            List<ConversationStateService.OpcionDoctor> opciones = new ArrayList<>();

            for (JsonNode item : disponibilidadArray) {
                String doctor = item.get("doctor").asText();
                String hora = item.get("hora").asText();
                boolean disponible = item.get("disponible").asBoolean();
                String especialidad = item.get("especialidad").asText();

                if (disponible) {
                    opciones.add(new ConversationStateService.OpcionDoctor(doctor, hora, especialidad));
                }
            }

            if (opciones.isEmpty()) {
                messageService.enviarMensaje(telefono,
                    "⚠️ No hay doctores disponibles para esta fecha. Por favor selecciona otra fecha.");
                estado.setEstado(ConversationStateService.EstadoConversacion.ESPERANDO_FECHA_CITA);
                return;
            }

            estado.setOpcionesDoctor(opciones);
            estado.guardarEstadoEnHistorial();
            estado.setEstado(ConversationStateService.EstadoConversacion.ESPERANDO_SELECCION_DOCTOR);
            messageService.enviarMensaje(telefono, templateService.generarSeleccionDoctor(fechaCita, opciones));

        } catch (Exception e) {
            log.error("Error consultando disponibilidad: {}", e.getMessage(), e);
            messageService.enviarMensaje(telefono,
                "⚠️ Error al consultar disponibilidad. Por favor intenta nuevamente.");
        }
    }

    private void procesarSeleccionDoctor(String telefono, String mensaje, ConversationStateService.ConversacionState estado) {
        try {
            int opcion = Integer.parseInt(mensaje);

            if (estado.getOpcionesDoctor() == null || estado.getOpcionesDoctor().isEmpty()) {
                messageService.enviarMensaje(telefono,
                    "⚠️ Error: no hay opciones disponibles. Por favor inicia nuevamente.");
                conversationStateService.removeConversacion(telefono);
                return;
            }

            if (opcion < 1 || opcion > estado.getOpcionesDoctor().size()) {
                messageService.enviarMensaje(telefono,
                    String.format("⚠️ Opción inválida. Responde un número entre 1 y %d",
                        estado.getOpcionesDoctor().size()));
                return;
            }

            ConversationStateService.OpcionDoctor seleccion = estado.getOpcionesDoctor().get(opcion - 1);
            estado.setDoctor(seleccion.doctor());
            estado.setHoraCita(seleccion.hora());

            // Ask for patient email
            estado.guardarEstadoEnHistorial();
            estado.setEstado(ConversationStateService.EstadoConversacion.ESPERANDO_EMAIL);
            messageService.enviarMensaje(telefono, templateService.generarPromptEmail());

        } catch (NumberFormatException e) {
            messageService.enviarMensaje(telefono, "⚠️ Responde con el número de opción");
        }
    }

    private void procesarEmail(String telefono, String mensaje, ConversationStateService.ConversacionState estado) {
        String email = mensaje.trim();

        if (email.equalsIgnoreCase("OMITIR") || email.equalsIgnoreCase("SALTAR")) {
            estado.setEmail("");
            mostrarResumenYConfirmar(telefono, estado);
            return;
        }

        // Basic email validation
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            messageService.enviarMensaje(telefono, "⚠️ Email inválido. Por favor ingresa un email válido o escribe OMITIR");
            return;
        }

        estado.setEmail(email);
        mostrarResumenYConfirmar(telefono, estado);
    }

    private void mostrarResumenYConfirmar(String telefono, ConversationStateService.ConversacionState estado) {
        String resumen = templateService.generarResumenCita(estado);
        estado.guardarEstadoEnHistorial();
        estado.setEstado(ConversationStateService.EstadoConversacion.CONFIRMACION_FINAL);
        messageService.enviarMensaje(telefono, resumen);
    }

    private void procesarConfirmacionFinal(String telefono, String mensaje, ConversationStateService.ConversacionState estado) {
        if (mensaje.equals("SI") || mensaje.equals("SÍ") || mensaje.equals("1") || mensaje.equals("CONFIRMAR")) {
            crearCitaCompleto(telefono, estado);
            conversationStateService.removeConversacion(telefono);
        } else if (mensaje.equals("NO") || mensaje.equals("2") || mensaje.equals("CANCELAR")) {
            conversationStateService.removeConversacion(telefono);
            messageService.enviarMensaje(telefono, "❌ Proceso cancelado. Envía cualquier mensaje para iniciar");
        } else {
            messageService.enviarMensaje(telefono, "⚠️ Responde SI para confirmar o NO para cancelar");
        }
    }

    // ==================== HELPER METHODS ====================

    private void mostrarMenu(String telefono) {
        messageService.enviarMensaje(telefono, templateService.generarMenu());
    }

    private void reenviarPromptActual(String telefono, ConversationStateService.ConversacionState estado) {
        String prompt = templateService.generarPromptActual(estado.getEstado());
        messageService.enviarMensaje(telefono, prompt);
    }

    private void crearCitaCompleto(String telefono, ConversationStateService.ConversacionState estado) {
        try {
            // Convert fechaCita (LocalDate) and horaCita (String) to LocalDateTime
            LocalDateTime fechaHora = estado.getFechaCita().atTime(LocalTime.parse(estado.getHoraCita(), FORMATO_HORA));

            if (fechaHora.isBefore(LocalDateTime.now())) {
                messageService.enviarMensaje(telefono,
                    "⚠️ La fecha y hora deben ser futuras. Por favor inicia nuevamente.");
                return;
            }

            CitaRequestCompleto request = new CitaRequestCompleto();
            request.setNombrePaciente(estado.getNombre());
            request.setTipoIdentificacion(estado.getTipoIdentificacion());
            request.setNumeroIdentificacion(estado.getNumeroIdentificacion());
            request.setTelefono(estado.getTelefonoPrincipal());
            request.setTelefono2(estado.getTelefonoSecundario());
            request.setDireccion(estado.getDireccion());
            request.setFechaNacimiento(estado.getFechaNacimiento());
            request.setEps(estado.getEps());
            request.setTipoCita(estado.getTipoCita());
            request.setFechaHora(fechaHora);
            request.setDoctor(estado.getDoctor());
            request.setEmail(estado.getEmail());

            citaService.crearCitaCompleta(request);

            log.info("✅ Cita creada para {} con doctor {} el {} a las {}",
                estado.getNombre(), estado.getDoctor(),
                estado.getFechaCita().format(FORMATO_FECHA),
                estado.getHoraCita()
            );

            // Send WhatsApp confirmation
            messageService.enviarMensaje(telefono,
                templateService.generarConfirmacionCita(
                    estado.getNombre(),
                    estado.getFechaCita().format(FORMATO_FECHA),
                    estado.getHoraCita(),
                    estado.getDoctor()
                )
            );

            // Send email confirmation if patient provided email
            if (estado.getEmail() != null && !estado.getEmail().isBlank()) {
                try {
                    log.info("📧 Enviando email de confirmación a {} para cita con {} el {}",
                        estado.getEmail(),
                        estado.getDoctor(),
                        fechaHora.format(FORMATO_FECHA)
                    );
                    emailService.enviarConfirmacionCita(
                        estado.getEmail(),
                        estado.getNombre(),
                        estado.getTipoCita(),
                        estado.getDoctor(),
                        fechaHora
                    );
                    log.info("✅ Email de confirmación enviado exitosamente a {}", estado.getEmail());
                } catch (Exception e) {
                    log.error("❌ Error enviando email a {}: {}", estado.getEmail(), e.getMessage(), e);
                    // Don't fail the flow if there's an error with email
                }
            } else {
                log.info("ℹ️ No se envió email: el paciente no proporcionó correo");
            }

            log.info("✅ Cita completa creada para {} via WhatsApp Sofia", estado.getNombre());

        } catch (Exception e) {
            log.error("Error creando cita: {}", e.getMessage(), e);
            messageService.enviarMensaje(telefono,
                "❌ Hubo un error al crear tu cita. Por favor intenta nuevamente escribiendo cualquier mensaje.");
        }
    }
}
