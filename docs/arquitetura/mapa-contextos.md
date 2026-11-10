# Mapa de Contextos — Rota Vital

**Projeto:** Rota Vital  
**Disciplina:** Infraestrutura de Software · CESAR School · ADS · 2026.2  
**Trabalho:** U2 · Parte 2 · Seção A — Mapa de Contextos

---

## Introdução

O mapa de contextos descreve como os Bounded Contexts do Rota Vital se relacionam entre si. Cada relação tem um tipo que define quem depende de quem, como os dados trafegam e quem tem autonomia para mudar o modelo.

### Tipos de relação utilizados

| Tipo | Descrição |
|------|-----------|
| **Cliente-Fornecedor (C/F)** | O contexto upstream (fornecedor) provê dados; o downstream (cliente) os consome. O fornecedor tem autonomia sobre seu modelo. |
| **Conformista (C)** | O contexto downstream adota o modelo do upstream sem adaptação. |
| **Camada Anticorrupção (ACL)** | O contexto downstream traduz o modelo do upstream para seu próprio modelo, protegendo sua linguagem ubíqua. |
| **Publicador-Assinante (Pub/Sub)** | O contexto publica eventos de domínio; outros contextos assinam e reagem a eles. |

---

## Diagrama do Mapa de Contextos

```
                    ┌─────────────────────────────────────────────────┐
                    │          NÚCLEO DO SISTEMA (Core)                │
                    │                                                   │
  ┌─────────────┐   │  ┌──────────────┐      ┌──────────────────────┐  │
  │  HOSPITAIS  │──►│  │  REQUISIÇÃO  │◄────►│      ALOCAÇÃO        │  │
  │  (Suporte)  │   │  │   (Core)     │      │      (Core)          │  │
  └─────────────┘   │  └──────┬───────┘      └──────────┬───────────┘  │
       ACL          │         │                          │              │
                    │         │ eventos                  │ consulta     │
                    │         ▼                          ▼              │
                    │  ┌──────────────┐      ┌──────────────────────┐  │
                    │  │  EXPEDIÇÃO   │◄────►│      ESTOQUE         │  │
                    │  │   (Core)     │      │      (Core)          │  │
                    │  └──────┬───────┘      └──────────────────────┘  │
                    │         │                                         │
                    └─────────┼───────────────────────────────────────-┘
                              │
              ┌───────────────┼───────────────┐
              │               │               │
              ▼               ▼               ▼
       ┌────────────┐  ┌─────────────┐  ┌──────────────┐
       │ TELEMETRIA │  │ NOTIFICAÇÃO │  │  IDENTIDADE  │
       │ (Suporte)  │  │ (Genérico)  │  │  (Genérico)  │
       └────────────┘  └─────────────┘  └──────────────┘
```

---

## Relações Detalhadas

### 1. Hospitais → Requisição
**Tipo:** Cliente-Fornecedor com Camada Anticorrupção (ACL)  
**Descrição:** O contexto de Requisição precisa saber qual hospital fez a solicitação e qual unidade solicitante. Ele consulta o contexto de Hospitais para obter essas informações, mas traduz o modelo para seu próprio vocabulário via ACL.  
**O que trafega:** `hospitalId`, `unidadeId` — apenas identificadores, não o objeto completo.  
**Motivo da ACL:** Requisição não deve depender do modelo interno de Hospital. Se Hospital mudar, a ACL absorve a mudança.

---

### 2. Requisição → Alocação
**Tipo:** Cliente-Fornecedor (Requisição é downstream, Alocação é upstream)  
**Descrição:** Quando uma requisição é registrada, o contexto de Alocação é acionado para verificar compatibilidade, aplicar FEFO e reservar bolsas. O resultado retorna como lista de alocações vinculadas à requisição.  
**O que trafega:** `tipoSanguineo`, `componente`, `quantidade`, `urgencia` → retorna `Alocacao[]`  
**Nota:** Na implementação monolítica atual, esta comunicação é feita por chamada direta de serviço. Na projeção de microsserviços, seria via evento (`RequisicaoRegistrada`).

---

### 3. Alocação → Estoque
**Tipo:** Cliente-Fornecedor com Camada Anticorrupção  
**Descrição:** O contexto de Alocação consulta o Estoque para identificar bolsas disponíveis, verificar compatibilidade e marcar bolsas como reservadas. O Estoque tem autonomia sobre seu modelo interno de bolsa.  
**O que trafega:** consulta por tipo sanguíneo + componente + disponibilidade; comando de reserva (`bolsaId`)  
**Invariante protegida:** O Estoque é responsável por garantir que uma bolsa só possa ser reservada por uma requisição ativa.

---

### 4. Requisição → Expedição (evento)
**Tipo:** Publicador-Assinante  
**Descrição:** Quando uma requisição atinge o status "Atendida" (todas as alocações concluídas), ela publica o evento `RequisicaoAtendida`. O contexto de Expedição assina esse evento e prepara a guia de expedição.  
**Evento publicado:** `RequisicaoAtendida { requisicaoId, hospitalId, bolsasAlocadas[] }`  
**Nota:** Na implementação monolítica, este evento é tratado internamente (listener Spring). Na projeção de microsserviços, seria via fila (RabbitMQ / SQS).

---

### 5. Expedição → Telemetria
**Tipo:** Cliente-Fornecedor  
**Descrição:** O contexto de Telemetria recebe o identificador da guia de expedição e registra leituras de temperatura e localização vinculadas a ela. A Expedição consulta a Telemetria para obter o histórico de leituras.  
**O que trafega:** `guiaId`, leituras (`temperatura`, `latitude`, `longitude`, `timestamp`)

---

### 6. Telemetria → Notificação (evento)
**Tipo:** Publicador-Assinante  
**Descrição:** Quando uma leitura de temperatura fica fora da faixa configurada, a Telemetria publica o evento `AlertaTemperatura`. O contexto de Notificação assina e gera o alerta.  
**Evento publicado:** `AlertaTemperatura { guiaId, temperatura, timestamp, limiteMinimo, limiteMaximo }`

---

### 7. Requisição → Notificação (evento)
**Tipo:** Publicador-Assinante  
**Descrição:** A Requisição publica eventos (`RequisicaoRegistrada`, `RequisicaoAtendida`) que a Notificação assina para gerar avisos para os usuários.

---

### 8. Estoque → Notificação (evento)
**Tipo:** Publicador-Assinante  
**Descrição:** O Estoque publica `BolsaProximaDoVencimento` e `BolsaVencida`. A Notificação assina e alerta os operadores.

---

### 9. Todos os contextos → Identidade
**Tipo:** Conformista  
**Descrição:** Todos os contextos delegam autenticação e autorização ao contexto de Identidade. Os contextos consomem os dados do usuário autenticado (id, perfil, permissões) sem tradução — conformando ao modelo da Identidade.

---

## Eventos de Domínio (opcional — entre contextos)

| Evento | Publicado por | Consumido por | Descrição |
|--------|-------------|--------------|-----------|
| `RequisicaoRegistrada` | Requisição | Notificação | Nova solicitação recebida |
| `BolsaReservada` | Alocação | Estoque, Dashboard | Bolsa alocada para uma requisição |
| `RequisicaoAtendida` | Requisição | Expedição, Notificação | Todas as alocações concluídas |
| `GuiaEmitida` | Expedição | Telemetria, Notificação | Expedição iniciada |
| `EntregaConcluida` | Expedição | Requisição, Notificação | Hemocomponentes entregues no hospital |
| `AlertaTemperatura` | Telemetria | Notificação | Temperatura fora da faixa configurada |
| `BolsaProximaDoVencimento` | Estoque | Notificação | Bolsa com validade próxima |
| `BolsaVencida` | Estoque | Notificação, Alocação | Bolsa vencida — deve ser bloqueada |

---

## Referências

- EVANS, E. *Domain-Driven Design*. Addison-Wesley, 2003.
- VERNON, V. *Implementing Domain-Driven Design*. Addison-Wesley, 2013.
- Slides das aulas 006 e 007 — Infraestrutura de Software · CESAR School · 2026.2
