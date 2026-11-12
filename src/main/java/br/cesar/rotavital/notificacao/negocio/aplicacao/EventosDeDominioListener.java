package br.cesar.rotavital.notificacao.negocio.aplicacao;

import br.cesar.rotavital.expedicao.negocio.dominio.eventos.GuiaEntregue;
import br.cesar.rotavital.notificacao.negocio.dominio.TipoNotificacao;
import br.cesar.rotavital.requisicao.negocio.dominio.eventos.RequisicaoAtendida;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class EventosDeDominioListener {

    private final NotificacaoService notificacoes;

    public EventosDeDominioListener(NotificacaoService notificacoes) {
        this.notificacoes = notificacoes;
    }

    @EventListener
    public void quandoRequisicaoAtendida(RequisicaoAtendida evento) {
        notificacoes.notificar(TipoNotificacao.REQUISICAO_ATENDIDA,
                "Requisição #" + evento.requisicaoId() + " atendida",
                evento.bolsas().size() + " bolsa(s) reservada(s). Pronta para emissão da guia de expedição.");
    }

    @EventListener
    public void quandoGuiaEntregue(GuiaEntregue evento) {
        notificacoes.notificar(TipoNotificacao.ENTREGA_CONCLUIDA,
                "Guia " + evento.numeroGuia() + " entregue",
                "Entrega confirmada no hospital #" + evento.hospitalId()
                        + ". Requisição #" + evento.requisicaoId() + " concluída.");
    }
}

