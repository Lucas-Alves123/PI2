package br.cesar.rotavital.requisicao.negocio.aplicacao;

import java.util.List;

public record RequisicaoParaExpedicao(Long requisicaoId, Long hospitalId, List<Long> bolsas) {
}

