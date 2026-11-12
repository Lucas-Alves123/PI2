package br.cesar.rotavital.requisicao.apresentacao;

import br.cesar.rotavital.hospitais.negocio.aplicacao.HospitalService;
import br.cesar.rotavital.requisicao.negocio.aplicacao.RequisicaoService;
import br.cesar.rotavital.requisicao.negocio.dominio.Urgencia;
import br.cesar.rotavital.shared.Componente;
import br.cesar.rotavital.shared.TipoAbo;
import br.cesar.rotavital.shared.TipoRh;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/requisicoes")
public class RequisicoesWebController {

    private final RequisicaoService requisicaoService;
    private final HospitalService hospitalService;

    public RequisicoesWebController(RequisicaoService requisicaoService, HospitalService hospitalService) {
        this.requisicaoService = requisicaoService;
        this.hospitalService = hospitalService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("requisicoes", requisicaoService.listarRecentes());
        model.addAttribute("hospitais", hospitalService.listarAtivos());
        model.addAttribute("tiposAbo", TipoAbo.values());
        model.addAttribute("tiposRh", TipoRh.values());
        model.addAttribute("componentes", Componente.values());
        model.addAttribute("urgencias", Urgencia.values());
        return "requisicoes";
    }

    @PostMapping("/criar")
    public String criar(@RequestParam Long hospitalId,
                        @RequestParam TipoAbo abo,
                        @RequestParam TipoRh rh,
                        @RequestParam Componente componente,
                        @RequestParam int quantidade,
                        @RequestParam Urgencia urgencia,
                        @RequestParam(required = false) String observacao) {
        requisicaoService.registrar(hospitalId, abo, rh, componente, quantidade, urgencia, observacao);
        return "redirect:/requisicoes";
    }

    @PostMapping("/{id}/alocar")
    public String tentarAlocacao(@PathVariable Long id) {
        requisicaoService.tentarAlocar(id);
        return "redirect:/requisicoes";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id,
                           @RequestParam(defaultValue = "Cancelado via interface") String motivo) {
        requisicaoService.cancelar(id, motivo);
        return "redirect:/requisicoes";
    }
}

