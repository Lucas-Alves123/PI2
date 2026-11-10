# Orçamento — Cenários de Carga

**Projeto:** Rota Vital  
**Disciplina:** Infraestrutura de Software · CESAR School · ADS · 2026.2  
**Trabalho:** U2 · Parte 2 · Seção E — Orçamento com preços reais

---

> ⚠️ **ATENÇÃO — Instrução de preenchimento:**
> Os valores marcados com `[PREENCHER]` devem ser consultados pela equipe nos calculadores oficiais listados abaixo e registrados com a data da consulta. A IA não inventa preços de nuvem.
>
> **Calculadores oficiais:**
> - AWS: https://calculator.aws/pricing/2/home
> - Azure: https://azure.microsoft.com/pt-br/pricing/calculator/
> - Google Cloud: https://cloud.google.com/products/calculator
> - Render: https://render.com/pricing

---

## Configurações por Cenário

### Cenário 1 — Baixo (3–5 hospitais)

| Item | Configuração | Qtd | Preço Unit. (USD/mês) | Total (USD/mês) |
|------|-------------|-----|----------------------|-----------------|
| App — AWS ECS Fargate (0.25 vCPU, 512 MB) | on-demand, us-east-1 | 1 | [PREENCHER] | [PREENCHER] |
| Banco — AWS RDS PostgreSQL db.t3.micro | on-demand, us-east-1 | 1 | [PREENCHER] | [PREENCHER] |
| Armazenamento RDS — 20 GB gp3 | — | 1 | [PREENCHER] | [PREENCHER] |
| Transferência de dados — até 1 GB/mês | outbound | 1 | [PREENCHER] | [PREENCHER] |
| **TOTAL ESTIMADO** | | | | **[PREENCHER]** |

**Custo por pedido:** Total mensal / 50 pedidos/dia / 30 dias = `[PREENCHER]` USD/pedido  
**Região:** us-east-1 (Norte da Virgínia)  
**Data da consulta:** ___/___/2026  
**Fonte:** AWS Pricing Calculator

---

### Cenário 2 — Moderado (20–40 hospitais)

| Item | Configuração | Qtd | Preço Unit. (USD/mês) | Total (USD/mês) |
|------|-------------|-----|----------------------|-----------------|
| App — AWS ECS Fargate (0.5 vCPU, 1 GB) | on-demand, us-east-1 | 2 | [PREENCHER] | [PREENCHER] |
| Load Balancer — AWS ALB | — | 1 | [PREENCHER] | [PREENCHER] |
| Banco — AWS RDS PostgreSQL db.t3.small | on-demand, us-east-1 | 1 | [PREENCHER] | [PREENCHER] |
| Armazenamento RDS — 50 GB gp3 | — | 1 | [PREENCHER] | [PREENCHER] |
| Transferência de dados — até 5 GB/mês | outbound | 1 | [PREENCHER] | [PREENCHER] |
| **TOTAL ESTIMADO** | | | | **[PREENCHER]** |

**Custo por pedido:** Total mensal / 500 pedidos/dia / 30 dias = `[PREENCHER]` USD/pedido  
**Região:** us-east-1  
**Data da consulta:** ___/___/2026  
**Fonte:** AWS Pricing Calculator

---

### Cenário 3 — Alto (100+ hospitais)

| Item | Configuração | Qtd | Preço Unit. (USD/mês) | Total (USD/mês) |
|------|-------------|-----|----------------------|-----------------|
| App — AWS ECS Fargate (1 vCPU, 2 GB) | on-demand, us-east-1 | 4 | [PREENCHER] | [PREENCHER] |
| Telemetria — AWS ECS Fargate (0.5 vCPU, 1 GB) | on-demand, us-east-1 | 2 | [PREENCHER] | [PREENCHER] |
| Load Balancer — AWS ALB | — | 1 | [PREENCHER] | [PREENCHER] |
| Banco — AWS RDS PostgreSQL db.t3.medium | on-demand, us-east-1 | 1 | [PREENCHER] | [PREENCHER] |
| Read Replica — db.t3.medium | on-demand | 1 | [PREENCHER] | [PREENCHER] |
| Armazenamento RDS — 200 GB gp3 | — | 1 | [PREENCHER] | [PREENCHER] |
| Transferência de dados — até 50 GB/mês | outbound | 1 | [PREENCHER] | [PREENCHER] |
| **TOTAL ESTIMADO** | | | | **[PREENCHER]** |

**Custo por pedido:** Total mensal / 5.000 pedidos/dia / 30 dias = `[PREENCHER]` USD/pedido  
**Região:** us-east-1  
**Data da consulta:** ___/___/2026  
**Fonte:** AWS Pricing Calculator

---

## Alternativa PaaS — Render.com (Cenários 1 e 2)

| Plano | Serviço | Preço (USD/mês) | Adequado para |
|-------|---------|----------------|--------------|
| Free | 1 web service (hiberna) + PostgreSQL 90 dias | $0 | Demo / entrega acadêmica |
| Starter | 1 web service sempre ativo | $7 | Cenário 1 |
| Standard | 1 web service dedicado | $25 por instância | Cenário 2 |
| Banco PostgreSQL | Managed PostgreSQL | a partir de $7 | Cenário 1 e 2 |

> Fonte: https://render.com/pricing  
> Data da consulta: ___/___/2026

---

## Resumo Comparativo

| Cenário | Plataforma | Custo mensal estimado | Custo por pedido |
|---------|-----------|----------------------|-----------------|
| 1 — Baixo | Render Starter | ~$14/mês | [PREENCHER] |
| 2 — Moderado | AWS ECS + RDS | [PREENCHER] | [PREENCHER] |
| 3 — Alto | AWS ECS + RDS + ALB | [PREENCHER] | [PREENCHER] |

---

## Como preencher este documento

1. Acesse https://calculator.aws/pricing/2/home
2. Selecione os serviços listados em cada cenário
3. Configure região **us-east-1**, preço **on-demand**
4. Copie os valores para a tabela acima
5. Registre a data da consulta e a URL gerada pelo calculador
6. Recalcule o custo por pedido com a fórmula: `Total mensal ÷ pedidos/dia ÷ 30`

> **Lembrete:** Preços on-demand são o maior valor possível (sem desconto de Reserved Instances ou Savings Plans). Com reserva de 1 ano, o custo pode ser ~30% menor.

