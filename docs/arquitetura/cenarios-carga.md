# Cenários de Carga e Orçamento — Rota Vital

**Projeto:** Rota Vital  
**Disciplina:** Infraestrutura de Software · CESAR School · ADS · 2026.2  
**Trabalho:** U2 · Parte 2 · Seção E — Três Cenários com Orçamento

---

## Premissas Gerais

Antes dos cálculos, declaramos as premissas assumidas:

| Premissa | Valor | Observação |
|----------|-------|-----------|
| Horas de pico por dia | 8h | Operação diurna (7h às 19h) |
| Distribuição de carga | 60% nas horas de pico | Padrão operacional hospitalar |
| Tempo médio de processamento (W) | 200 ms | Inclui validação, consulta ao banco e resposta |
| Disponibilidade desejada (todos) | 99,5% | ≈ 44h de downtime/ano |
| Leituras de telemetria por veículo | 1 por minuto | Premissa sintética |

> ⚠️ **Nota sobre o W (tempo médio de processamento):** O valor de 200 ms é uma premissa acadêmica. Num sistema real, seria medido com testes de carga (k6, JMeter, Gatling). Para o Rota Vital, estimamos que a consulta de compatibilidade + FEFO + gravação no banco leva entre 100 ms e 300 ms.

---

## Lei de Little

**L = λ × W**

Onde:
- **L** = número médio de requisições em andamento simultaneamente (concorrência)
- **λ** (lambda) = taxa de chegada de requisições por segundo (req/s)
- **W** = tempo médio de processamento de cada requisição (segundos)

**Como usar:**
Se o pico é de 10 req/s e cada requisição demora 0,2 s:  
L = 10 × 0,2 = **2 requisições em andamento ao mesmo tempo**

Isso significa que precisamos de capacidade para processar pelo menos 2 requisições concorrentes. Se cada instância da aplicação suporta ~50 threads, **1 instância é suficiente** para este caso.

---

## Cenário 1: Carga Baixa

**Perfil:** Hemocentro regional atendendo 3 a 5 hospitais de pequeno porte.

| Parâmetro | Valor |
|-----------|-------|
| Usuários simultâneos | 10 |
| Requisições hospitalares por dia | 50 |
| Leituras de telemetria por dia | 2.880 (2 veículos × 1 leitura/min × 24h) |
| Pico de req/s (aplicação) | 2 req/s |
| Pico de req/s (telemetria) | 0,1 req/s |
| Disponibilidade desejada | 99,5% |
| W (tempo médio) | 0,2 s |

**Lei de Little:**

```
L (aplicação)  = 2 req/s × 0,2 s = 0,4 (< 1 requisição em andamento)
L (telemetria) = 0,1 req/s × 0,2 s = 0,02
```

**Instâncias necessárias:** 1 instância da aplicação (monolito) com 1 réplica.  
**Conexões com banco:** mínimo de 5 conexões no pool (HikariCP padrão).

### Diagrama — Cenário 1

```
[ Usuários (10) ]
       │ HTTP
       ▼
[ Load Balancer (opcional) ]
       │
       ▼
┌─────────────────────────────┐
│   App: 1 instância          │
│   Spring Boot (512 MB RAM)  │
│   1 vCPU                    │
└──────────────┬──────────────┘
               │ JDBC (pool: 5 conexões)
               ▼
┌─────────────────────────────┐
│   PostgreSQL 16              │
│   db.t3.micro ou similar    │
│   1 vCPU / 1 GB RAM         │
└─────────────────────────────┘
```

---

## Cenário 2: Carga Moderada

**Perfil:** Hemocentro estadual atendendo 20 a 40 hospitais de médio porte, com telemetria de 10 veículos.

| Parâmetro | Valor |
|-----------|-------|
| Usuários simultâneos | 100 |
| Requisições hospitalares por dia | 500 |
| Leituras de telemetria por dia | 14.400 (10 veículos × 1 leitura/min × 24h) |
| Pico de req/s (aplicação) | 15 req/s |
| Pico de req/s (telemetria) | 2 req/s |
| Disponibilidade desejada | 99,5% |
| W (tempo médio) | 0,2 s |

**Lei de Little:**

```
L (aplicação)  = 15 req/s × 0,2 s = 3 (3 requisições em andamento)
L (telemetria) = 2 req/s × 0,2 s = 0,4
L (total)      = 3,4 ≈ 4 requisições concorrentes
```

**Instâncias necessárias:** 2 instâncias da aplicação (redundância + distribuição de carga).  
**Conexões com banco:** 10 conexões por instância = 20 conexões totais (pool HikariCP configurado).

### Diagrama — Cenário 2

```
[ Usuários (100) ]
        │ HTTPS
        ▼
[ Load Balancer ]
   ┌────┴────┐
   ▼         ▼
┌───────┐ ┌───────┐
│ App 1 │ │ App 2 │   2 instâncias Spring Boot
│ 1 vCPU│ │ 1 vCPU│   1 GB RAM cada
│ 1 GB  │ │ 1 GB  │
└───┬───┘ └───┬───┘
    └─────┬───┘
          │ JDBC (pool: 10 × 2 = 20 conexões)
          ▼
┌──────────────────────────┐
│   PostgreSQL 16          │
│   db.t3.small            │
│   2 vCPU / 2 GB RAM      │
└──────────────────────────┘
```

---

## Cenário 3: Carga Alta

**Perfil:** Hemocentro nacional ou rede de hemocentros regionais, 100+ hospitais, 50 veículos com telemetria contínua.

| Parâmetro | Valor |
|-----------|-------|
| Usuários simultâneos | 500 |
| Requisições hospitalares por dia | 5.000 |
| Leituras de telemetria por dia | 72.000 (50 veículos × 1 leitura/min × 24h) |
| Pico de req/s (aplicação) | 80 req/s |
| Pico de req/s (telemetria) | 15 req/s |
| Disponibilidade desejada | 99,9% |
| W (tempo médio aplicação) | 0,2 s |
| W (tempo médio telemetria) | 0,1 s |

**Lei de Little:**

```
L (aplicação)  = 80 req/s × 0,2 s = 16 requisições em andamento
L (telemetria) = 15 req/s × 0,1 s = 1,5 requisições em andamento
L (total)      ≈ 18 requisições concorrentes
```

**Instâncias necessárias:**  
- Aplicação: 4 instâncias (16 req concorrentes / ~4 req por instância com margem)  
- Telemetria: 2 instâncias separadas (justificativa para extração como microsserviço)  
**Conexões com banco:** 15 por instância × 4 instâncias = 60 conexões (app) + 15 × 2 = 30 (telemetria) = 90 conexões totais.

### Diagrama — Cenário 3

```
[ Usuários (500) ]
        │ HTTPS
        ▼
[ Load Balancer ]
   ┌────┬────┬────┐
   ▼    ▼    ▼    ▼
[ App1 ][ App2 ][ App3 ][ App4 ]   4 instâncias
   2 vCPU / 2 GB RAM cada
        │
   ┌────┘
   ▼
[ Telemetria ][ Telemetria ]   2 instâncias separadas
   1 vCPU / 1 GB RAM
        │
        └──────────┐
                   ▼
        ┌──────────────────────┐
        │  PostgreSQL          │
        │  db.t3.medium        │
        │  2 vCPU / 4 GB RAM   │
        │  (read replica para  │
        │   telemetria)        │
        └──────────────────────┘
              +
        ┌──────────────────────┐
        │  Read Replica        │
        │  (consultas pesadas  │
        │   de telemetria)     │
        └──────────────────────┘
```

---

## Resumo — Comparação dos Cenários

| Parâmetro | Cenário 1 (Baixo) | Cenário 2 (Moderado) | Cenário 3 (Alto) |
|-----------|-------------------|---------------------|-----------------|
| Usuários simultâneos | 10 | 100 | 500 |
| Req/dia | 50 | 500 | 5.000 |
| Pico (req/s) | 2 | 15 | 80 |
| L (concorrência) | 0,4 | 3,4 | 18 |
| Instâncias app | 1 | 2 | 4 |
| Instâncias telemetria | — | — | 2 |
| Conexões banco | 5 | 20 | 90 |
| Disponibilidade | 99,5% | 99,5% | 99,9% |

---

## Orçamento Estimado

> ⚠️ **IMPORTANTE:** Os preços abaixo são estimativas baseadas em pesquisa de preços on-demand de provedores de nuvem. Preços mudam com frequência. Consulte sempre os calculadores oficiais antes de tomar decisões. Os valores estão em **dólar americano (USD)**, região **us-east-1 (AWS)** / **East US (Azure)**.
>
> **Esta seção requer validação com preços atuais dos provedores.** A equipe deve acessar os calculadores oficiais e atualizar os valores com a data de consulta.

### Calculadores oficiais
- AWS Pricing Calculator: https://calculator.aws/pricing/2/home
- Azure Pricing Calculator: https://azure.microsoft.com/pt-br/pricing/calculator/
- Google Cloud Pricing Calculator: https://cloud.google.com/products/calculator
- Render Pricing: https://render.com/pricing

### Referência de Preços (a consultar e validar)

| Serviço | Cenário 1 | Cenário 2 | Cenário 3 |
|---------|-----------|-----------|-----------|
| App (instâncias EC2/ECS) | t3.micro × 1 | t3.small × 2 | t3.medium × 4 |
| Banco (RDS PostgreSQL) | db.t3.micro | db.t3.small | db.t3.medium + read replica |
| Load Balancer | — | ALB | ALB |
| Transferência de dados | Mínima | Moderada | Alta |
| **Custo mensal estimado** | **~$20/mês** | **~$120/mês** | **~$500/mês** |
| **Custo por pedido** | ~$0,013 | ~$0,008 | ~$0,003 |

> **Nota:** Os valores acima são estimativas de referência com base em preços AWS on-demand de 2024/2025. A equipe deve consultar o AWS Pricing Calculator com as configurações exatas e registrar a data da consulta aqui.
>
> **Data de consulta:** ___/___/2026 (a preencher pela equipe)  
> **Fonte:** AWS Pricing Calculator — us-east-1  
> **Observação:** Preços on-demand, sem reserva de instância. Com Reserved Instances (1 ano), o custo pode ser reduzido em ~30%.

### Alternativa PaaS — Render.com

Para o ambiente acadêmico (Cenário 1 e 2), o Render oferece:
- **Plano gratuito:** 1 serviço web + 1 banco PostgreSQL (90 dias). Adequado para demo.
- **Starter ($7/mês):** 1 serviço sempre ativo + banco PostgreSQL ($7/mês). Adequado para Cenário 1.
- **Standard ($25/mês por serviço):** Adequado para Cenário 2 com 2 instâncias.

> Fonte: https://render.com/pricing (consultar para data atual)

---

## Referências

- NYGARD, M. *Documenting Architecture Decisions*, 2011.
- AWS Pricing: https://aws.amazon.com/pricing/
- Render Pricing: https://render.com/pricing
- Lei de Little: LITTLE, J.D.C. *A Proof for the Queuing Formula: L = λW*, 1961.
- Slides das aulas 006 e 007 — Infraestrutura de Software · CESAR School · 2026.2
