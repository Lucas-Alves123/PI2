package br.cesar.rotavital.hospitais.negocio.dominio;

import br.cesar.rotavital.shared.RegraDeNegocioException;
import jakarta.persistence.*;

@Entity
@Table(name = "unidade")
public class Unidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false)
    private boolean ativa = true;

    protected Unidade() {}

    public Unidade(Hospital hospital, String nome) {
        if (nome == null || nome.isBlank()) throw new RegraDeNegocioException("Nome da unidade é obrigatório.");
        this.hospital = hospital;
        this.nome = nome.trim();
        this.ativa = true;
    }

    public Long getId() { return id; }
    public Hospital getHospital() { return hospital; }
    public String getNome() { return nome; }
    public boolean isAtiva() { return ativa; }
}

