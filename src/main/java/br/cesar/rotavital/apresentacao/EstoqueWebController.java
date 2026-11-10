package br.cesar.rotavital.apresentacao;

import br.cesar.rotavital.estoque.dados.BolsaRepositoryJpa;
import br.cesar.rotavital.estoque.negocio.dominio.Bolsa;
import br.cesar.rotavital.estoque.negocio.dominio.StatusBolsa;
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

    private final BolsaRepositoryJpa bolsaRepository;

    public EstoqueWebController(BolsaRepositoryJpa bolsaRepository) {
        this.bolsaRepository = bolsaRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("bolsas", bolsaRepository.findAll());
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
        Bolsa nova = new Bolsa(
                codigo,
                abo,
                rh,
                componente,
                LocalDate.parse(dataColeta),
                LocalDate.parse(dataValidade),
                localizacao
        );
        bolsaRepository.save(nova);
        return "redirect:/estoque";
    }

    @PostMapping("/descartar")
    public String descartar(@RequestParam Long bolsaId) {
        bolsaRepository.findById(bolsaId).ifPresent(b -> {
            b.descartar("Descarte operacional");
            bolsaRepository.save(b);
        });
        return "redirect:/estoque";
    }
}

