package br.cesar.rotavital.estoque.negocio.aplicacao;

public record ResumoEstoque(long total, long disponiveis, long reservadas, long proximasDoVencimento, long vencidas) {
}

