package br.cesar.rotavital.requisicao.negocio.dominio.eventos;

import java.util.List;

public record RequisicaoAtendida(Long requisicaoId, Long hospitalId, List<Long> bolsas) {
}

