package br.cesar.rotavital.expedicao.negocio.dominio;

import br.cesar.rotavital.shared.RegraDeNegocioException;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "guia_expedicao")
public class GuiaExpedicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String numero;

    @Column(name = "requisicao_id", nullable = false)
    private Long requisicaoId;

    @Column(name = "hospital_id", nullable = false)
    private Long hospitalId;

    @Column(name = "veiculo_id")
    private Long veiculoId;

    @Column(length = 150)
    private String responsavel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusExpedicao status = StatusExpedicao.PREPARANDO;

    @Column(name = "saida_em")
    private OffsetDateTime saidaEm;

    @Column(name = "entrega_em")
    private OffsetDateTime entregaEm;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm = OffsetDateTime.now();

    @ElementCollection
    @CollectionTable(name = "guia_bolsa", joinColumns = @JoinColumn(name = "guia_id"))
    @Column(name = "bolsa_id")
    private Set<Long> bolsasIds = new HashSet<>();

    protected GuiaExpedicao() {}

    public GuiaExpedicao(String numero, Long requisicaoId, Long hospitalId, Long veiculoId, String responsavel) {
        if (numero == null || numero.isBlank()) throw new RegraDeNegocioException("Número da guia é obrigatório.");
        if (requisicaoId == null) throw new RegraDeNegocioException("Requisição é obrigatória.");
        if (hospitalId == null) throw new RegraDeNegocioException("Hospital é obrigatório.");
        this.numero = numero.trim().toUpperCase();
        this.requisicaoId = requisicaoId;
        this.hospitalId = hospitalId;
        this.veiculoId = veiculoId;
        this.responsavel = responsavel;
        this.status = StatusExpedicao.PREPARANDO;
    }

    public void adicionarBolsa(Long bolsaId) {
        this.bolsasIds.add(bolsaId);
    }

    public void iniciarTransito() {
        if (this.status != StatusExpedicao.PREPARANDO) {
            throw new RegraDeNegocioException("Apenas guias em preparação podem iniciar trânsito.");
        }
        this.status = StatusExpedicao.EM_TRANSITO;
        this.saidaEm = OffsetDateTime.now();
    }

    public void confirmarEntrega() {
        if (this.status != StatusExpedicao.EM_TRANSITO) {
            throw new RegraDeNegocioException("Apenas guias em trânsito podem ser entregues.");
        }
        this.status = StatusExpedicao.ENTREGUE;
        this.entregaEm = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public String getNumero() { return numero; }
    public Long getRequisicaoId() { return requisicaoId; }
    public Long getHospitalId() { return hospitalId; }
    public Long getVeiculoId() { return veiculoId; }
    public String getResponsavel() { return responsavel; }
    public StatusExpedicao getStatus() { return status; }
    public OffsetDateTime getSaidaEm() { return saidaEm; }
    public OffsetDateTime getEntregaEm() { return entregaEm; }
    public Set<Long> getBolsasIds() { return bolsasIds; }
}

