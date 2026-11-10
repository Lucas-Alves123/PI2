package br.cesar.rotavital.shared;

public enum Componente {
    CH("Concentrado de Hemácias"),
    PFC("Plasma Fresco Congelado"),
    CP("Concentrado de Plaquetas"),
    CRIO("Crioprecipitado");

    private final String descricao;

    Componente(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}

