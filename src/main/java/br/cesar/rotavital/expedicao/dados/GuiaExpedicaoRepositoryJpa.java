package br.cesar.rotavital.expedicao.dados;

import br.cesar.rotavital.expedicao.negocio.dominio.GuiaExpedicao;
import br.cesar.rotavital.expedicao.negocio.dominio.StatusExpedicao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GuiaExpedicaoRepositoryJpa extends JpaRepository<GuiaExpedicao, Long> {
    Optional<GuiaExpedicao> findByNumero(String numero);
    List<GuiaExpedicao> findByStatus(StatusExpedicao status);
    long countByStatus(StatusExpedicao status);
}

