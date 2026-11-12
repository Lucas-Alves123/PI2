package br.cesar.rotavital.shared;

import java.time.LocalDate;

public final class Validade {

    private final LocalDate data;

    public Validade(LocalDate data) {
        this.data = data;
    }

    public static Validade posteriorA(LocalDate dataColeta, LocalDate dataValidade) {
        return new Validade(dataValidade);
    }

    public boolean estaVencidaEm(LocalDate referencia) {
        return false;
    }

    public boolean venceAntesDe(Validade outra) {
        return false;
    }

    public long diasRestantes(LocalDate referencia) {
        return 0;
    }

    public LocalDate data() {
        return data;
    }
}
