package br.cesar.rotavital.hospitais.apresentacao;

import br.cesar.rotavital.hospitais.negocio.aplicacao.HospitalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/hospitais")
public class HospitaisWebController {

    private final HospitalService hospitalService;

    public HospitaisWebController(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("hospitais", hospitalService.listarTodos());
        return "hospitais";
    }

    @PostMapping("/cadastrar")
    public String cadastrar(@RequestParam String nome,
                            @RequestParam String cnpj,
                            @RequestParam String logradouro,
                            @RequestParam String cidade,
                            @RequestParam String estado,
                            @RequestParam String telefone,
                            @RequestParam String email,
                            @RequestParam String responsavel) {
        hospitalService.cadastrar(nome, cnpj, logradouro, cidade, estado, telefone, email, responsavel);
        return "redirect:/hospitais";
    }
}

