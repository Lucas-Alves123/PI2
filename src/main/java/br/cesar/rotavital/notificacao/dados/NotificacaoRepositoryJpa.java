package br.cesar.rotavital.notificacao.dados;

import br.cesar.rotavital.notificacao.negocio.dominio.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificacaoRepositoryJpa extends JpaRepository<Notificacao, Long> {
    List<Notificacao> findTop10ByOrderByCriadaEmDesc();
    long countByLidaFalse();
}

