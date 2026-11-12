package br.cesar.rotavital.estoque.negocio.dominio;

import br.cesar.rotavital.shared.Componente;
import br.cesar.rotavital.shared.RegraDeNegocioException;
import br.cesar.rotavital.shared.TipoAbo;
import br.cesar.rotavital.shared.TipoRh;
import br.cesar.rotavital.shared.TipoSanguineo;
import br.cesar.rotavital.shared.Validade;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "bolsa")
public class Bolsa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAbo abo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoRh rh;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Componente componente;

    @Column(name = "data_coleta", nullable = false)
    private LocalDate dataColeta;

    @Column(name = "data_validade", nullable = false)
    private LocalDate dataValidade;

    @Column(length = 100)
    private String localizacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusBolsa status = StatusBolsa.DISPONIVEL;

    @Column(name = "cadastrado_em", nullable = false)
    private OffsetDateTime cadastradoEm = OffsetDateTime.now();

    protected Bolsa() {}

    public Bolsa(String codigo, TipoAbo abo, TipoRh rh, Componente componente,
                 LocalDate dataColeta, LocalDate dataValidade, String localizacao) {
        if (codigo == null || codigo.isBlank()) {
            throw new RegraDeNegocioException("Código da bolsa é obrigatório.");
        }
        Validade validade = Validade.posteriorA(dataColeta, dataValidade);
        this.codigo = codigo.trim().toUpperCase();
        this.abo = abo;
        this.rh = rh;
        this.componente = componente;
        this.dataColeta = dataColeta;
        this.dataValidade = validade.data();
        this.localizacao = localizacao;
        this.status = StatusBolsa.DISPONIVEL;
    }

    public Validade getValidade() {
        return new Validade(dataValidade);
    }

    public boolean estaVencida(LocalDate referencia) {
        return getValidade().estaVencidaEm(referencia);
    }

    public boolean estaDisponivel() {
        return this.status == StatusBolsa.DISPONIVEL;
    }

    public void reservar() {
        if (this.status != StatusBolsa.DISPONIVEL) {
            throw new RegraDeNegocioException("Apenas bolsas disponíveis podem ser reservadas. Status atual: " + this.status);
        }
        this.status = StatusBolsa.RESERVADA;
    }

    public void liberar() {
        if (this.status == StatusBolsa.RESERVADA || this.status == StatusBolsa.EM_SEPARACAO) {
            this.status = StatusBolsa.DISPONIVEL;
        }
    }

    public void descartar(String motivo) {
        this.status = StatusBolsa.DESCARTADA;
    }

    public void marcarComoVencida() {
        this.status = StatusBolsa.VENCIDA;
    }

    public TipoSanguineo getTipoSanguineo() {
        return new TipoSanguineo(abo, rh);
    }

    // Getters
    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public TipoAbo getAbo() { return abo; }
    public TipoRh getRh() { return rh; }
    public Componente getComponente() { return componente; }
    public LocalDate getDataColeta() { return dataColeta; }
    public LocalDate getDataValidade() { return dataValidade; }
    public String getLocalizacao() { return localizacao; }
    public StatusBolsa getStatus() { return status; }
    public OffsetDateTime getCadastradoEm() { return cadastradoEm; }
}

