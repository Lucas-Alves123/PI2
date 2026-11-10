package br.cesar.rotavital.aceitacao;

import br.cesar.rotavital.estoque.dados.BolsaRepositoryJpa;
import br.cesar.rotavital.estoque.negocio.dominio.Bolsa;
import br.cesar.rotavital.requisicao.dados.RequisicaoRepositoryJpa;
import br.cesar.rotavital.requisicao.negocio.aplicacao.AlocacaoFefoService;
import br.cesar.rotavital.requisicao.negocio.dominio.Requisicao;
import br.cesar.rotavital.requisicao.negocio.dominio.StatusRequisicao;
import br.cesar.rotavital.requisicao.negocio.dominio.Urgencia;
import br.cesar.rotavital.shared.Componente;
import br.cesar.rotavital.shared.TipoAbo;
import br.cesar.rotavital.shared.TipoRh;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class AlocacaoSteps {

    @Autowired
    private BolsaRepositoryJpa bolsaRepository;

    @Autowired
    private RequisicaoRepositoryJpa requisicaoRepository;

    @Autowired
    private AlocacaoFefoService alocacaoService;

    private Requisicao ultimaRequisicao;
    private Long bolsaMaisProximaId;

    @Dado("que existem três bolsas O+ de CH disponíveis com datas de vencimento diferentes")
    public void tresBolsasOPlusComVencimentosDiferentes() {
        // A que vence mais cedo deve ser alocada (FEFO)
        Bolsa b1 = new Bolsa("TEST-FEFO-A", TipoAbo.O, TipoRh.POSITIVO, Componente.CH,
                LocalDate.now().minusDays(10), LocalDate.now().plusDays(25), "A1");
        Bolsa b2 = new Bolsa("TEST-FEFO-B", TipoAbo.O, TipoRh.POSITIVO, Componente.CH,
                LocalDate.now().minusDays(10), LocalDate.now().plusDays(5), "A1"); // vence mais cedo
        Bolsa b3 = new Bolsa("TEST-FEFO-C", TipoAbo.O, TipoRh.POSITIVO, Componente.CH,
                LocalDate.now().minusDays(10), LocalDate.now().plusDays(40), "A1");
        bolsaMaisProximaId = bolsaRepository.save(b2).getId();
        bolsaRepository.save(b1);
        bolsaRepository.save(b3);
    }

    @Dado("que existe apenas uma bolsa AB+ de CH disponível no estoque")
    public void apenasUmaBolsaAbPlusCH() {
        Bolsa b = new Bolsa("TEST-INCOMPAT-01", TipoAbo.AB, TipoRh.POSITIVO, Componente.CH,
                LocalDate.now().minusDays(5), LocalDate.now().plusDays(20), "A4");
        bolsaRepository.save(b);
    }

    @Dado("que existe apenas 1 bolsa O+ de CH disponível no estoque")
    public void apenasUmaBolsaOPlusCH() {
        Bolsa b = new Bolsa("TEST-PARCIAL-01", TipoAbo.O, TipoRh.POSITIVO, Componente.CH,
                LocalDate.now().minusDays(5), LocalDate.now().plusDays(15), "A1");
        bolsaRepository.save(b);
    }

    @Quando("uma requisição for registrada solicitando {int} bolsa O+ de CH")
    public void requisicaoOPlusCH(int quantidade) {
        Requisicao req = new Requisicao(1L, null, TipoAbo.O, TipoRh.POSITIVO, Componente.CH,
                quantidade, Urgencia.ROTINA, "cenário BDD");
        ultimaRequisicao = requisicaoRepository.save(req);
        alocacaoService.alocarParaRequisicao(ultimaRequisicao.getId());
        ultimaRequisicao = requisicaoRepository.findById(ultimaRequisicao.getId()).orElseThrow();
    }

    @Quando("uma requisição for registrada solicitando {int} bolsa O- de CH")
    public void requisicaoOMenosCH(int quantidade) {
        Requisicao req = new Requisicao(1L, null, TipoAbo.O, TipoRh.NEGATIVO, Componente.CH,
                quantidade, Urgencia.ROTINA, "cenário BDD incompatível");
        ultimaRequisicao = requisicaoRepository.save(req);
        alocacaoService.alocarParaRequisicao(ultimaRequisicao.getId());
        ultimaRequisicao = requisicaoRepository.findById(ultimaRequisicao.getId()).orElseThrow();
    }

    @Quando("uma requisição for registrada solicitando {int} bolsas O+ de CH")
    public void requisicaoVariasBolsas(int quantidade) {
        requisicaoOPlusCH(quantidade);
    }

    @Entao("a bolsa com data de validade mais próxima deve ser alocada")
    public void bolsaFefoDeveSerAlocada() {
        assertThat(ultimaRequisicao.getAlocacoes()).isNotEmpty();
        Long alocadaId = ultimaRequisicao.getAlocacoes().get(0).getBolsaId();
        assertThat(alocadaId).isEqualTo(bolsaMaisProximaId);
    }

    @Entao("nenhuma bolsa deve ser alocada")
    public void nenhumaBolsaAlocada() {
        assertThat(ultimaRequisicao.getAlocacoes()).isEmpty();
    }

    @Entao("apenas 1 bolsa deve ser alocada")
    public void apenasUmaBolsaAlocada() {
        assertThat(ultimaRequisicao.getAlocacoes()).hasSize(1);
    }

    @E("o status da requisição deve ser {string}")
    public void statusDaRequisicaoDeveSer(String statusEsperado) {
        assertThat(ultimaRequisicao.getStatus().name()).isEqualTo(statusEsperado);
    }
}

