package com.chatbox.citas.service;

import com.chatbox.citas.model.Cita;
import com.chatbox.citas.model.Cita.EstadoCita;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecordatorioService {

    private final CitaService citaService;
    private final WhatsAppService whatsAppService;

    @Value("${reminder.enabled:true}")
    private boolean recordatoriosHabilitados;

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    @Scheduled(cron = "0 0 * * * *")
    public void enviarRecordatorios() {
        if (!recordatoriosHabilitados) {
            log.debug("Recordatorios deshabilitados");
            return;
        }

        log.info("Iniciando proceso de recordatorios - {}", LocalDateTime.now());

        enviarRecordatorios24h();
        enviarRecordatorios1h();
    }

    private void enviarRecordatorios24h() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime en24Horas = ahora.plusHours(24);

        LocalDateTime inicioBusqueda = en24Horas.minusMinutes(30);
        LocalDateTime finBusqueda = en24Horas.plusMinutes(30);

        List<Cita> citas = citaService.obtenerCitasParaRecordatorio24h(inicioBusqueda, finBusqueda);

        log.info("Found {} appointments for 24h reminder", citas.size());

        for (Cita cita : citas) {
            try {
                String fecha = cita.getFechaHora().format(FORMATO_FECHA);
                String hora = cita.getFechaHora().format(FORMATO_HORA);

                whatsAppService.enviarRecordatorio(
                        cita.getPaciente().getTelefono(),
                        cita.getPaciente().getNombre(),
                        fecha,
                        hora,
                        cita.getDoctor(),
                        24
                );

                citaService.marcarRecordatorioEnviado(cita, 24);

                log.info("Recordatorio 24h enviado para cita ID: {}", cita.getId());

            } catch (Exception e) {
                log.error("Error enviando recordatorio 24h para cita {}: {}",
                        cita.getId(), e.getMessage(), e);
            }
        }
    }

    private void enviarRecordatorios1h() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime en1Hora = ahora.plusHours(1);

        LocalDateTime inicioBusqueda = en1Hora.minusMinutes(10);
        LocalDateTime finBusqueda = en1Hora.plusMinutes(10);

        List<Cita> citas = citaService.obtenerCitasParaRecordatorio1h(inicioBusqueda, finBusqueda);

        log.info("Found {} appointments for 1h reminder", citas.size());

        for (Cita cita : citas) {
            try {
                String fecha = cita.getFechaHora().format(FORMATO_FECHA);
                String hora = cita.getFechaHora().format(FORMATO_HORA);

                whatsAppService.enviarRecordatorio(
                        cita.getPaciente().getTelefono(),
                        cita.getPaciente().getNombre(),
                        fecha,
                        hora,
                        cita.getDoctor(),
                        1
                );

                citaService.marcarRecordatorioEnviado(cita, 1);

                log.info("Recordatorio 1h enviado para cita ID: {}", cita.getId());

            } catch (Exception e) {
                log.error("Error enviando recordatorio 1h para cita {}: {}",
                        cita.getId(), e.getMessage(), e);
            }
        }
    }

    @Scheduled(cron = "0 */30 * * * *")
    public void verificarRecordatorios() {
        // Verificación periódica (cada 30 minutos)
    }
}
