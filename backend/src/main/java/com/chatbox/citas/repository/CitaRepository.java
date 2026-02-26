package com.chatbox.citas.repository;

import com.chatbox.citas.model.Cita;
import com.chatbox.citas.model.Cita.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findAllByOrderByFechaHoraDesc();

    List<Cita> findByPaciente_TelefonoOrderByFechaHoraDesc(String telefono);

    List<Cita> findByPaciente_IdOrderByFechaHoraDesc(Long pacienteId);

    List<Cita> findByEstadoOrderByFechaHoraAsc(EstadoCita estado);

    @Query("SELECT c FROM Cita c WHERE c.estado = 'PROGRAMADA' AND c.fechaHora BETWEEN :inicio AND :fin")
    List<Cita> findCitasProgramadasEntre(LocalDateTime inicio, LocalDateTime fin);

    @Query("SELECT c FROM Cita c WHERE c.estado = 'PROGRAMADA' AND " +
           "c.recordatorio24hEnviado = false AND " +
           "c.fechaHora BETWEEN :inicio AND :fin")
    List<Cita> findCitasParaRecordatorio24h(LocalDateTime inicio, LocalDateTime fin);

    @Query("SELECT c FROM Cita c WHERE c.estado = 'PROGRAMADA' AND " +
           "c.recordatorio1hEnviado = false AND " +
           "c.fechaHora BETWEEN :inicio AND :fin")
    List<Cita> findCitasParaRecordatorio1h(LocalDateTime inicio, LocalDateTime fin);
}
