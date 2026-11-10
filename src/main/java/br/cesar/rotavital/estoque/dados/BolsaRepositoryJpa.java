package br.cesar.rotavital.estoque.dados;

import br.cesar.rotavital.estoque.negocio.dominio.Bolsa;
import br.cesar.rotavital.estoque.negocio.dominio.StatusBolsa;
import br.cesar.rotavital.shared.Componente;
import br.cesar.rotavital.shared.TipoAbo;
import br.cesar.rotavital.shared.TipoRh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BolsaRepositoryJpa extends JpaRepository<Bolsa, Long> {

    Optional<Bolsa> findByCodigo(String codigo);

    List<Bolsa> findByStatus(StatusBolsa status);

    @Query("SELECT b FROM Bolsa b WHERE b.status = :status AND b.componente = :componente AND b.dataValidade >= :hoje ORDER BY b.dataValidade ASC")
    List<Bolsa> buscarCandidatasFEFO(
            @Param("status") StatusBolsa status,
            @Param("componente") Componente componente,
            @Param("hoje") LocalDate hoje
    );

    long countByStatus(StatusBolsa status);

    @Query("SELECT COUNT(b) FROM Bolsa b WHERE b.status = 'DISPONIVEL' AND b.dataValidade BETWEEN :hoje AND :limite")
    long contarProximasVencimento(@Param("hoje") LocalDate hoje, @Param("limite") LocalDate limite);

    @Query("SELECT COUNT(b) FROM Bolsa b WHERE b.dataValidade < :hoje AND b.status <> 'DESCARTADA'")
    long contarVencidas(@Param("hoje") LocalDate hoje);
}

