package br.cesar.rotavital.shared;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class ValidadeTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 12, 1);

    @Test
    void naoAceitaDataNula() {
        assertThatThrownBy(() -> new Validade(null))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void validadeDeveSerPosteriorAColeta() {
        assertThatThrownBy(() -> Validade.posteriorA(HOJE, HOJE.minusDays(1)))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("posterior");
    }

    @Test
    void naoEstaVencidaNoUltimoDia() {
        Validade v = new Validade(HOJE);
        assertThat(v.estaVencidaEm(HOJE)).isFalse();
    }

    @Test
    void estaVencidaNoDiaSeguinte() {
        Validade v = new Validade(HOJE);
        assertThat(v.estaVencidaEm(HOJE.plusDays(1))).isTrue();
    }

    @Test
    void comparaQualVencePrimeiroParaFefo() {
        Validade cedo = new Validade(HOJE.plusDays(3));
        Validade tarde = new Validade(HOJE.plusDays(30));
        assertThat(cedo.venceAntesDe(tarde)).isTrue();
        assertThat(tarde.venceAntesDe(cedo)).isFalse();
    }

    @Test
    void calculaDiasRestantes() {
        assertThat(new Validade(HOJE.plusDays(7)).diasRestantes(HOJE)).isEqualTo(7);
    }

    @Test
    void duasValidadesComMesmaDataSaoIguais() {
        assertThat(new Validade(HOJE)).isEqualTo(new Validade(HOJE));
        assertThat(new Validade(HOJE).hashCode()).isEqualTo(new Validade(HOJE).hashCode());
    }
}
