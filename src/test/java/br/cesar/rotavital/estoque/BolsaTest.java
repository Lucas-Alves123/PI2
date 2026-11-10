package br.cesar.rotavital.estoque.negocio.dominio;

import br.cesar.rotavital.shared.Componente;
import br.cesar.rotavital.shared.TipoAbo;
import br.cesar.rotavital.shared.TipoRh;
import br.cesar.rotavital.shared.TipoSanguineo;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class BolsaTest {

    private Bolsa bolsaValida() {
        return new Bolsa("RV-TEST-001", TipoAbo.O, TipoRh.POSITIVO, Componente.CH,
                LocalDate.now().minusDays(10),
                LocalDate.now().plusDays(30),
                "Prateleira A1");
    }

    @Test
    void novaBolsaDeveEstarDisponivel() {
        Bolsa bolsa = bolsaValida();
        assertThat(bolsa.getStatus()).isEqualTo(StatusBolsa.DISPONIVEL);
    }

    @Test
    void deveReservarBolsaDisponivel() {
        Bolsa bolsa = bolsaValida();
        bolsa.reservar();
        assertThat(bolsa.getStatus()).isEqualTo(StatusBolsa.RESERVADA);
    }

    @Test
    void naoDeveReservarBolsaJaReservada() {
        Bolsa bolsa = bolsaValida();
        bolsa.reservar();
        assertThatThrownBy(bolsa::reservar)
                .hasMessageContaining("disponíveis");
    }

    @Test
    void deveDetectarBolsaVencida() {
        Bolsa bolsa = new Bolsa("RV-VENC-001", TipoAbo.A, TipoRh.NEGATIVO, Componente.PFC,
                LocalDate.now().minusDays(50),
                LocalDate.now().minusDays(1), // vencida ontem
                "Freezer B1");
        assertThat(bolsa.estaVencida(LocalDate.now())).isTrue();
    }

    @Test
    void deveLiberarBolsaReservada() {
        Bolsa bolsa = bolsaValida();
        bolsa.reservar();
        bolsa.liberar();
        assertThat(bolsa.getStatus()).isEqualTo(StatusBolsa.DISPONIVEL);
    }

    @Test
    void codigoNaoPodeSerVazio() {
        assertThatThrownBy(() ->
            new Bolsa("", TipoAbo.O, TipoRh.POSITIVO, Componente.CH,
                    LocalDate.now().minusDays(5), LocalDate.now().plusDays(10), null)
        ).hasMessageContaining("obrigatório");
    }

    @Test
    void validadeDeveSerPosteriorAColeta() {
        assertThatThrownBy(() ->
            new Bolsa("RV-ERRO-001", TipoAbo.B, TipoRh.POSITIVO, Componente.CP,
                    LocalDate.now(),
                    LocalDate.now().minusDays(1),
                    null)
        ).hasMessageContaining("validade");
    }
}

