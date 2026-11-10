package br.cesar.rotavital.telemetria.negocio.dominio;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "leitura_telemetria")
public class LeituraTelemetria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "guia_id")
    private Long guiaId;

    @Column(name = "veiculo_id")
    private Long veiculoId;

    @Column(precision = 5, scale = 2)
    private BigDecimal temperatura;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(nullable = false)
    private boolean alerta = false;

    @Column(name = "lido_em", nullable = false)
    private OffsetDateTime lidoEm = OffsetDateTime.now();

    protected LeituraTelemetria() {}

    public LeituraTelemetria(Long guiaId, Long veiculoId, BigDecimal temperatura,
                             BigDecimal latitude, BigDecimal longitude,
                             BigDecimal limiteMin, BigDecimal limiteMax) {
        this.guiaId = guiaId;
        this.veiculoId = veiculoId;
        this.temperatura = temperatura;
        this.latitude = latitude;
        this.longitude = longitude;
        this.lidoEm = OffsetDateTime.now();

        if (temperatura != null && limiteMin != null && limiteMax != null) {
            this.alerta = (temperatura.compareTo(limiteMin) < 0 || temperatura.compareTo(limiteMax) > 0);
        }
    }

    public Long getId() { return id; }
    public Long getGuiaId() { return guiaId; }
    public Long getVeiculoId() { return veiculoId; }
    public BigDecimal getTemperatura() { return temperatura; }
    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public boolean isAlerta() { return alerta; }
    public OffsetDateTime getLidoEm() { return lidoEm; }
}

