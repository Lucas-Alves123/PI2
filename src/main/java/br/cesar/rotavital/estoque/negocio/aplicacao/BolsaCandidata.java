package br.cesar.rotavital.estoque.negocio.aplicacao;

import br.cesar.rotavital.shared.TipoSanguineo;
import br.cesar.rotavital.shared.Validade;

public record BolsaCandidata(Long id, TipoSanguineo tipoSanguineo, Validade validade) {
}

