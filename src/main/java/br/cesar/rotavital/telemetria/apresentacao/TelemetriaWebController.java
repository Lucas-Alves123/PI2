package br.cesar.rotavital.telemetria.apresentacao;

import br.cesar.rotavital.telemetria.negocio.aplicacao.TelemetriaService;
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

    private final TelemetriaService telemetriaService;

    public TelemetriaWebController(TelemetriaService telemetriaService) {
        this.telemetriaService = telemetriaService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("leituras", telemetriaService.ultimasLeituras());
        model.addAttribute("limiteMin", telemetriaService.limiteMinimo());
        model.addAttribute("limiteMax", telemetriaService.limiteMaximo());
        return "telemetria";
    }

    @PostMapping("/simular")
    public String simularLeitura(@RequestParam Long guiaId,
                                 @RequestParam BigDecimal temperatura,
                                 @RequestParam(defaultValue = "-8.0578") BigDecimal latitude,
                                 @RequestParam(defaultValue = "-34.8829") BigDecimal longitude) {
        telemetriaService.registrarLeitura(guiaId, 1L, temperatura, latitude, longitude);
        return "redirect:/telemetria";
    }
}

