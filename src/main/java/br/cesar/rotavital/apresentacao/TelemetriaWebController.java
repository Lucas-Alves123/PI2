package br.cesar.rotavital.apresentacao;

import br.cesar.rotavital.telemetria.dados.LeituraTelemetriaRepositoryJpa;
import br.cesar.rotavital.telemetria.negocio.dominio.LeituraTelemetria;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
@RequestMapping("/telemetria")
public class TelemetriaWebController {

    private final LeituraTelemetriaRepositoryJpa telemetriaRepository;

    @Value("${rotavital.temperatura.limite-minimo:2.0}")
    private BigDecimal limiteMin;

    @Value("${rotavital.temperatura.limite-maximo:6.0}")
    private BigDecimal limiteMax;

    public TelemetriaWebController(LeituraTelemetriaRepositoryJpa telemetriaRepository) {
        this.telemetriaRepository = telemetriaRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("leituras", telemetriaRepository.findTop20ByOrderByLidoEmDesc());
        model.addAttribute("limiteMin", limiteMin);
        model.addAttribute("limiteMax", limiteMax);
        return "telemetria";
    }

    @PostMapping("/simular")
    public String simularLeitura(@RequestParam Long guiaId,
                                 @RequestParam BigDecimal temperatura,
                                 @RequestParam(defaultValue = "-8.0578") BigDecimal latitude,
                                 @RequestParam(defaultValue = "-34.8829") BigDecimal longitude) {
        LeituraTelemetria l = new LeituraTelemetria(guiaId, 1L, temperatura, latitude, longitude, limiteMin, limiteMax);
        telemetriaRepository.save(l);
        return "redirect:/telemetria";
    }
}

