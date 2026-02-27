package com.chatbox.citas.service;

import com.chatbox.citas.service.email.EmailTemplateService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final Environment env;
    private final EmailTemplateService emailTemplateService;

    private boolean emailConfigurado() {
        String host = env.getProperty("spring.mail.host");
        String username = env.getProperty("spring.mail.username");
        String password = env.getProperty("spring.mail.password");

        boolean configurado = host != null && !host.isBlank() &&
                             username != null && !username.isBlank() &&
                             password != null && !password.isBlank();

        if (!configurado) {
            log.warn("⚠️ Email NO configurado - Verifica variables de entorno:");
            if (host == null || host.isBlank()) {
                log.warn("  ❌ spring.mail.host está vacío");
            }
            if (username == null || username.isBlank()) {
                log.warn("  ❌ BREVO_SMTP_USERNAME no está configurado (spring.mail.username)");
            }
            if (password == null || password.isBlank()) {
                log.warn("  ❌ BREVO_SMTP_KEY no está configurado (spring.mail.password)");
            }
        }

        return configurado;
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
        log.info("🔍 [DEBUG] Iniciando envío de email a: {}", toEmail);
        log.info("🔍 [DEBUG] Configuración - host: {}, port: {}, username: {}, from: {}, from-name: {}",
            env.getProperty("spring.mail.host"),
            env.getProperty("spring.mail.port"),
            env.getProperty("spring.mail.username"),
            env.getProperty("app.email.from"),
            env.getProperty("app.email.from-name")
        );

        if (!emailConfigurado()) {
            log.warn("⚠️ Email NO configurado - retornando sin enviar");
            return;
        }

        if (toEmail == null || toEmail.isBlank()) {
            log.warn("No se envía email: dirección de email vacía");
            return;
        }

        enviarConReintento(toEmail, () -> {
            log.info("📧 Creando mensaje MIME...");
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Confirmación de Cita Médica");
            helper.setFrom(env.getProperty("app.email.from"), env.getProperty("app.email.from-name"));
            helper.setText(emailTemplateService.generarConfirmacionCita(nombrePaciente, tipoCita, doctor, fechaHora), true);

            log.info("📤 Enviando email...");
            mailSender.send(message);
            log.info("✅ Email enviado exitosamente a {}", toEmail);
        });
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
            return;
        }

        if (toEmail == null || toEmail.isBlank()) {
            log.warn("No se envía recordatorio: dirección de email vacía");
            return;
        }

        enviarConReintento(toEmail, () -> {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("⏰ Recordatorio de Cita Médica");
            helper.setFrom(env.getProperty("app.email.from"), env.getProperty("app.email.from-name"));
            helper.setText(emailTemplateService.generarRecordatorioCita(nombrePaciente, tipoCita, doctor, fechaHora, horasAntes), true);

            mailSender.send(message);
            log.info("✅ Recordatorio enviado a {} ({} horas antes)", toEmail, horasAntes);
        });
    }

    /**
     * Envía email con reintentos en caso de fallo
     */
    private void enviarConReintento(String toEmail, Runnable emailSender) {
        Exception lastException = null;
        int maxReintentos = 2;

        for (int intento = 1; intento <= maxReintentos; intento++) {
            try {
                emailSender.run();
                return; // Éxito
            } catch (Exception e) {
                lastException = e;
                log.warn("⚠️ Intento {}/{} falló para {}: {}",
                    intento, maxReintentos, toEmail, e.getMessage());

                if (intento < maxReintentos) {
                    try {
                        Thread.sleep(1000 * intento); // Esperar antes de reintentar
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        // Todos los reintentos fallaron
        log.error("❌ Error enviando email a {} después de {} intentos: {}",
            toEmail, maxReintentos, lastException.getMessage(), lastException);
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
            helper.setText(emailTemplateService.generarRecordatorioCita(nombrePaciente, tipoCita, doctor, fechaHora, horasAntes), true);

            mailSender.send(message);
            log.info("✅ Recordatorio enviado a {} ({} horas antes)", toEmail, horasAntes);
        } catch (Exception e) {
            log.error("❌ Error enviando recordatorio a {}: {}", toEmail, e.getMessage(), e);
        }
    }

}
