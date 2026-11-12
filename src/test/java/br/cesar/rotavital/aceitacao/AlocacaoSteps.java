package br.cesar.rotavital.aceitacao;

import br.cesar.rotavital.estoque.negocio.aplicacao.EstoqueService;
import br.cesar.rotavital.hospitais.negocio.aplicacao.HospitalService;
import br.cesar.rotavital.hospitais.negocio.dominio.Hospital;
import br.cesar.rotavital.requisicao.negocio.aplicacao.RequisicaoService;
import br.cesar.rotavital.requisicao.negocio.dominio.Requisicao;
import br.cesar.rotavital.requisicao.negocio.dominio.Urgencia;
import br.cesar.rotavital.shared.Componente;
import br.cesar.rotavital.shared.TipoAbo;
import br.cesar.rotavital.shared.TipoRh;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AlocacaoSteps {

    @Autowired
    private EstoqueService estoque;

    @Autowired
    private HospitalService hospitais;

    @Autowired
    private RequisicaoService requisicoes;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private TransactionTemplate transacao;

    private Long hospitalSinteticoId;
    private Long requisicaoId;
    private Long bolsaQueVencePrimeiro;

    @Before
    public void estoqueVazio() {
        jdbc.execute("DELETE FROM leitura_telemetria");
        jdbc.execute("DELETE FROM guia_bolsa");
        jdbc.execute("DELETE FROM guia_expedicao");
        jdbc.execute("DELETE FROM alocacao");
        jdbc.execute("DELETE FROM requisicao");
        jdbc.execute("DELETE FROM notificacao");
        jdbc.execute("DELETE FROM bolsa");

        List<Hospital> ativos = hospitais.listarAtivos();
        if (ativos.isEmpty()) {
            hospitalSinteticoId = hospitais.cadastrar(
                    "Hospital Sintético Recife", "10.000.000/0001-99",
                    "Av. Agamenon Magalhães, 100", "Recife", "PE",
                    "(81) 3000-0000", "hemoterapia@sintetico.recife.br", "Dra. Helena Costa"
            ).getId();
        } else {
            hospitalSinteticoId = ativos.get(0).getId();
        }
    }

    @Dado("que existem três bolsas O+ de CH disponíveis com datas de vencimento diferentes")
    public void tresBolsasOPositivoComVencimentosDiferentes() {
        LocalDate coleta = LocalDate.now().minusDays(10);
        registrarBolsa("TEST-FEFO-A", TipoAbo.O, TipoRh.POSITIVO, coleta, LocalDate.now().plusDays(25));
        bolsaQueVencePrimeiro = registrarBolsa("TEST-FEFO-B", TipoAbo.O, TipoRh.POSITIVO, coleta, LocalDate.now().plusDays(5));
        registrarBolsa("TEST-FEFO-C", TipoAbo.O, TipoRh.POSITIVO, coleta, LocalDate.now().plusDays(40));
    }

    @Dado("que existe apenas uma bolsa AB+ de CH disponível no estoque")
    public void apenasUmaBolsaAbPositivo() {
        registrarBolsa("TEST-INCOMPAT-01", TipoAbo.AB, TipoRh.POSITIVO,
                LocalDate.now().minusDays(5), LocalDate.now().plusDays(20));
    }

    @Dado("que existe apenas 1 bolsa O+ de CH disponível no estoque")
    public void apenasUmaBolsaOPositivo() {
        registrarBolsa("TEST-PARCIAL-01", TipoAbo.O, TipoRh.POSITIVO,
                LocalDate.now().minusDays(5), LocalDate.now().plusDays(15));
    }

    @Quando("uma requisição for registrada solicitando {int} bolsa O+ de CH")
    public void requisicaoOPositivo(int quantidade) {
        registrarRequisicao(TipoAbo.O, TipoRh.POSITIVO, quantidade);
    }

    @Quando("uma requisição for registrada solicitando {int} bolsas O+ de CH")
    public void requisicaoVariasOPositivo(int quantidade) {
        registrarRequisicao(TipoAbo.O, TipoRh.POSITIVO, quantidade);
    }

    @Quando("uma requisição for registrada solicitando {int} bolsa O- de CH")
    public void requisicaoONegativo(int quantidade) {
        registrarRequisicao(TipoAbo.O, TipoRh.NEGATIVO, quantidade);
    }

    @Entao("a bolsa com data de validade mais próxima deve ser alocada")
    public void bolsaFefoAlocada() {
        assertThat(bolsasAlocadas()).containsExactly(bolsaQueVencePrimeiro);
    }

    @Entao("nenhuma bolsa deve ser alocada")
    public void nenhumaBolsaAlocada() {
        assertThat(bolsasAlocadas()).isEmpty();
    }

    @Entao("apenas 1 bolsa deve ser alocada")
    public void apenasUmaBolsaAlocada() {
        assertThat(bolsasAlocadas()).hasSize(1);
    }

    @E("o status da requisição deve ser {string}")
    public void statusDaRequisicao(String esperado) {
        String status = transacao.execute(t -> requisicoes.buscar(requisicaoId).getStatus().name());
        assertThat(status).isEqualTo(esperado);
    }

    @E("a bolsa alocada deve ficar reservada no estoque")
    public void bolsaReservadaNoEstoque() {
        String status = jdbc.queryForObject("SELECT status FROM bolsa WHERE id = ?", String.class, bolsaQueVencePrimeiro);
        assertThat(status).isEqualTo("RESERVADA");
    }

    @E("o contexto de Notificação deve registrar que a requisição foi atendida")
    public void notificacaoDeRequisicaoAtendida() {
        Integer total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM notificacao WHERE tipo = 'REQUISICAO_ATENDIDA' AND titulo LIKE ?",
                Integer.class, "%#" + requisicaoId + " %");
        assertThat(total).isEqualTo(1);
    }

    @E("nenhuma notificação de requisição atendida deve ser registrada")
    public void semNotificacao() {
        Integer total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM notificacao WHERE tipo = 'REQUISICAO_ATENDIDA'", Integer.class);
        assertThat(total).isZero();
    }

    private Long registrarBolsa(String codigo, TipoAbo abo, TipoRh rh, LocalDate coleta, LocalDate validade) {
        return estoque.registrarBolsa(codigo, abo, rh, Componente.CH, coleta, validade, "Prateleira teste").getId();
    }

    private void registrarRequisicao(TipoAbo abo, TipoRh rh, int quantidade) {
        Requisicao req = requisicoes.registrar(hospitalSinteticoId, abo, rh, Componente.CH,
                quantidade, Urgencia.ROTINA, "cenário BDD");
        requisicaoId = req.getId();
    }

    private List<Long> bolsasAlocadas() {
        return transacao.execute(t -> requisicoes.buscar(requisicaoId).bolsasAlocadas());
    }
}
