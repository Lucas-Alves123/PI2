package br.cesar.rotavital.hospitais.negocio.dominio;

import br.cesar.rotavital.shared.RegraDeNegocioException;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hospital")
public class Hospital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 18)
    private String cnpj;

    @Column(length = 200)
    private String logradouro;

    @Column(length = 100)
    private String cidade;

    @Column(length = 2)
    private String estado;

    @Column(length = 20)
    private String telefone;

    @Column(length = 100)
    private String email;

    @Column(length = 150)
    private String responsavel;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm = OffsetDateTime.now();

    @OneToMany(mappedBy = "hospital", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Unidade> unidades = new ArrayList<>();

    protected Hospital() {}

    public Hospital(String nome, String cnpj, String logradouro, String cidade,
                    String estado, String telefone, String email, String responsavel) {
        if (nome == null || nome.isBlank()) throw new RegraDeNegocioException("Nome do hospital é obrigatório.");
        if (cnpj == null || cnpj.isBlank()) throw new RegraDeNegocioException("CNPJ é obrigatório.");
        this.nome = nome.trim();
        this.cnpj = cnpj.trim();
        this.logradouro = logradouro;
        this.cidade = cidade;
        this.estado = estado;
        this.telefone = telefone;
        this.email = email;
        this.responsavel = responsavel;
        this.ativo = true;
    }

    public void adicionarUnidade(String nomeUnidade) {
        Unidade u = new Unidade(this, nomeUnidade);
        this.unidades.add(u);
    }

    public void desativar() { this.ativo = false; }
    public void ativar() { this.ativo = true; }

    // Getters
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getCnpj() { return cnpj; }
    public String getLogradouro() { return logradouro; }
    public String getCidade() { return cidade; }
    public String getEstado() { return estado; }
    public String getTelefone() { return telefone; }
    public String getEmail() { return email; }
    public String getResponsavel() { return responsavel; }
    public boolean isAtivo() { return ativo; }
    public OffsetDateTime getCriadoEm() { return criadoEm; }
    public List<Unidade> getUnidades() { return unidades; }
}

