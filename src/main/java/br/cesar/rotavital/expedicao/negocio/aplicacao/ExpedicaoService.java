package br.cesar.rotavital.expedicao.negocio.aplicacao;

import br.cesar.rotavital.expedicao.dados.GuiaExpedicaoRepositoryJpa;
import br.cesar.rotavital.expedicao.negocio.dominio.GuiaExpedicao;
import br.cesar.rotavital.expedicao.negocio.dominio.StatusExpedicao;
import br.cesar.rotavital.expedicao.negocio.dominio.eventos.GuiaEntregue;
import br.cesar.rotavital.requisicao.negocio.aplicacao.RequisicaoParaExpedicao;
import br.cesar.rotavital.requisicao.negocio.aplicacao.RequisicaoService;
import br.cesar.rotavital.shared.RegraDeNegocioException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExpedicaoService {

    private static final Long VEICULO_PADRAO = 1L;

    private final GuiaExpedicaoRepositoryJpa guiaRepository;
    private final RequisicaoService requisicoes;
    private final ApplicationEventPublisher eventos;

    public ExpedicaoService(GuiaExpedicaoRepositoryJpa guiaRepository,
                            RequisicaoService requisicoes,
                            ApplicationEventPublisher eventos) {
        this.guiaRepository = guiaRepository;
        this.requisicoes = requisicoes;
        this.eventos = eventos;
    }

    @Transactional(readOnly = true)
    public List<GuiaExpedicao> listarGuias() {
        return guiaRepository.findAll();
    }

    @Transactional
    public GuiaExpedicao emitirGuia(Long requisicaoId, String responsavel) {
        RequisicaoParaExpedicao req = requisicoes.iniciarExpedicao(requisicaoId);
        String numero = "GUIA-" + System.currentTimeMillis() % 1_000_000;
        GuiaExpedicao guia = new GuiaExpedicao(numero, req.requisicaoId(), req.hospitalId(), VEICULO_PADRAO, responsavel);
        req.bolsas().forEach(guia::adicionarBolsa);
        return guiaRepository.save(guia);
    }

    @Transactional
    public void despachar(Long guiaId) {
        buscar(guiaId).iniciarTransito();
    }

    @Transactional
    public void confirmarEntrega(Long guiaId) {
        GuiaExpedicao guia = buscar(guiaId);
        guia.confirmarEntrega();
        eventos.publishEvent(new GuiaEntregue(guia.getId(), guia.getNumero(), guia.getRequisicaoId(), guia.getHospitalId()));
    }

    @Transactional(readOnly = true)
    public long contarEmTransito() {
        return guiaRepository.countByStatus(StatusExpedicao.EM_TRANSITO);
    }

    private GuiaExpedicao buscar(Long guiaId) {
        return guiaRepository.findById(guiaId)
                .orElseThrow(() -> new RegraDeNegocioException("Guia não encontrada: " + guiaId));
    }
}

