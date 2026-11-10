package br.cesar.rotavital.apresentacao;

import br.cesar.rotavital.hospitais.dados.HospitalRepositoryJpa;
import br.cesar.rotavital.hospitais.negocio.dominio.Hospital;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/hospitais")
public class HospitaisWebController {

    private final HospitalRepositoryJpa hospitalRepository;

    public HospitaisWebController(HospitalRepositoryJpa hospitalRepository) {
        this.hospitalRepository = hospitalRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("hospitais", hospitalRepository.findAll());
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
        Hospital h = new Hospital(nome, cnpj, logradouro, cidade, estado, telefone, email, responsavel);
        hospitalRepository.save(h);
        return "redirect:/hospitais";
    }
}

