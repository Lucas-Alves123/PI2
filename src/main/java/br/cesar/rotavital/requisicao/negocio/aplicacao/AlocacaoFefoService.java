package br.cesar.rotavital.requisicao.negocio.aplicacao;

import br.cesar.rotavital.estoque.negocio.aplicacao.BolsaCandidata;
import br.cesar.rotavital.estoque.negocio.aplicacao.EstoqueService;
import br.cesar.rotavital.requisicao.dados.RequisicaoRepositoryJpa;
import br.cesar.rotavital.requisicao.negocio.dominio.Requisicao;
import br.cesar.rotavital.requisicao.negocio.dominio.eventos.RequisicaoAtendida;
import br.cesar.rotavital.shared.RegraDeNegocioException;
import br.cesar.rotavital.shared.TipoSanguineo;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class AlocacaoFefoService {

    private final EstoqueService estoque;
    private final RequisicaoRepositoryJpa requisicaoRepository;
    private final ApplicationEventPublisher eventos;

    public AlocacaoFefoService(EstoqueService estoque,
                               RequisicaoRepositoryJpa requisicaoRepository,
                               ApplicationEventPublisher eventos) {
        this.estoque = estoque;
        this.requisicaoRepository = requisicaoRepository;
        this.eventos = eventos;
    }

    @Transactional
    public List<Long> alocarParaRequisicao(Long requisicaoId) {
        Requisicao req = requisicaoRepository.findById(requisicaoId)
                .orElseThrow(() -> new RegraDeNegocioException("Requisição não encontrada: " + requisicaoId));

        int necessarias = req.quantidadePendente();
        if (necessarias == 0) {
            return List.of();
        }

        LocalDate hoje = LocalDate.now();
        TipoSanguineo receptor = req.getTipoSanguineoSolicitado();
        List<Long> reservadas = new ArrayList<>();

        for (BolsaCandidata candidata : estoque.buscarCandidatasFefo(req.getComponente(), hoje)) {
            if (reservadas.size() >= necessarias) {
                break;
            }
            if (candidata.tipoSanguineo().podeDoarPara(receptor, req.getComponente())) {
                req.adicionarAlocacao(candidata.id(), candidata.validade(), hoje);
                estoque.reservarBolsa(candidata.id());
                reservadas.add(candidata.id());
            }
        }

        requisicaoRepository.save(req);

        if (req.estaAtendida() && !reservadas.isEmpty()) {
            eventos.publishEvent(new RequisicaoAtendida(req.getId(), req.getHospitalId(), req.bolsasAlocadas()));
        }
        return reservadas;
    }
}
