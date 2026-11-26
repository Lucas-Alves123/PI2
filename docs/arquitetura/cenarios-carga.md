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
- Aplicação: 4 instâncias (para atender à disponibilidade de 99,9% com redundância N+2 e margem de folga para picos de 2× a 3× sobre L = 16, mantendo cada instância bem abaixo do limite de saturação de CPU/threads).  
- Telemetria: 2 instâncias separadas (justificativa para extração como microsserviço).  
**Conexões com banco (derivadas de L):**  
- Fórmula adotada: `pool por instância = max(5, ceil((L / instâncias) × fator de segurança 3))`.  
- Cenário 1: `max(5, ceil(0,4 × 3)) = 5` conexões (1 instância = 5 conexões totais).  
- Cenário 2: `max(10, ceil((3,4 / 2) × 3)) = 10` conexões por instância (2 instâncias = 20 conexões totais).  
- Cenário 3: `max(15, ceil((16 / 4) × 3)) = 15` conexões por instância Core (`4 × 15 = 60`) + `15 × 2 = 30` nas instâncias de telemetria = **90 conexões totais**.

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

## Análise de Sensibilidade — Variação do Tempo de Processamento (W)

Os cálculos acima assumem W = 0,2 s (200 ms). Na prática, o tempo de processamento varia conforme a carga no banco, a presença de índices e a complexidade da query FEFO. A tabela abaixo mostra como L (concorrência) e as instâncias necessárias se comportam com diferentes valores de W:

| W | Cenário 1 — λ = 2 req/s | Cenário 2 — λ = 15 req/s | Cenário 3 — λ = 80 req/s | Instâncias Core (C3) |
|---|------------------------|--------------------------|--------------------------|----------------------|
| 100 ms | L = 0,2 | L = 1,5 | L = 8,0 | 2 |
| **200 ms (baseline)** | **L = 0,4** | **L = 3,0** | **L = 16,0** | **4** |
| 400 ms | L = 0,8 | L = 6,0 | L = 32,0 | 8 |
| 800 ms | L = 1,6 | L = 12,0 | L = 64,0 | 14 |

> *Nota:* A tabela acima considera apenas a carga da aplicação principal (λ = 2, 15 e 80 req/s). Somando a telemetria no baseline (200 ms), o L total é 0,42 (C1), 3,4 (C2) e 17,5 ≈ 18 (C3), conforme a tabela-resumo anterior.

> Se a query FEFO degradar de 200 ms para 400 ms — por falta de índice em `(status, data_validade)`, por exemplo — o Cenário 3 passa a exigir 8 instâncias em vez de 4, **dobrando o custo de infraestrutura**. Um índice bem colocado resolve; sem ele, o orçamento dobra.

---

## Orçamento Estimado

> Os valores abaixo foram apurados no **AWS Pricing Calculator** em **10 de novembro de 2026**, região **us-east-1 (N. Virginia)**, modalidade **on-demand**. O detalhamento completo — componente a componente — está em [`orcamento.md`](orcamento.md) e na planilha `orcamento.xlsx`.

| Serviço | Cenário 1 | Cenário 2 | Cenário 3 |
|---------|-----------|-----------|-----------|
| App (ECS Fargate) | 1× 0,25 vCPU / 0,5 GB | 2× 0,5 vCPU / 1 GB | 4× Core + 2× Telemetria |
| Banco (RDS PostgreSQL) | db.t3.micro (Single-AZ) | db.t3.small (Single-AZ) | db.t3.medium (Multi-AZ + Read Replica) |
| Load Balancer | — | ALB | ALB |
| **Custo mensal (AWS)** | **$24,57** | **$92,18** | **$417,14** |
| **Custo por pedido** | $0,0164 | $0,0061 | $0,0027 |

> **Observação:** Com Reserved Instances de 1 ano, os custos de ECS e RDS reduzem entre 28% e 38% em relação aos valores on-demand acima.

### Alternativa PaaS — Render.com (Cenários 1 e 2)

| Plano | Custo | Adequado para |
|-------|-------|---------------|
| Starter | $7,00/mês (app) + $7,00/mês (banco) = **$14,00** | Cenário 1 — demo e operação regional |
| Standard | $25,00/mês por serviço | Cenário 2 — 2 instâncias com banco dedicado |

> O Cenário 3 exige infraestrutura dedicada (ECS + Multi-AZ + Read Replica) e não é coberto pelos planos Render.

---

## Referências

- NYGARD, M. *Documenting Architecture Decisions*, 2011.
- AWS Pricing: https://aws.amazon.com/pricing/
- Render Pricing: https://render.com/pricing
- Lei de Little: LITTLE, J.D.C. *A Proof for the Queuing Formula: L = λW*, 1961.
- Slides das aulas 006 e 007 — Infraestrutura de Software · CESAR School · 2026.2
