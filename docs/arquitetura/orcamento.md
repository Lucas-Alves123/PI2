# Orçamento — Dimensionamento e Custos de Infraestrutura

**Projeto:** Rota Vital — Gestão Logística de Hemocomponentes  
**Disciplina:** Infraestrutura de Software · CESAR School · ADS · 2026.2  
**Trabalho:** U2 · Parte 2 · Seção E — Orçamento

---

## 1. Premissas de Consulta
- **Provedor:** Amazon Web Services (AWS) e Render PaaS
- **Região:** us-east-1 (N. Virginia)
- **Modalidade:** Preços On-Demand em dólar (USD)
- **Data da Consulta:** 10 de Novembro de 2026
- **Fonte Oficial:** AWS Pricing Calculator (calculator.aws) e Render Pricing

---

## 2. Cenário 1 — Carga Baixa (Operação Regional / 3 a 5 Hospitais)
- **Volume:** 50 pedidos/dia (1.500/mês) | 10 usuários simultâneos | Pico: 2 req/s

| Componente / Recurso | Especificação | Qtd | Preço Unitário | Custo Mensal (USD) |
| :--- | :--- | :---: | :---: | :---: |
| **AWS ECS Fargate (Aplicação)** | 0.25 vCPU, 0.5 GB RAM (730h) | 1 | $0.0125/hora | $9.13 |
| **AWS RDS PostgreSQL** | db.t3.micro (Single-AZ, 1 vCPU, 1 GB) | 1 | $0.0180/hora | $13.14 |
| **Armazenamento RDS** | General Purpose SSD (gp3) - 20 GB | 1 | $0.115/GB-mês | $2.30 |
| **Data Transfer Out** | Transferência de dados (~10 GB/mês) | 1 | Primeiros 100 GB Free | $0.00 |
| **Total Mensal AWS (Cenário 1)** | | | | **$24.57** |

> **Custo por Pedido (AWS):** $24.57 ÷ 1.500 pedidos = **$0.0164 (aprox. 1,6 centavos de dólar por bolsa/pedido)**

### Alternativa em PaaS (Render)
- Web Service Starter: $7.00/mês
- PostgreSQL Starter (1 GB RAM / 10 GB SSD): $7.00/mês
- **Total Render Cenário 1:** **$14.00/mês** ($0.0093 por pedido)

---

## 3. Cenário 2 — Carga Moderada (Estadual / 20 a 40 Hospitais)
- **Volume:** 500 pedidos/dia (15.000/mês) | 100 usuários simultâneos | Pico: 15 req/s

| Componente / Recurso | Especificação | Qtd | Preço Unitário | Custo Mensal (USD) |
| :--- | :--- | :---: | :---: | :---: |
| **AWS ECS Fargate (Aplicação)** | 0.5 vCPU, 1 GB RAM (730h) | 2 | $0.0250/hora | $36.50 |
| **AWS Application Load Balancer** | 1 ALB (~15 LCU consumidos) | 1 | $0.0225/hora + LCU | $22.50 |
| **AWS RDS PostgreSQL** | db.t3.small (Single-AZ, 2 vCPU, 2 GB) | 1 | $0.0360/hora | $26.28 |
| **Armazenamento RDS** | General Purpose SSD (gp3) - 60 GB | 1 | $0.115/GB-mês | $6.90 |
| **Data Transfer Out** | Transferência de dados (~50 GB/mês) | 1 | Free tier (<100 GB) | $0.00 |
| **Total Mensal AWS (Cenário 2)** | | | | **$92.18** |

> **Custo por Pedido (AWS):** $92.18 ÷ 15.000 pedidos = **$0.0061 (aprox. 0,6 centavos de dólar por pedido)**

---

## 4. Cenário 3 — Carga Alta (Rede Nacional / 100+ Hospitais + Telemetria Contínua)
- **Volume:** 5.000 pedidos/dia (150.000/mês) | 500 usuários simultâneos | Pico: 95 req/s (App + Telemetria)

| Componente / Recurso | Especificação | Qtd | Preço Unitário | Custo Mensal (USD) |
| :--- | :--- | :---: | :---: | :---: |
| **AWS ECS Fargate (Core App)** | 1.0 vCPU, 2 GB RAM (730h) | 4 | $0.0499/hora | $145.71 |
| **AWS ECS Fargate (Telemetria)** | 0.5 vCPU, 1 GB RAM (730h) | 2 | $0.0250/hora | $36.50 |
| **AWS Application Load Balancer** | 1 ALB com roteamento de path | 1 | $0.0225/hora + LCU | $35.00 |
| **AWS RDS PostgreSQL (Master)** | db.t3.medium (Multi-AZ, 2 vCPU, 4 GB) | 1 | $0.1440/hora | $105.12 |
| **AWS RDS Read Replica** | db.t3.medium (Single-AZ, réplica telemetria) | 1 | $0.0720/hora | $52.56 |
| **Armazenamento RDS** | General Purpose SSD (gp3) - 250 GB | 1 | $0.115/GB-mês | $28.75 |
| **Data Transfer Out** | Transferência de dados (~250 GB/mês) | 1 | 150 GB cobrados ($0.09) | $13.50 |
| **Total Mensal AWS (Cenário 3)** | | | | **$417.14** |

> **Custo por Pedido (AWS):** $417.14 ÷ 150.000 pedidos = **$0.0027 (aprox. 0,27 centavos de dólar por pedido)**

---

## 5. Resumo Consolidado de Custos

| Cenário | Pedidos/mês | Instâncias App | Banco de Dados | Custo Mensal (USD) | Custo / Pedido (USD) |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **1 — Baixo** | 1.500 | 1x ECS (0.25 vCPU) | db.t3.micro (Single) | **$24.57** | $0.0164 |
| **2 — Moderado** | 15.000 | 2x ECS + ALB | db.t3.small (Single) | **$92.18** | $0.0061 |
| **3 — Alto** | 150.000 | 6x ECS + ALB | db.t3.medium (Multi-AZ + Replica) | **$417.14** | $0.0027 |

### Observações sobre Otimização de Custos
1. **Economia de Escala:** Conforme o volume aumenta de 1.500 para 150.000 pedidos, o custo por pedido cai de $0.0164 para $0.0027 (redução de mais de 80%), diluindo a infraestrutura base.
2. **Savings Plans & Instâncias Reservadas:** Contratos de 1 ou 3 anos com a AWS reduzem os custos de ECS e RDS entre 28% e 38% em relação aos valores on-demand listados.
