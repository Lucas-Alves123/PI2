package br.cesar.rotavital.identidade.dados;

import br.cesar.rotavital.identidade.negocio.dominio.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepositoryJpa extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByLogin(String login);
}

