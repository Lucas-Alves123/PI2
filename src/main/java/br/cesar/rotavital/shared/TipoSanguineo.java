package br.cesar.rotavital.shared;

import java.util.Objects;

public final class TipoSanguineo {

    private final TipoAbo abo;
    private final TipoRh rh;

    public TipoSanguineo(TipoAbo abo, TipoRh rh) {
        if (abo == null || rh == null) {
            throw new RegraDeNegocioException("ABO e Rh são obrigatórios para compor o tipo sanguíneo.");
        }
        this.abo = abo;
        this.rh = rh;
    }

    public TipoAbo getAbo() {
        return abo;
    }

    public TipoRh getRh() {
        return rh;
    }

    public boolean podeDoarPara(TipoSanguineo receptor, Componente componente) {
        if (componente == Componente.CH) {
            return compatibilidadeHemacias(receptor);
        }
        return this.abo == receptor.abo;
    }

    private boolean compatibilidadeHemacias(TipoSanguineo receptor) {
        if (this.rh == TipoRh.POSITIVO && receptor.rh == TipoRh.NEGATIVO) {
            return false;
        }
        if (this.abo == TipoAbo.O) {
            return true;
        }
        if (receptor.abo == TipoAbo.AB) {
            return true;
        }
        return this.abo == receptor.abo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TipoSanguineo that = (TipoSanguineo) o;
        return abo == that.abo && rh == that.rh;
    }

    @Override
    public int hashCode() {
        return Objects.hash(abo, rh);
    }

    @Override
    public String toString() {
        return abo.name() + (rh == TipoRh.POSITIVO ? "+" : "-");
    }
}
