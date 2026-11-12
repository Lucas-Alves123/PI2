package br.cesar.rotavital.estoque.apresentacao;

import br.cesar.rotavital.estoque.negocio.aplicacao.EstoqueService;
import br.cesar.rotavital.shared.Componente;
import br.cesar.rotavital.shared.TipoAbo;
import br.cesar.rotavital.shared.TipoRh;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/estoque")
public class EstoqueWebController {

    private final EstoqueService estoqueService;

    public EstoqueWebController(EstoqueService estoqueService) {
        this.estoqueService = estoqueService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("bolsas", estoqueService.listarBolsas());
        model.addAttribute("tiposAbo", TipoAbo.values());
        model.addAttribute("tiposRh", TipoRh.values());
        model.addAttribute("componentes", Componente.values());
        return "estoque";
    }

    @PostMapping("/cadastrar")
    public String cadastrar(@RequestParam String codigo,
                            @RequestParam TipoAbo abo,
                            @RequestParam TipoRh rh,
                            @RequestParam Componente componente,
                            @RequestParam String dataColeta,
                            @RequestParam String dataValidade,
                            @RequestParam String localizacao) {
        estoqueService.registrarBolsa(codigo, abo, rh, componente,
                LocalDate.parse(dataColeta), LocalDate.parse(dataValidade), localizacao);
        return "redirect:/estoque";
    }

    @PostMapping("/descartar")
    public String descartar(@RequestParam Long bolsaId) {
        estoqueService.descartarBolsa(bolsaId, "Descarte operacional");
        return "redirect:/estoque";
    }
}

