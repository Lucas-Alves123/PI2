package br.cesar.rotavital.apresentacao;

import br.cesar.rotavital.expedicao.dados.GuiaExpedicaoRepositoryJpa;
import br.cesar.rotavital.expedicao.negocio.dominio.GuiaExpedicao;
import br.cesar.rotavital.requisicao.dados.RequisicaoRepositoryJpa;
import br.cesar.rotavital.requisicao.negocio.dominio.Requisicao;
import br.cesar.rotavital.requisicao.negocio.dominio.StatusRequisicao;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/expedicao")
public class ExpedicaoWebController {

    private final GuiaExpedicaoRepositoryJpa guiaRepository;
    private final RequisicaoRepositoryJpa requisicaoRepository;

    public ExpedicaoWebController(GuiaExpedicaoRepositoryJpa guiaRepository,
                                  RequisicaoRepositoryJpa requisicaoRepository) {
        this.guiaRepository = guiaRepository;
        this.requisicaoRepository = requisicaoRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("guias", guiaRepository.findAll());
        model.addAttribute("prontasParaExpedir", requisicaoRepository.findByStatus(StatusRequisicao.ATENDIDA));
        return "expedicao";
    }

    @PostMapping("/gerar-guia")
    public String gerarGuia(@RequestParam Long requisicaoId,
                            @RequestParam(defaultValue = "Motorista Sintético") String responsavel) {
        Requisicao req = requisicaoRepository.findById(requisicaoId).orElseThrow();
        String numeroGuia = "GUIA-" + System.currentTimeMillis() % 100000;
        GuiaExpedicao guia = new GuiaExpedicao(numeroGuia, req.getId(), req.getHospitalId(), 1L, responsavel);
        req.getAlocacoes().forEach(a -> guia.adicionarBolsa(a.getBolsaId()));

        guiaRepository.save(guia);
        req.marcarEmExpedicao();
        requisicaoRepository.save(req);

        return "redirect:/expedicao";
    }

    @PostMapping("/{id}/despachar")
    public String despachar(@PathVariable Long id) {
        guiaRepository.findById(id).ifPresent(g -> {
            g.iniciarTransito();
            guiaRepository.save(g);
        });
        return "redirect:/expedicao";
    }

    @PostMapping("/{id}/entregar")
    public String entregar(@PathVariable Long id) {
        guiaRepository.findById(id).ifPresent(g -> {
            g.confirmarEntrega();
            guiaRepository.save(g);

            requisicaoRepository.findById(g.getRequisicaoId()).ifPresent(r -> {
                r.marcarConcluida();
                requisicaoRepository.save(r);
            });
        });
        return "redirect:/expedicao";
    }
}

