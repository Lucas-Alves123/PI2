package br.cesar.rotavital.hospitais.negocio.aplicacao;

import br.cesar.rotavital.hospitais.dados.HospitalRepositoryJpa;
import br.cesar.rotavital.hospitais.negocio.dominio.Hospital;
import br.cesar.rotavital.shared.RegraDeNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HospitalService {

    private final HospitalRepositoryJpa hospitalRepository;

    public HospitalService(HospitalRepositoryJpa hospitalRepository) {
        this.hospitalRepository = hospitalRepository;
    }

    @Transactional(readOnly = true)
    public List<Hospital> listarTodos() {
        return hospitalRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Hospital> listarAtivos() {
        return hospitalRepository.findByAtivoTrue();
    }

    @Transactional(readOnly = true)
    public boolean estaAtivo(Long hospitalId) {
        return hospitalRepository.findById(hospitalId).map(Hospital::isAtivo).orElse(false);
    }

    @Transactional
    public Hospital cadastrar(String nome, String cnpj, String logradouro, String cidade, String estado,
                              String telefone, String email, String responsavel) {
        if (hospitalRepository.findByCnpj(cnpj).isPresent()) {
            throw new RegraDeNegocioException("Já existe hospital com o CNPJ " + cnpj);
        }
        return hospitalRepository.save(new Hospital(nome, cnpj, logradouro, cidade, estado, telefone, email, responsavel));
    }
}

