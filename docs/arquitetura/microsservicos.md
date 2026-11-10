# Projeção para Microsserviços — Rota Vital

**Projeto:** Rota Vital  
**Disciplina:** Infraestrutura de Software · CESAR School · ADS · 2026.2  
**Trabalho:** U2 · Parte 2 · Seção D — Projeção com Microsserviços

---

## Introdução

O Rota Vital começa como um monolito modular (ADR 0001). Esta seção documenta como o sistema poderia evoluir para microsserviços, quais contextos seriam extraídos primeiro, como eles se comunicariam e como os dados seriam separados.

Esta projeção não é o estado atual — é o planejamento de uma evolução futura, materializada caso o volume de operações justifique a complexidade adicional.

---

## Quais Contextos Virariam Serviços

### Candidatos a extração independente

| Bounded Context | Prioridade | Motivo |
|----------------|-----------|--------|
| **Telemetria** | 🔴 Alta | Volume de dados cresce muito mais rápido que os outros contextos — leituras contínuas de GPS + temperatura por veículo. Pode precisar de escala independente e banco otimizado para séries temporais. |
| **Notificação** | 🟡 Média | Já é genérico, pode usar SaaS (SendGrid, Twilio). Desacoplar facilita troca de provedor. |
| **Identidade** | 🟡 Média | Reutilizável por outros sistemas. Pode ser substituído por Keycloak ou Auth0. |
| **Expedição** | 🟢 Baixa | Pode escalar independentemente se o volume de guias crescer muito. |
| **Hospitais** | 🟢 Baixa | Dados relativamente estáticos; extração traz pouco benefício imediato. |
| **Requisição + Estoque** | 🔵 Mantém junto | Alta coesão e acoplamento transacional. Separar exigiria Sagas complexas. |

---

## Arquitetura Alvo (Projeção)

```
[ Browser / App Móvel ]
        │ HTTPS
        ▼
┌─────────────────┐
│   API Gateway   │  ← Roteia requisições, autentica via Identidade
└────────┬────────┘
         │
   ┌─────┼──────────────────────────────────┐
   │     │                                  │
   ▼     ▼                                  ▼
┌──────┐ ┌───────────────────────┐  ┌─────────────────┐
│Ident.│ │  Requisição + Estoque │  │   Expedição     │
│Serv. │ │       Serviço         │  │   Serviço       │
│      │ │  (Core principal)     │  │                 │
│ DB:  │ │  DB: PostgreSQL       │  │  DB: PostgreSQL │
│Postgres│ └───────────┬─────────┘  └────────┬────────┘
└──────┘              │                      │
                      │ eventos              │ eventos
                      ▼                      ▼
              ┌──────────────────────────────────────┐
              │        Message Broker                 │
              │  (RabbitMQ / Amazon SQS)              │
              └────┬────────────────────┬────────────┘
                   │                    │
                   ▼                    ▼
         ┌──────────────┐    ┌──────────────────┐
         │  Telemetria  │    │   Notificação    │
         │   Serviço    │    │    Serviço       │
         │ DB: TimescaleDB│  │ (SaaS externo)   │
         └──────────────┘    └──────────────────┘
```

---

## Comunicação entre Serviços

### REST (síncrono)
Usado quando o chamador precisa da resposta imediatamente.

| De | Para | Operação |
|----|------|---------|
| API Gateway | Qualquer serviço | Roteamento de requisições |
| Requisição | Hospitais | Verificar se hospital existe |
| Requisição | Estoque | Consultar bolsas disponíveis |

### Eventos / Fila (assíncrono)
Usado quando o chamador não precisa esperar a resposta — "dispara e esquece".

| Evento | Publicado por | Consumido por |
|--------|-------------|--------------|
| `RequisicaoRegistrada` | Requisição+Estoque | Notificação |
| `BolsaReservada` | Estoque | — (log, dashboard) |
| `RequisicaoAtendida` | Requisição | Expedição, Notificação |
| `GuiaEmitida` | Expedição | Telemetria, Notificação |
| `AlertaTemperatura` | Telemetria | Notificação |
| `EntregaConcluida` | Expedição | Requisição+Estoque, Notificação |

---

## Estratégia de Dados por Serviço

Cada microsserviço tem seu próprio banco — sem banco compartilhado.

| Serviço | Banco | Justificativa |
|---------|-------|--------------|
| Requisição + Estoque | PostgreSQL | Transações ACID entre bolsa e alocação |
| Expedição | PostgreSQL | Relacionamentos entre guias, veículos e bolsas |
| Telemetria | TimescaleDB (PostgreSQL extension) | Otimizado para séries temporais; queries por intervalo de tempo |
| Identidade | PostgreSQL | Dados relacionais de usuários e permissões |
| Notificação | Sem banco próprio | Usa SaaS externo (SendGrid, Twilio) |

**Referência cruzada:** Quando um serviço precisa de dado de outro, ele armazena apenas o `id` de referência — nunca o objeto completo. Consultas cross-service são feitas via REST ou eventos.

---

## Exemplo de Saga: Expedição de Hemocomponentes

A Saga é um padrão de consistência eventual para transações distribuídas. No Rota Vital, a expedição de hemocomponentes envolve múltiplos serviços.

### Fluxo normal (happy path)

```
1. Requisição+Estoque    → Publica RequisicaoAtendida
2. Expedição             → Recebe evento, inicia guia
3. Expedição             → Verifica disponibilidade de veículo
4. Expedição             → Confirma expedição, publica GuiaEmitida
5. Telemetria            → Recebe GuiaEmitida, começa a registrar leituras
6. Notificação           → Recebe GuiaEmitida, envia alerta ao hospital
```

### Fluxo de compensação (falha: veículo indisponível)

```
1. Requisição+Estoque    → Publica RequisicaoAtendida
2. Expedição             → Recebe evento, inicia guia
3. Expedição             → Verifica veículo → INDISPONÍVEL
4. Expedição             → Publica ExpedicaoCancelada (compensação)
5. Requisição+Estoque    → Recebe ExpedicaoCancelada
                         → Cancela reservas das bolsas
                         → Muda status das bolsas para Disponível
                         → Muda status da requisição para Atendida (aguardando expedição)
                         → Registra ocorrência
6. Notificação           → Recebe ExpedicaoCancelada
                         → Alerta o operador de expedição
```

**Tipo de Saga:** Coreografada (cada serviço reage a eventos, sem orquestrador central). Adequada para fluxos simples como este.

---

## Comparação: Monolito Modular vs. Microsserviços

| Aspecto | Monolito Modular (atual) | Microsserviços (projeção) |
|---------|------------------------|--------------------------|
| **Unidade de implantação** | Um único artefato (JAR + Docker image) | Múltiplos serviços, cada um com seu próprio container e pipeline |
| **Escalabilidade** | Toda a aplicação escala junta | Cada serviço escala de forma independente (ex.: Telemetria pode ter 10 réplicas enquanto Hospitais tem 1) |
| **Consistência** | Transações ACID garantidas pelo banco único | Consistência eventual via Sagas; mais complexo de implementar e testar |
| **Custo de infraestrutura** | Baixo — 1 instância de app + 1 banco | Alto — múltiplas instâncias, múltiplos bancos, message broker, API Gateway |
| **Complexidade operacional** | Baixa — `docker compose up` | Alta — Kubernetes ou ECS, service discovery, tracing distribuído, circuit breakers |
| **Tempo de desenvolvimento** | Menor — sem overhead de comunicação entre serviços | Maior — cada serviço precisa de CI/CD próprio, contratos de API, gestão de versões |
| **Isolamento de falhas** | Um bug pode afetar todos os módulos | Um serviço com falha não derruba os outros (com circuit breaker) |
| **Recomendado quando** | Time pequeno, escopo definido, volume moderado | Time grande, escala por serviço necessária, domínio estável e bem definido |

**Conclusão:** Para o Rota Vital acadêmico e para uma operação de pequeno a médio porte, o monolito modular é a escolha correta. A projeção para microsserviços é o plano de evolução para quando o volume de telemetria justificar a separação.

---

## Referências

- NEWMAN, S. *Building Microservices*. 2. ed., O'Reilly, 2021.
- RICHARDSON, C. *Microservices Patterns*. Manning, 2018.
- JONAS, E. et al. *A Berkeley View on Serverless Computing*. 2019.
- Slides das aulas 006 e 007 — Infraestrutura de Software · CESAR School · 2026.2
