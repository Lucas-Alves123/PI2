package br.cesar.rotavital.requisicao.negocio.aplicacao;

import br.cesar.rotavital.estoque.negocio.aplicacao.EstoqueService;
import br.cesar.rotavital.hospitais.negocio.aplicacao.HospitalService;
import br.cesar.rotavital.requisicao.dados.RequisicaoRepositoryJpa;
import br.cesar.rotavital.requisicao.negocio.dominio.Requisicao;
import br.cesar.rotavital.requisicao.negocio.dominio.StatusRequisicao;
import br.cesar.rotavital.requisicao.negocio.dominio.Urgencia;
import br.cesar.rotavital.shared.Componente;
import br.cesar.rotavital.shared.RegraDeNegocioException;
import br.cesar.rotavital.shared.TipoAbo;
import br.cesar.rotavital.shared.TipoRh;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RequisicaoService {

    private final RequisicaoRepositoryJpa requisicaoRepository;
    private final AlocacaoFefoService alocacaoFefo;
    private final HospitalService hospitais;
    private final EstoqueService estoque;

    public RequisicaoService(RequisicaoRepositoryJpa requisicaoRepository,
                             AlocacaoFefoService alocacaoFefo,
                             HospitalService hospitais,
                             EstoqueService estoque) {
        this.requisicaoRepository = requisicaoRepository;
        this.alocacaoFefo = alocacaoFefo;
        this.hospitais = hospitais;
        this.estoque = estoque;
    }

    @Transactional
    public Requisicao registrar(Long hospitalId, TipoAbo abo, TipoRh rh, Componente componente,
                                int quantidade, Urgencia urgencia, String observacao) {
        if (!hospitais.estaAtivo(hospitalId)) {
            throw new RegraDeNegocioException("Hospital inexistente ou inativo: " + hospitalId);
        }
        Requisicao req = requisicaoRepository.save(
                new Requisicao(hospitalId, null, abo, rh, componente, quantidade, urgencia, observacao));
        alocacaoFefo.alocarParaRequisicao(req.getId());
        return buscar(req.getId());
    }

    @Transactional
    public void tentarAlocar(Long requisicaoId) {
        alocacaoFefo.alocarParaRequisicao(requisicaoId);
    }

    @Transactional
    public void cancelar(Long requisicaoId, String motivo) {
        Requisicao req = buscar(requisicaoId);
        List<Long> bolsas = req.bolsasAlocadas();
        req.cancelar(motivo);
        bolsas.forEach(estoque::liberarBolsa);
    }

    @Transactional
    public RequisicaoParaExpedicao iniciarExpedicao(Long requisicaoId) {
        Requisicao req = buscar(requisicaoId);
        req.marcarEmExpedicao();
        return new RequisicaoParaExpedicao(req.getId(), req.getHospitalId(), req.bolsasAlocadas());
    }

    @Transactional
    public void concluir(Long requisicaoId) {
        buscar(requisicaoId).marcarConcluida();
    }

    @Transactional(readOnly = true)
    public Requisicao buscar(Long requisicaoId) {
        return requisicaoRepository.findById(requisicaoId)
                .orElseThrow(() -> new RegraDeNegocioException("Requisição não encontrada: " + requisicaoId));
    }

    @Transactional(readOnly = true)
    public List<Requisicao> listarRecentes() {
        return requisicaoRepository.findAllByOrderByCriadoEmDesc();
    }

    @Transactional(readOnly = true)
    public List<Requisicao> listarProntasParaExpedicao() {
        return requisicaoRepository.findByStatus(StatusRequisicao.ATENDIDA);
    }

    @Transactional(readOnly = true)
    public ResumoRequisicoes resumo() {
        return new ResumoRequisicoes(
                requisicaoRepository.countByStatus(StatusRequisicao.PENDENTE)
                        + requisicaoRepository.countByStatus(StatusRequisicao.PARCIALMENTE_ATENDIDA),
                requisicaoRepository.countByStatus(StatusRequisicao.ATENDIDA),
                requisicaoRepository.countByStatus(StatusRequisicao.EM_EXPEDICAO));
    }
}

