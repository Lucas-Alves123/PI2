package br.cesar.rotavital.requisicao.dados;

import br.cesar.rotavital.requisicao.negocio.dominio.Alocacao;
import br.cesar.rotavital.requisicao.negocio.dominio.StatusAlocacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlocacaoRepositoryJpa extends JpaRepository<Alocacao, Long> {
    Optional<Alocacao> findByBolsaIdAndStatus(Long bolsaId, StatusAlocacao status);
    List<Alocacao> findByRequisicaoId(Long requisicaoId);
}

