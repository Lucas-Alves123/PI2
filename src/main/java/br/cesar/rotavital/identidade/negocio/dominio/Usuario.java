package br.cesar.rotavital.identidade.negocio.dominio;

import br.cesar.rotavital.shared.RegraDeNegocioException;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String login;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PerfilUsuario perfil;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm = OffsetDateTime.now();

    protected Usuario() {}

    public Usuario(String login, String senhaHash, String nome, String email, PerfilUsuario perfil) {
        if (login == null || login.isBlank()) throw new RegraDeNegocioException("Login é obrigatório.");
        if (senhaHash == null || senhaHash.isBlank()) throw new RegraDeNegocioException("Senha é obrigatória.");
        this.login = login.trim().toLowerCase();
        this.senhaHash = senhaHash;
        this.nome = nome;
        this.email = email;
        this.perfil = perfil != null ? perfil : PerfilUsuario.ATENDIMENTO;
        this.ativo = true;
    }

    public Long getId() { return id; }
    public String getLogin() { return login; }
    public String getSenhaHash() { return senhaHash; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public PerfilUsuario getPerfil() { return perfil; }
    public boolean isAtivo() { return ativo; }
}

