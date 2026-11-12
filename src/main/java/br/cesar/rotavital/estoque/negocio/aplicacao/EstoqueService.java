package br.cesar.rotavital.estoque.negocio.aplicacao;

import br.cesar.rotavital.estoque.dados.BolsaRepositoryJpa;
import br.cesar.rotavital.estoque.negocio.dominio.Bolsa;
import br.cesar.rotavital.estoque.negocio.dominio.StatusBolsa;
import br.cesar.rotavital.shared.Componente;
import br.cesar.rotavital.shared.RegraDeNegocioException;
import br.cesar.rotavital.shared.TipoAbo;
import br.cesar.rotavital.shared.TipoRh;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class EstoqueService {

    private final BolsaRepositoryJpa bolsaRepository;
    private final int diasAlertaVencimento;

    public EstoqueService(BolsaRepositoryJpa bolsaRepository,
                          @Value("${rotavital.estoque.dias-alerta-vencimento:7}") int diasAlertaVencimento) {
        this.bolsaRepository = bolsaRepository;
        this.diasAlertaVencimento = diasAlertaVencimento;
    }

    @Transactional(readOnly = true)
    public List<Bolsa> listarBolsas() {
        return bolsaRepository.findAll();
    }

    @Transactional
    public Bolsa registrarBolsa(String codigo, TipoAbo abo, TipoRh rh, Componente componente,
                                LocalDate dataColeta, LocalDate dataValidade, String localizacao) {
        if (bolsaRepository.findByCodigo(codigo.trim().toUpperCase()).isPresent()) {
            throw new RegraDeNegocioException("Já existe bolsa com o código " + codigo);
        }
        return bolsaRepository.save(new Bolsa(codigo, abo, rh, componente, dataColeta, dataValidade, localizacao));
    }

    @Transactional
    public void descartarBolsa(Long bolsaId, String motivo) {
        buscar(bolsaId).descartar(motivo);
    }

    @Transactional(readOnly = true)
    public List<BolsaCandidata> buscarCandidatasFefo(Componente componente, LocalDate hoje) {
        return bolsaRepository.buscarCandidatasFEFO(StatusBolsa.DISPONIVEL, componente, hoje).stream()
                .map(b -> new BolsaCandidata(b.getId(), b.getTipoSanguineo(), b.getValidade()))
                .toList();
    }

    @Transactional
    public void reservarBolsa(Long bolsaId) {
        buscar(bolsaId).reservar();
    }

    @Transactional
    public void liberarBolsa(Long bolsaId) {
        buscar(bolsaId).liberar();
    }

    @Transactional(readOnly = true)
    public ResumoEstoque resumo(LocalDate hoje) {
        return new ResumoEstoque(
                bolsaRepository.count(),
                bolsaRepository.countByStatus(StatusBolsa.DISPONIVEL),
                bolsaRepository.countByStatus(StatusBolsa.RESERVADA),
                bolsaRepository.contarProximasVencimento(hoje, hoje.plusDays(diasAlertaVencimento)),
                bolsaRepository.contarVencidas(hoje));
    }

    private Bolsa buscar(Long bolsaId) {
        return bolsaRepository.findById(bolsaId)
                .orElseThrow(() -> new RegraDeNegocioException("Bolsa não encontrada: " + bolsaId));
    }
}

