package br.cesar.rotavital.requisicao.negocio.dominio;

import br.cesar.rotavital.shared.RegraDeNegocioException;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "alocacao")
public class Alocacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requisicao_id", nullable = false)
    private Requisicao requisicao;

    @Column(name = "bolsa_id", nullable = false)
    private Long bolsaId;

    @Column(name = "data_validade", nullable = false)
    private LocalDate dataValidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAlocacao status = StatusAlocacao.RESERVADA;

    @Column(name = "alocado_em", nullable = false)
    private OffsetDateTime alocadoEm = OffsetDateTime.now();

    protected Alocacao() {}

    public Alocacao(Requisicao requisicao, Long bolsaId, LocalDate dataValidade) {
        if (requisicao == null) throw new RegraDeNegocioException("Requisição vinculada é obrigatória.");
        if (bolsaId == null) throw new RegraDeNegocioException("ID da bolsa é obrigatório.");
        if (dataValidade == null) throw new RegraDeNegocioException("Data de validade da bolsa é obrigatória.");
        this.requisicao = requisicao;
        this.bolsaId = bolsaId;
        this.dataValidade = dataValidade;
        this.status = StatusAlocacao.RESERVADA;
    }

    public void confirmar() {
        this.status = StatusAlocacao.CONFIRMADA;
    }

    public void cancelar() {
        this.status = StatusAlocacao.CANCELADA;
    }

    public Long getId() { return id; }
    public Requisicao getRequisicao() { return requisicao; }
    public Long getBolsaId() { return bolsaId; }
    public LocalDate getDataValidade() { return dataValidade; }
    public StatusAlocacao getStatus() { return status; }
    public OffsetDateTime getAlocadoEm() { return alocadoEm; }
}

