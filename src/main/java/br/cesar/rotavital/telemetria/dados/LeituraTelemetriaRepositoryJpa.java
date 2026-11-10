package br.cesar.rotavital.telemetria.dados;

import br.cesar.rotavital.telemetria.negocio.dominio.LeituraTelemetria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LeituraTelemetriaRepositoryJpa extends JpaRepository<LeituraTelemetria, Long> {
    List<LeituraTelemetria> findTop20ByOrderByLidoEmDesc();
    List<LeituraTelemetria> findByGuiaIdOrderByLidoEmAsc(Long guiaId);
    long countByAlertaTrue();
}

