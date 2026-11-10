package br.cesar.rotavital.apresentacao;

import br.cesar.rotavital.hospitais.dados.HospitalRepositoryJpa;
import br.cesar.rotavital.requisicao.dados.RequisicaoRepositoryJpa;
import br.cesar.rotavital.requisicao.negocio.aplicacao.AlocacaoFefoService;
import br.cesar.rotavital.requisicao.negocio.dominio.Requisicao;
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

    private final RequisicaoRepositoryJpa requisicaoRepository;
    private final HospitalRepositoryJpa hospitalRepository;
    private final AlocacaoFefoService alocacaoService;

    public RequisicoesWebController(RequisicaoRepositoryJpa requisicaoRepository,
                                     HospitalRepositoryJpa hospitalRepository,
                                     AlocacaoFefoService alocacaoService) {
        this.requisicaoRepository = requisicaoRepository;
        this.hospitalRepository = hospitalRepository;
        this.alocacaoService = alocacaoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("requisicoes", requisicaoRepository.findAllByOrderByCriadoEmDesc());
        model.addAttribute("hospitais", hospitalRepository.findByAtivoTrue());
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
        Requisicao req = new Requisicao(hospitalId, null, abo, rh, componente, quantidade, urgencia, observacao);
        requisicaoRepository.save(req);
        // Tenta alocação inteligente FEFO imediata
        alocacaoService.alocarParaRequisicao(req.getId());
        return "redirect:/requisicoes";
    }

    @PostMapping("/{id}/alocar")
    public String tentarAlocacao(@PathVariable Long id) {
        alocacaoService.alocarParaRequisicao(id);
        return "redirect:/requisicoes";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, @RequestParam(defaultValue = "Cancelado via interface") String motivo) {
        requisicaoRepository.findById(id).ifPresent(r -> {
            r.cancelar(motivo);
            requisicaoRepository.save(r);
        });
        return "redirect:/requisicoes";
    }
}

