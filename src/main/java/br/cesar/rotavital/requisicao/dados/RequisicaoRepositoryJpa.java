package br.cesar.rotavital.requisicao.dados;

import br.cesar.rotavital.requisicao.negocio.dominio.Requisicao;
import br.cesar.rotavital.requisicao.negocio.dominio.StatusRequisicao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequisicaoRepositoryJpa extends JpaRepository<Requisicao, Long> {
    List<Requisicao> findByStatus(StatusRequisicao status);
    List<Requisicao> findAllByOrderByCriadoEmDesc();
    long countByStatus(StatusRequisicao status);
}

