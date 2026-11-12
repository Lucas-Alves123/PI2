package br.cesar.rotavital.painel.apresentacao;

import br.cesar.rotavital.estoque.negocio.aplicacao.EstoqueService;
import br.cesar.rotavital.estoque.negocio.aplicacao.ResumoEstoque;
import br.cesar.rotavital.expedicao.negocio.aplicacao.ExpedicaoService;
import br.cesar.rotavital.notificacao.negocio.aplicacao.NotificacaoService;
import br.cesar.rotavital.requisicao.negocio.aplicacao.RequisicaoService;
import br.cesar.rotavital.requisicao.negocio.aplicacao.ResumoRequisicoes;
import br.cesar.rotavital.telemetria.negocio.aplicacao.TelemetriaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Controller
public class DashboardWebController {

    private final EstoqueService estoque;
    private final RequisicaoService requisicoes;
    private final ExpedicaoService expedicao;
    private final TelemetriaService telemetria;
    private final NotificacaoService notificacoes;

    public DashboardWebController(EstoqueService estoque, RequisicaoService requisicoes,
                                  ExpedicaoService expedicao, TelemetriaService telemetria,
                                  NotificacaoService notificacoes) {
        this.estoque = estoque;
        this.requisicoes = requisicoes;
        this.expedicao = expedicao;
        this.telemetria = telemetria;
        this.notificacoes = notificacoes;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        ResumoEstoque e = estoque.resumo(LocalDate.now());
        ResumoRequisicoes r = requisicoes.resumo();

        model.addAttribute("totalBolsas", e.total());
        model.addAttribute("bolsasDisponiveis", e.disponiveis());
        model.addAttribute("bolsasReservadas", e.reservadas());
        model.addAttribute("bolsasProximas", e.proximasDoVencimento());
        model.addAttribute("bolsasVencidas", e.vencidas());

        model.addAttribute("reqPendentes", r.pendentes());
        model.addAttribute("reqAtendidas", r.atendidas());
        model.addAttribute("reqEmExpedicao", r.emExpedicao());
        model.addAttribute("guiasEmTransito", expedicao.contarEmTransito());
        model.addAttribute("alertasAtivos", telemetria.contarAlertas());

        model.addAttribute("notificacoesRecentes", notificacoes.recentes());
        model.addAttribute("ultimasRequisicoes", requisicoes.listarRecentes());
        return "dashboard";
    }
}

