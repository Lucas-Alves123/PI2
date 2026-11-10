package br.cesar.rotavital.requisicao;

import br.cesar.rotavital.requisicao.negocio.dominio.Requisicao;
import br.cesar.rotavital.requisicao.negocio.dominio.StatusRequisicao;
import br.cesar.rotavital.requisicao.negocio.dominio.Urgencia;
import br.cesar.rotavital.shared.Componente;
import br.cesar.rotavital.shared.TipoAbo;
import br.cesar.rotavital.shared.TipoRh;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class RequisicaoTest {

    private Requisicao requisicaoPadrao() {
        return new Requisicao(1L, null, TipoAbo.O, TipoRh.POSITIVO,
                Componente.CH, 2, Urgencia.ROTINA, null);
    }

    @Test
    void novaRequisicaoDeveEstarPendente() {
        Requisicao req = requisicaoPadrao();
        assertThat(req.getStatus()).isEqualTo(StatusRequisicao.PENDENTE);
    }

    @Test
    void deveEstarParcialmenteAtendidaAposUmaAlocacaoDeDuas() {
        Requisicao req = requisicaoPadrao();
        req.adicionarAlocacao(100L, LocalDate.now().plusDays(10));
        assertThat(req.getStatus()).isEqualTo(StatusRequisicao.PARCIALMENTE_ATENDIDA);
    }

    @Test
    void deveEstarAtendidaAposTodasAlocacoes() {
        Requisicao req = requisicaoPadrao();
        req.adicionarAlocacao(100L, LocalDate.now().plusDays(10));
        req.adicionarAlocacao(200L, LocalDate.now().plusDays(20));
        assertThat(req.getStatus()).isEqualTo(StatusRequisicao.ATENDIDA);
    }

    @Test
    void naoDeveAdicionarMesmaBolsaDuasVezes() {
        Requisicao req = requisicaoPadrao();
        req.adicionarAlocacao(100L, LocalDate.now().plusDays(10));
        assertThatThrownBy(() -> req.adicionarAlocacao(100L, LocalDate.now().plusDays(10)))
                .hasMessageContaining("vinculada");
    }

    @Test
    void deveCancelarRequisicaoPendente() {
        Requisicao req = requisicaoPadrao();
        req.cancelar("Teste de cancelamento");
        assertThat(req.getStatus()).isEqualTo(StatusRequisicao.CANCELADA);
    }

    @Test
    void quantidadeZeroNaoDeveSerPermitida() {
        assertThatThrownBy(() ->
            new Requisicao(1L, null, TipoAbo.A, TipoRh.NEGATIVO, Componente.CH, 0, Urgencia.URGENCIA, null)
        ).hasMessageContaining("maior que zero");
    }

    @Test
    void hospitalIdObrigatorio() {
        assertThatThrownBy(() ->
            new Requisicao(null, null, TipoAbo.O, TipoRh.NEGATIVO, Componente.PFC, 1, Urgencia.ROTINA, null)
        ).hasMessageContaining("Hospital é obrigatório");
    }
}

