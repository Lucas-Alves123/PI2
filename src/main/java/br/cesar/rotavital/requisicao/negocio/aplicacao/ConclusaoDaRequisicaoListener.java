package br.cesar.rotavital.requisicao.negocio.aplicacao;

import br.cesar.rotavital.expedicao.negocio.dominio.eventos.GuiaEntregue;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ConclusaoDaRequisicaoListener {

    private final RequisicaoService requisicoes;

    public ConclusaoDaRequisicaoListener(RequisicaoService requisicoes) {
        this.requisicoes = requisicoes;
    }

    @EventListener
    public void quandoGuiaEntregue(GuiaEntregue evento) {
        requisicoes.concluir(evento.requisicaoId());
    }
}

