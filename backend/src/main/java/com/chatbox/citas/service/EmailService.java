package com.chatbox.citas.service;

import com.chatbox.citas.service.email.BrevoEmailApiService;
import com.chatbox.citas.service.email.EmailTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service for sending emails
 * Uses Brevo HTTP API (works on platforms with port restrictions like Render)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final Environment env;
    private final BrevoEmailApiService brevoEmailApiService;
    private final EmailTemplateService emailTemplateService;

    /**
     * Envía email de confirmación de cita al paciente
     */
    public void enviarConfirmacionCita(
        String toEmail,
        String nombrePaciente,
        String tipoCita,
        String doctor,
        LocalDateTime fechaHora
    ) {
        log.info("📧 [Brevo API] Enviando email de confirmación a: {}", toEmail);

        if (!brevoEmailApiService.isConfigured()) {
            log.warn("⚠️ Email NO configurado - retornando sin enviar");
            return;
        }

        if (toEmail == null || toEmail.isBlank()) {
            log.warn("No se envía email: dirección de email vacía");
            return;
        }

        try {
            String htmlContent = emailTemplateService.generarConfirmacionCita(
                nombrePaciente, tipoCita, doctor, fechaHora
            );

            brevoEmailApiService.sendHtmlEmail(
                toEmail,
                nombrePaciente,
                "Confirmación de Cita Médica",
                htmlContent
            );

            log.info("✅ Email de confirmación enviado exitosamente a {}", toEmail);

        } catch (Exception e) {
            log.error("❌ Error enviando email de confirmación a {}: {}",
                toEmail, e.getMessage(), e);
        }
    }

    /**
     * Envía email de recordatorio de cita
     */
    public void enviarRecordatorioCita(
        String toEmail,
        String nombrePaciente,
        String tipoCita,
        String doctor,
        LocalDateTime fechaHora,
        int horasAntes
    ) {
        log.info("📧 [Brevo API] Enviando recordatorio a: {} ({} horas antes)", toEmail, horasAntes);

        if (!brevoEmailApiService.isConfigured()) {
            log.warn("⚠️ Email NO configurado - retornando sin enviar");
            return;
        }

        if (toEmail == null || toEmail.isBlank()) {
            log.warn("No se envía recordatorio: dirección de email vacía");
            return;
        }

        try {
            String htmlContent = emailTemplateService.generarRecordatorioCita(
                nombrePaciente, tipoCita, doctor, fechaHora, horasAntes
            );

            brevoEmailApiService.sendHtmlEmail(
                toEmail,
                nombrePaciente,
                "⏰ Recordatorio de Cita Médica",
                htmlContent
            );

            log.info("✅ Recordatorio enviado exitosamente a {} ({} horas antes)", toEmail, horasAntes);

        } catch (Exception e) {
            log.error("❌ Error enviando recordatorio a {}: {}",
                toEmail, e.getMessage(), e);
        }
    }

    /**
     * Envía email de cancelación de cita
     */
    public void enviarCancelacionCita(
        String toEmail,
        String nombrePaciente,
        String tipoCita,
        String doctor,
        LocalDateTime fechaHora
    ) {
        log.info("📧 [Brevo API] Enviando cancelación a: {}", toEmail);

        if (!brevoEmailApiService.isConfigured()) {
            log.warn("⚠️ Email NO configurado - retornando sin enviar");
            return;
        }

        if (toEmail == null || toEmail.isBlank()) {
            log.warn("No se envía cancelación: dirección de email vacía");
            return;
        }

        try {
            String htmlContent = emailTemplateService.generarCancelacionCita(
                nombrePaciente, tipoCita, doctor, fechaHora
            );

            brevoEmailApiService.sendHtmlEmail(
                toEmail,
                nombrePaciente,
                "Cancelación de Cita",
                htmlContent
            );

            log.info("✅ Cancelación enviada exitosamente a {}", toEmail);

        } catch (Exception e) {
            log.error("❌ Error enviando cancelación a {}: {}",
                toEmail, e.getMessage(), e);
        }
    }
}
