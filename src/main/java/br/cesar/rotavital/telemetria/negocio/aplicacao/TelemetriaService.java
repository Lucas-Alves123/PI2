package br.cesar.rotavital.telemetria.negocio.aplicacao;

import br.cesar.rotavital.telemetria.dados.LeituraTelemetriaRepositoryJpa;
import br.cesar.rotavital.telemetria.negocio.dominio.LeituraTelemetria;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TelemetriaService {

    private final LeituraTelemetriaRepositoryJpa leituraRepository;
    private final BigDecimal limiteMinimo;
    private final BigDecimal limiteMaximo;

    public TelemetriaService(LeituraTelemetriaRepositoryJpa leituraRepository,
                             @Value("${rotavital.temperatura.limite-minimo:2.0}") BigDecimal limiteMinimo,
                             @Value("${rotavital.temperatura.limite-maximo:6.0}") BigDecimal limiteMaximo) {
        this.leituraRepository = leituraRepository;
        this.limiteMinimo = limiteMinimo;
        this.limiteMaximo = limiteMaximo;
    }

    @Transactional(readOnly = true)
    public List<LeituraTelemetria> ultimasLeituras() {
        return leituraRepository.findTop20ByOrderByLidoEmDesc();
    }

    @Transactional
    public LeituraTelemetria registrarLeitura(Long guiaId, Long veiculoId, BigDecimal temperatura,
                                              BigDecimal latitude, BigDecimal longitude) {
        return leituraRepository.save(
                new LeituraTelemetria(guiaId, veiculoId, temperatura, latitude, longitude, limiteMinimo, limiteMaximo));
    }

    @Transactional(readOnly = true)
    public long contarAlertas() {
        return leituraRepository.countByAlertaTrue();
    }

    public BigDecimal limiteMinimo() { return limiteMinimo; }

    public BigDecimal limiteMaximo() { return limiteMaximo; }
}

