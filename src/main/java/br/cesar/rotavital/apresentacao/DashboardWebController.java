package br.cesar.rotavital.apresentacao;

import br.cesar.rotavital.estoque.dados.BolsaRepositoryJpa;
import br.cesar.rotavital.estoque.negocio.dominio.StatusBolsa;
import br.cesar.rotavital.expedicao.dados.GuiaExpedicaoRepositoryJpa;
import br.cesar.rotavital.expedicao.negocio.dominio.StatusExpedicao;
import br.cesar.rotavital.notificacao.dados.NotificacaoRepositoryJpa;
import br.cesar.rotavital.requisicao.dados.RequisicaoRepositoryJpa;
import br.cesar.rotavital.requisicao.negocio.dominio.StatusRequisicao;
import br.cesar.rotavital.telemetria.dados.LeituraTelemetriaRepositoryJpa;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Controller
public class DashboardWebController {

    private final BolsaRepositoryJpa bolsaRepository;
    private final RequisicaoRepositoryJpa requisicaoRepository;
    private final GuiaExpedicaoRepositoryJpa guiaRepository;
    private final LeituraTelemetriaRepositoryJpa telemetriaRepository;
    private final NotificacaoRepositoryJpa notificacaoRepository;

    public DashboardWebController(BolsaRepositoryJpa bolsaRepository,
                                  RequisicaoRepositoryJpa requisicaoRepository,
                                  GuiaExpedicaoRepositoryJpa guiaRepository,
                                  LeituraTelemetriaRepositoryJpa telemetriaRepository,
                                  NotificacaoRepositoryJpa notificacaoRepository) {
        this.bolsaRepository = bolsaRepository;
        this.requisicaoRepository = requisicaoRepository;
        this.guiaRepository = guiaRepository;
        this.telemetriaRepository = telemetriaRepository;
        this.notificacaoRepository = notificacaoRepository;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        LocalDate hoje = LocalDate.now();
        LocalDate limite7Dias = hoje.plusDays(7);

        long totalBolsas = bolsaRepository.count();
        long bolsasDisponiveis = bolsaRepository.countByStatus(StatusBolsa.DISPONIVEL);
        long bolsasReservadas = bolsaRepository.countByStatus(StatusBolsa.RESERVADA);
        long bolsasProximas = bolsaRepository.contarProximasVencimento(hoje, limite7Dias);
        long bolsasVencidas = bolsaRepository.contarVencidas(hoje);

        long reqPendentes = requisicaoRepository.countByStatus(StatusRequisicao.PENDENTE)
                + requisicaoRepository.countByStatus(StatusRequisicao.PARCIALMENTE_ATENDIDA);
        long reqAtendidas = requisicaoRepository.countByStatus(StatusRequisicao.ATENDIDA);
        long reqEmExpedicao = requisicaoRepository.countByStatus(StatusRequisicao.EM_EXPEDICAO);

        long guiasEmTransito = guiaRepository.countByStatus(StatusExpedicao.EM_TRANSITO);
        long alertasAtivos = telemetriaRepository.countByAlertaTrue();

        model.addAttribute("totalBolsas", totalBolsas);
        model.addAttribute("bolsasDisponiveis", bolsasDisponiveis);
        model.addAttribute("bolsasReservadas", bolsasReservadas);
        model.addAttribute("bolsasProximas", bolsasProximas);
        model.addAttribute("bolsasVencidas", bolsasVencidas);

        model.addAttribute("reqPendentes", reqPendentes);
        model.addAttribute("reqAtendidas", reqAtendidas);
        model.addAttribute("reqEmExpedicao", reqEmExpedicao);
        model.addAttribute("guiasEmTransito", guiasEmTransito);
        model.addAttribute("alertasAtivos", alertasAtivos);

        model.addAttribute("notificacoesRecentes", notificacaoRepository.findTop10ByOrderByCriadaEmDesc());
        model.addAttribute("ultimasRequisicoes", requisicaoRepository.findAllByOrderByCriadoEmDesc());

        return "dashboard";
    }
}

