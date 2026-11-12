package br.cesar.rotavital.expedicao.negocio.dominio.eventos;

public record GuiaEntregue(Long guiaId, String numeroGuia, Long requisicaoId, Long hospitalId) {
}

