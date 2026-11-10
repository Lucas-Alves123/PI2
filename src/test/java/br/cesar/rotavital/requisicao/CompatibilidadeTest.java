package br.cesar.rotavital.requisicao;

import br.cesar.rotavital.shared.Componente;
import br.cesar.rotavital.shared.TipoAbo;
import br.cesar.rotavital.shared.TipoRh;
import br.cesar.rotavital.shared.TipoSanguineo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

class CompatibilidadeTest {

    @Test
    void oNegativoDeveDoarParaTodos() {
        TipoSanguineo doador = new TipoSanguineo(TipoAbo.O, TipoRh.NEGATIVO);
        TipoSanguineo[] receptores = {
            new TipoSanguineo(TipoAbo.O,  TipoRh.NEGATIVO),
            new TipoSanguineo(TipoAbo.O,  TipoRh.POSITIVO),
            new TipoSanguineo(TipoAbo.A,  TipoRh.NEGATIVO),
            new TipoSanguineo(TipoAbo.A,  TipoRh.POSITIVO),
            new TipoSanguineo(TipoAbo.B,  TipoRh.NEGATIVO),
            new TipoSanguineo(TipoAbo.B,  TipoRh.POSITIVO),
            new TipoSanguineo(TipoAbo.AB, TipoRh.NEGATIVO),
            new TipoSanguineo(TipoAbo.AB, TipoRh.POSITIVO),
        };
        for (TipoSanguineo r : receptores) {
            assertThat(doador.podeDoarPara(r, Componente.CH))
                .as("O- deve poder doar para " + r).isTrue();
        }
    }

    @Test
    void abPositivoSoDeveReceberAbPositivo() {
        TipoSanguineo receptor = new TipoSanguineo(TipoAbo.AB, TipoRh.POSITIVO);
        // qualquer tipo pode doar para AB+
        assertThat(new TipoSanguineo(TipoAbo.A, TipoRh.NEGATIVO).podeDoarPara(receptor, Componente.CH)).isTrue();
        assertThat(new TipoSanguineo(TipoAbo.B, TipoRh.POSITIVO).podeDoarPara(receptor, Componente.CH)).isTrue();
        assertThat(new TipoSanguineo(TipoAbo.O, TipoRh.NEGATIVO).podeDoarPara(receptor, Componente.CH)).isTrue();
    }

    @Test
    void positivoNaoDeveDoarParaNegativo() {
        TipoSanguineo doador = new TipoSanguineo(TipoAbo.O, TipoRh.POSITIVO);
        TipoSanguineo receptor = new TipoSanguineo(TipoAbo.O, TipoRh.NEGATIVO);
        assertThat(doador.podeDoarPara(receptor, Componente.CH)).isFalse();
    }

    @ParameterizedTest(name = "Doador {0}{1} → Receptor {2}{3}: compatível = {4}")
    @CsvSource({
        "A, POSITIVO, A, POSITIVO, true",
        "A, POSITIVO, A, NEGATIVO, false",
        "A, POSITIVO, AB, POSITIVO, true",
        "B, NEGATIVO, A, NEGATIVO, false",
        "O, NEGATIVO, B, POSITIVO, true",
        "AB, POSITIVO, A, POSITIVO, false",
    })
    void tabelaCompatibilidadeCH(TipoAbo dAbo, TipoRh dRh, TipoAbo rAbo, TipoRh rRh, boolean esperado) {
        TipoSanguineo doador = new TipoSanguineo(dAbo, dRh);
        TipoSanguineo receptor = new TipoSanguineo(rAbo, rRh);
        assertThat(doador.podeDoarPara(receptor, Componente.CH)).isEqualTo(esperado);
    }

    @Test
    void tiposSanguineosIguaisSaoIguais() {
        TipoSanguineo t1 = new TipoSanguineo(TipoAbo.A, TipoRh.POSITIVO);
        TipoSanguineo t2 = new TipoSanguineo(TipoAbo.A, TipoRh.POSITIVO);
        assertThat(t1).isEqualTo(t2);
        assertThat(t1.toString()).isEqualTo("A+");
    }

    @Test
    void aboNuloDeveLancarExcecao() {
        assertThatThrownBy(() -> new TipoSanguineo(null, TipoRh.POSITIVO))
            .hasMessageContaining("obrigatórios");
    }
}

