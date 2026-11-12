package br.cesar.rotavital.shared;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public final class Validade implements Comparable<Validade> {

    private final LocalDate data;

    public Validade(LocalDate data) {
        if (data == null) {
            throw new RegraDeNegocioException("Data de validade é obrigatória.");
        }
        this.data = data;
    }

    public static Validade posteriorA(LocalDate dataColeta, LocalDate dataValidade) {
        if (dataColeta == null || dataValidade == null || !dataValidade.isAfter(dataColeta)) {
            throw new RegraDeNegocioException("Data de validade deve ser posterior à data de coleta.");
        }
        return new Validade(dataValidade);
    }

    public boolean estaVencidaEm(LocalDate referencia) {
        return referencia.isAfter(data);
    }

    public boolean venceAntesDe(Validade outra) {
        return data.isBefore(outra.data);
    }

    public long diasRestantes(LocalDate referencia) {
        return ChronoUnit.DAYS.between(referencia, data);
    }

    public LocalDate data() {
        return data;
    }

    @Override
    public int compareTo(Validade outra) {
        return data.compareTo(outra.data);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Validade outra)) return false;
        return data.equals(outra.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    @Override
    public String toString() {
        return data.toString();
    }
}
