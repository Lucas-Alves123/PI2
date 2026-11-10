package br.cesar.rotavital.hospitais.dados;

import br.cesar.rotavital.hospitais.negocio.dominio.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface HospitalRepositoryJpa extends JpaRepository<Hospital, Long> {
    Optional<Hospital> findByCnpj(String cnpj);
    List<Hospital> findByAtivoTrue();
}

