package br.cesar.rotavital.requisicao.negocio.aplicacao;

import br.cesar.rotavital.estoque.dados.BolsaRepositoryJpa;
import br.cesar.rotavital.estoque.negocio.dominio.Bolsa;
import br.cesar.rotavital.estoque.negocio.dominio.StatusBolsa;
import br.cesar.rotavital.requisicao.dados.RequisicaoRepositoryJpa;
import br.cesar.rotavital.requisicao.negocio.dominio.Requisicao;
import br.cesar.rotavital.requisicao.negocio.dominio.StatusRequisicao;
import br.cesar.rotavital.shared.RegraDeNegocioException;
import br.cesar.rotavital.shared.TipoSanguineo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class AlocacaoFefoService {

    private final BolsaRepositoryJpa bolsaRepository;
    private final RequisicaoRepositoryJpa requisicaoRepository;

    public AlocacaoFefoService(BolsaRepositoryJpa bolsaRepository, RequisicaoRepositoryJpa requisicaoRepository) {
        this.bolsaRepository = bolsaRepository;
        this.requisicaoRepository = requisicaoRepository;
    }

    /**
     * Executa a alocação FEFO inteligente:
     * 1. Busca bolsas candidatas do componente ordenadas por validade (FEFO)
     * 2. Filtra por compatibilidade ABO/Rh
     * 3. Reserva as bolsas até atingir a quantidade necessária
     * 4. Registra na requisição
     */
    @Transactional
    public List<Bolsa> alocarParaRequisicao(Long requisicaoId) {
        Requisicao req = requisicaoRepository.findById(requisicaoId)
                .orElseThrow(() -> new RegraDeNegocioException("Requisição não encontrada: " + requisicaoId));

        if (req.getStatus() == StatusRequisicao.ATENDIDA ||
            req.getStatus() == StatusRequisicao.CONCLUIDA ||
            req.getStatus() == StatusRequisicao.CANCELADA) {
            throw new RegraDeNegocioException("Requisição em status que não permite novas alocações: " + req.getStatus());
        }

        int necessarias = req.quantidadePendente();
        if (necessarias <= 0) {
            return List.of();
        }

        LocalDate hoje = LocalDate.now();
        List<Bolsa> candidatas = bolsaRepository.buscarCandidatasFEFO(StatusBolsa.DISPONIVEL, req.getComponente(), hoje);

        TipoSanguineo tipoReceptor = req.getTipoSanguineoSolicitado();
        List<Bolsa> selecionadas = new ArrayList<>();

        for (Bolsa bolsa : candidatas) {
            if (selecionadas.size() >= necessarias) {
                break;
            }

            TipoSanguineo tipoDoador = bolsa.getTipoSanguineo();
            if (tipoDoador.podeDoarPara(tipoReceptor, req.getComponente())) {
                bolsa.reservar();
                bolsaRepository.save(bolsa);

                req.adicionarAlocacao(bolsa.getId(), bolsa.getDataValidade());
                selecionadas.add(bolsa);
            }
        }

        requisicaoRepository.save(req);
        return selecionadas;
    }
}

