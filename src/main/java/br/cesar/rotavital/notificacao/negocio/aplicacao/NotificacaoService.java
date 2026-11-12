package br.cesar.rotavital.notificacao.negocio.aplicacao;

import br.cesar.rotavital.notificacao.dados.NotificacaoRepositoryJpa;
import br.cesar.rotavital.notificacao.negocio.dominio.Notificacao;
import br.cesar.rotavital.notificacao.negocio.dominio.TipoNotificacao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificacaoService {

    private final NotificacaoRepositoryJpa notificacaoRepository;

    public NotificacaoService(NotificacaoRepositoryJpa notificacaoRepository) {
        this.notificacaoRepository = notificacaoRepository;
    }

    @Transactional
    public Notificacao notificar(TipoNotificacao tipo, String titulo, String mensagem) {
        return notificacaoRepository.save(new Notificacao(tipo, titulo, mensagem));
    }

    @Transactional(readOnly = true)
    public List<Notificacao> recentes() {
        return notificacaoRepository.findTop10ByOrderByCriadaEmDesc();
    }
}

