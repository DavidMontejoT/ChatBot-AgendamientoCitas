package com.chatbox.citas.service.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service responsible for email template generation
 * Provides consistent HTML email formatting across the application
 */
@Slf4j
@Service
public class EmailTemplateService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' hh:mm a");

    /**
     * Generate appointment confirmation email HTML template
     */
    public String generarConfirmacionCita(
        String nombre,
        String tipo,
        String doctor,
        LocalDateTime fecha
    ) {
        String fechaFormateada = fecha.format(DATE_FORMATTER);

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
     * Generate appointment reminder email HTML template
     */
    public String generarRecordatorioCita(
        String nombre,
        String tipo,
        String doctor,
        LocalDateTime fecha,
        int horasAntes
    ) {
        String fechaFormateada = fecha.format(DATE_FORMATTER);

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

    /**
     * Generate cancellation confirmation email HTML template
     */
    public String generarCancelacionCita(
        String nombre,
        String tipo,
        String doctor,
        LocalDateTime fecha
    ) {
        String fechaFormateada = fecha.format(DATE_FORMATTER);

        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background-color: #f8f9fa;">
                <div style="background: #e74c3c; color: white; padding: 30px 20px; text-align: center; border-radius: 8px 8px 0 0;">
                    <h1 style="margin: 0; font-size: 24px;">Cancelación de Cita</h1>
                </div>

                <div style="padding: 30px 20px;">
                    <h3 style="color: #2c3e50; margin-bottom: 20px;">Estimado/a %s</h3>
                    <p style="color: #555; line-height: 1.6;">Tu cita ha sido cancelada:</p>

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

                    <div style="background: #d1ecf1; border-left: 4px solid #17a2b8; padding: 15px; margin: 25px 0; border-radius: 4px;">
                        <p style="margin: 0; color: #0c5460; font-size: 14px;">
                            Si deseas reagendar tu cita, contáctanos al <strong>3013188696</strong>
                        </p>
                    </div>
                </div>

                <div style="background: #34495e; color: white; padding: 15px; text-align: center; font-size: 12px; border-radius: 0 0 8px 8px;">
                    © 2024 Sociedad Urológica del Cauca. Todos los derechos reservados.
                </div>
            </div>
            """.formatted(nombre, tipo, doctor, fechaFormateada);
    }
}
