package br.cesar.rotavital.requisicao.negocio.dominio;

import br.cesar.rotavital.shared.Componente;
import br.cesar.rotavital.shared.RegraDeNegocioException;
import br.cesar.rotavital.shared.TipoAbo;
import br.cesar.rotavital.shared.TipoRh;
import br.cesar.rotavital.shared.TipoSanguineo;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "requisicao")
public class Requisicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hospital_id", nullable = false)
    private Long hospitalId;

    @Column(name = "unidade_id")
    private Long unidadeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "abo_solicitado", nullable = false)
    private TipoAbo aboSolicitado;

    @Enumerated(EnumType.STRING)
    @Column(name = "rh_solicitado", nullable = false)
    private TipoRh rhSolicitado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Componente componente;

    @Column(nullable = false)
    private int quantidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Urgencia urgencia = Urgencia.ROTINA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusRequisicao status = StatusRequisicao.PENDENTE;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm = OffsetDateTime.now();

    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime atualizadoEm = OffsetDateTime.now();

    @OneToMany(mappedBy = "requisicao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Alocacao> alocacoes = new ArrayList<>();

    protected Requisicao() {}

    public Requisicao(Long hospitalId, Long unidadeId, TipoAbo abo, TipoRh rh,
                      Componente componente, int quantidade, Urgencia urgencia, String observacao) {
        if (hospitalId == null) throw new RegraDeNegocioException("Hospital é obrigatório.");
        if (abo == null || rh == null) throw new RegraDeNegocioException("Tipo sanguíneo é obrigatório.");
        if (componente == null) throw new RegraDeNegocioException("Hemocomponente é obrigatório.");
        if (quantidade <= 0) throw new RegraDeNegocioException("Quantidade deve ser maior que zero.");

        this.hospitalId = hospitalId;
        this.unidadeId = unidadeId;
        this.aboSolicitado = abo;
        this.rhSolicitado = rh;
        this.componente = componente;
        this.quantidade = quantidade;
        this.urgencia = urgencia != null ? urgencia : Urgencia.ROTINA;
        this.observacao = observacao;
        this.status = StatusRequisicao.PENDENTE;
    }

    public void adicionarAlocacao(Long bolsaId, LocalDate validade) {
        if (this.status == StatusRequisicao.CANCELADA || this.status == StatusRequisicao.CONCLUIDA) {
            throw new RegraDeNegocioException("Não é permitido alocar bolsas em requisição concluída ou cancelada.");
        }
        boolean jaExiste = alocacoes.stream().anyMatch(a -> a.getBolsaId().equals(bolsaId));
        if (jaExiste) {
            throw new RegraDeNegocioException("Bolsa já vinculada a esta requisição.");
        }

        Alocacao aloc = new Alocacao(this, bolsaId, validade);
        this.alocacoes.add(aloc);
        atualizarStatusAtendimento();
    }

    public void atualizarStatusAtendimento() {
        int totalAlocado = (int) alocacoes.stream()
                .filter(a -> a.getStatus() == StatusAlocacao.RESERVADA || a.getStatus() == StatusAlocacao.CONFIRMADA)
                .count();

        if (totalAlocado == 0) {
            this.status = StatusRequisicao.PENDENTE;
        } else if (totalAlocado < this.quantidade) {
            this.status = StatusRequisicao.PARCIALMENTE_ATENDIDA;
        } else {
            this.status = StatusRequisicao.ATENDIDA;
        }
        this.atualizadoEm = OffsetDateTime.now();
    }

    public void cancelar(String justificativa) {
        if (this.status == StatusRequisicao.EM_EXPEDICAO || this.status == StatusRequisicao.CONCLUIDA) {
            throw new RegraDeNegocioException("Não é possível cancelar requisição já despachada ou concluída.");
        }
        this.status = StatusRequisicao.CANCELADA;
        for (Alocacao a : this.alocacoes) {
            a.cancelar();
        }
        this.observacao = (this.observacao == null ? "" : this.observacao + " | ") + "Cancelado: " + justificativa;
        this.atualizadoEm = OffsetDateTime.now();
    }

    public void marcarEmExpedicao() {
        if (this.status != StatusRequisicao.ATENDIDA) {
            throw new RegraDeNegocioException("Apenas requisições totalmente atendidas podem avançar para expedição.");
        }
        this.status = StatusRequisicao.EM_EXPEDICAO;
        this.atualizadoEm = OffsetDateTime.now();
    }

    public void marcarConcluida() {
        this.status = StatusRequisicao.CONCLUIDA;
        this.atualizadoEm = OffsetDateTime.now();
    }

    public TipoSanguineo getTipoSanguineoSolicitado() {
        return new TipoSanguineo(aboSolicitado, rhSolicitado);
    }

    // Getters
    public Long getId() { return id; }
    public Long getHospitalId() { return hospitalId; }
    public Long getUnidadeId() { return unidadeId; }
    public TipoAbo getAboSolicitado() { return aboSolicitado; }
    public TipoRh getRhSolicitado() { return rhSolicitado; }
    public Componente getComponente() { return componente; }
    public int getQuantidade() { return quantidade; }
    public Urgencia getUrgencia() { return urgencia; }
    public StatusRequisicao getStatus() { return status; }
    public String getObservacao() { return observacao; }
    public OffsetDateTime getCriadoEm() { return criadoEm; }
    public OffsetDateTime getAtualizadoEm() { return atualizadoEm; }
    public List<Alocacao> getAlocacoes() { return alocacoes; }
}

