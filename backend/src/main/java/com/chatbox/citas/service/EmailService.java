package com.chatbox.citas.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final Environment env;

    private boolean emailConfigurado() {
        return env.getProperty("spring.mail.host") != null &&
               !env.getProperty("spring.mail.host", "").isBlank();
    }

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
        if (!emailConfigurado()) {
            log.debug("Email no configurado, saltando envío");
            return;
        }

        if (toEmail == null || toEmail.isBlank()) {
            log.warn("No se envía email: dirección de email vacía");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Confirmación de Cita Médica");
            helper.setFrom(env.getProperty("app.email.from"), env.getProperty("app.email.from-name"));
            helper.setText(buildEmailTemplate(nombrePaciente, tipoCita, doctor, fechaHora), true);

            mailSender.send(message);
            log.info("✅ Email enviado exitosamente a {}", toEmail);
        } catch (Exception e) {
            log.error("❌ Error enviando email a {}: {}", toEmail, e.getMessage(), e);
        }
    }

    /**
     * Construye template HTML del email de confirmación
     */
    private String buildEmailTemplate(String nombre, String tipo, String doctor, LocalDateTime fecha) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' hh:mm a");
        String fechaFormateada = fecha.format(formatter);

        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background-color: #f8f9fa;">
                <div style="background: #2c3e50; color: white; padding: 30px 20px; text-align: center; border-radius: 8px 8px 0 0;">
                    <h1 style="margin: 0; font-size: 24px;">Sociedad Urológica del Cauca</h1>
                    <h2 style="margin: 10px 0 0 0; font-size: 20px; font-weight: normal;">Confirmación de Cita</h2>
                </div>

                <div style="padding: 30px 20px;">
                    <h3 style="color: #2c3e50; margin-bottom: 20px;">Estimado/a %s</h3>
                    <p style="color: #555; line-height: 1.6;">Tu cita ha sido confirmada con los siguientes detalles:</p>

                    <table style="width: 100%%; border-collapse: collapse; margin: 25px 0; background: white;">
                        <tr style="background: #ecf0f1;">
                            <td style="padding: 15px; font-weight: bold; color: #2c3e50; border-bottom: 1px solid #ddd;">Tipo de Cita:</td>
                            <td style="padding: 15px; color: #555; border-bottom: 1px solid #ddd;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 15px; font-weight: bold; color: #2c3e50; border-bottom: 1px solid #ddd;">Doctor:</td>
                            <td style="padding: 15px; color: #555; border-bottom: 1px solid #ddd;">%s</td>
                        </tr>
                        <tr style="background: #ecf0f1;">
                            <td style="padding: 15px; font-weight: bold; color: #2c3e50;">Fecha y Hora:</td>
                            <td style="padding: 15px; color: #555;">%s</td>
                        </tr>
                    </table>

                    <div style="background: #d4edda; border-left: 4px solid #28a745; padding: 15px; margin: 25px 0; border-radius: 4px;">
                        <p style="margin: 0; color: #155724; font-size: 14px;">
                            <strong>Recordatorios:</strong> Recibirás recordatorios 24 horas y 1 hora antes de tu cita.
                        </p>
                    </div>

                    <p style="color: #7f8c8d; font-size: 12px; margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd;">
                        Si tienes preguntas, contáctanos al <strong>3013188696</strong><br>
                        Sociedad Urológica del Cauca
                    </p>
                </div>

                <div style="background: #34495e; color: white; padding: 15px; text-align: center; font-size: 12px; border-radius: 0 0 8px 8px;">
                    © 2024 Sociedad Urológica del Cauca. Todos los derechos reservados.
                </div>
            </div>
            """.formatted(nombre, tipo, doctor, fechaFormateada);
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
        if (!emailConfigurado()) {
            log.debug("Email no configurado, saltando envío de recordatorio");
            return;
        }

        if (toEmail == null || toEmail.isBlank()) {
            log.warn("No se envía recordatorio: dirección de email vacía");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("⏰ Recordatorio de Cita Médica");
            helper.setFrom(env.getProperty("app.email.from"), env.getProperty("app.email.from-name"));
            helper.setText(buildReminderTemplate(nombrePaciente, tipoCita, doctor, fechaHora, horasAntes), true);

            mailSender.send(message);
            log.info("✅ Recordatorio enviado a {} ({} horas antes)", toEmail, horasAntes);
        } catch (Exception e) {
            log.error("❌ Error enviando recordatorio a {}: {}", toEmail, e.getMessage(), e);
        }
    }

    /**
     * Construye template HTML del email de recordatorio
     */
    private String buildReminderTemplate(String nombre, String tipo, String doctor, LocalDateTime fecha, int horasAntes) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' hh:mm a");
        String fechaFormateada = fecha.format(formatter);

        String mensaje = horasAntes == 24
            ? "Tu cita es <strong>mañana</strong>. ¡Te esperamos!"
            : "Tu cita es en <strong>1 hora</strong>. ¡Te esperamos pronto!";

        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background-color: #f8f9fa;">
                <div style="background: #e74c3c; color: white; padding: 30px 20px; text-align: center; border-radius: 8px 8px 0 0;">
                    <h1 style="margin: 0; font-size: 24px;">⏰ Recordatorio de Cita</h1>
                </div>

                <div style="padding: 30px 20px;">
                    <h3 style="color: #2c3e50; margin-bottom: 20px;">Estimado/a %s</h3>

                    <table style="width: 100%%; border-collapse: collapse; margin: 25px 0; background: white;">
                        <tr style="background: #ecf0f1;">
                            <td style="padding: 15px; font-weight: bold; color: #2c3e50; border-bottom: 1px solid #ddd;">Tipo de Cita:</td>
                            <td style="padding: 15px; color: #555; border-bottom: 1px solid #ddd;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 15px; font-weight: bold; color: #2c3e50; border-bottom: 1px solid #ddd;">Doctor:</td>
                            <td style="padding: 15px; color: #555; border-bottom: 1px solid #ddd;">%s</td>
                        </tr>
                        <tr style="background: #ecf0f1;">
                            <td style="padding: 15px; font-weight: bold; color: #2c3e50;">Fecha y Hora:</td>
                            <td style="padding: 15px; color: #555;">%s</td>
                        </tr>
                    </table>

                    <div style="background: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 25px 0; border-radius: 4px;">
                        <p style="margin: 0; color: #856404; font-size: 16px;">%s</p>
                    </div>

                    <p style="color: #7f8c8d; font-size: 12px; margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd;">
                        Si tienes preguntas, contáctanos al <strong>3013188696</strong>
                    </p>
                </div>
            </div>
            """.formatted(nombre, tipo, doctor, fechaFormateada, mensaje);
    }
}
