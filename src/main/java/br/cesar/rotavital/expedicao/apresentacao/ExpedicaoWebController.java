package br.cesar.rotavital.expedicao.apresentacao;

import br.cesar.rotavital.expedicao.negocio.aplicacao.ExpedicaoService;
import br.cesar.rotavital.requisicao.negocio.aplicacao.RequisicaoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/expedicao")
public class ExpedicaoWebController {

    private final ExpedicaoService expedicaoService;
    private final RequisicaoService requisicaoService;

    public ExpedicaoWebController(ExpedicaoService expedicaoService, RequisicaoService requisicaoService) {
        this.expedicaoService = expedicaoService;
        this.requisicaoService = requisicaoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("guias", expedicaoService.listarGuias());
        model.addAttribute("prontasParaExpedir", requisicaoService.listarProntasParaExpedicao());
        return "expedicao";
    }

    @PostMapping("/gerar-guia")
    public String gerarGuia(@RequestParam Long requisicaoId,
                            @RequestParam(defaultValue = "Motorista Sintético") String responsavel) {
        expedicaoService.emitirGuia(requisicaoId, responsavel);
        return "redirect:/expedicao";
    }

    @PostMapping("/{id}/despachar")
    public String despachar(@PathVariable Long id) {
        expedicaoService.despachar(id);
        return "redirect:/expedicao";
    }

    @PostMapping("/{id}/entregar")
    public String entregar(@PathVariable Long id) {
        expedicaoService.confirmarEntrega(id);
        return "redirect:/expedicao";
    }
}

