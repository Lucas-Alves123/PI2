# Roteiro da Apresentação — Rota Vital (10 Minutos)

**Trabalho:** U2 · Parte 2 — Planejamento da Remodelagem Arquitetural  
**Disciplina:** Infraestrutura de Software · CESAR School · ADS · 2026.2  
**Tempo Total:** 10 minutos de fala + 5 minutos de arguição da banca

---

## Divisão do Tempo e Tópicos

### 1. Introdução e Contexto (1 Minuto)
- **O que é o Rota Vital:** Sistema logístico para gestão de hemocomponentes (sangue total, hemácias, plaquetas, plasma).
- **Problema resolvido:** Eliminar o controle manual em bancos de sangue, automatizando triagem ABO/Rh, validade crítica (FEFO) e controle de temperatura (cadeia fria).
- **Onde estamos:** A Parte 1 entregou a base de CI/CD e containerização. A Parte 2 remodela o domínio e a arquitetura para suportar o crescimento do sistema.

---

### 2. Remodelagem com DDD — Seções A e B (3 Minutos)
- **Subdomínios:**
  - *Core (Diferencial):* Requisição & Alocação, Estoque e Expedição.
  - *Suporte:* Hospitais e Telemetria de veículos.
  - *Genérico:* Identidade/Acesso e Notificações.
- **Linguagem Ubíqua & Termo Chave ("Bolsa"):**
  - Mostrar a diferença semântica: No **Estoque**, a *Bolsa* é um item individual com tipo sanguíneo e vencimento. Na **Expedição**, a *Bolsa* é tratada como volume de carga associado a uma guia e monitorado por temperatura.
- **Modelo Tático (Agregado Principal):**
  - Raiz `Requisicao`, entidades `Alocacao` e Objeto de Valor `TipoSanguineo`.
  - **Invariantes obrigatórias:**
    1. Rejeição de bolsas incompatíveis;
    2. Prioridade estrita por FEFO (First Expired, First Out);
    3. Proteção contra reserva duplicada;
    4. Atendimento concluído apenas após suprir 100% da quantidade solicitada.
- **BDD (Cucumber/Gherkin):**
  - Apresentar o cenário de alocação automática priorizando a bolsa com validade mais próxima.

---

### 3. Três Camadas e Projeção de Microsserviços — Seções C e D (3 Minutos)
- **Arquitetura Lógica em 3 Camadas:**
  - *Apresentação:* Controllers REST e telas Thymeleaf.
  - *Negócio:* Casos de uso (Aplicação) orquestrando o Domínio puro, sem acoplamento a frameworks.
  - *Dados:* Repositórios JPA e banco relacional.
  - *Camada Lógica vs. Tier Físico:* As 3 camadas rodam em 1 processo Java (Tier 1), conectando-se via rede ao PostgreSQL (Tier 2).
- **Projeção para Microsserviços:**
  - Justificativa para manter o Monolito Modular inicialmente (menor complexidade operacional para o time).
  - Candidato prioritário para extração futura: **Serviço de Telemetria** (volume de leituras de GPS e temperatura cresce desproporcionalmente).
- **Saga Coreografada:**
  - Apresentar o fluxo de emissão de guia e a **compensação**: se o veículo estiver quebrado/indisponível, o evento de cancelamento dispara a liberação automática das bolsas reservadas no estoque.
- **ADRs:** Citar brevemente a ADR 0001 (Monolito Modular) e ADR 0002 (Stack Java/Spring/Postgres).

---

### 4. Cenários de Carga e Orçamento — Seção E (3 Minutos)
- **Aplicação da Lei de Little ($L = \lambda \times W$):**
  - Explicar como o pico de requisições por segundo ($\lambda$) e o tempo médio de resposta ($W = 200\text{ ms}$) determinaram a concorrência média e o número de instâncias:
    - *Cenário 1 (Baixo):* $\lambda = 2\text{ req/s} \rightarrow L = 0,4 \rightarrow$ 1 instância (5 conexões).
    - *Cenário 2 (Moderado):* $\lambda = 15\text{ req/s} \rightarrow L = 3,0 \rightarrow$ 2 instâncias + ALB (20 conexões).
    - *Cenário 3 (Alto):* $\lambda = 95\text{ req/s} \rightarrow L = 17,5 \rightarrow$ 4 instâncias Core + 2 Telemetria (90 conexões).
- **Orçamento AWS (Região us-east-1):**
  - Apresentar a tabela de custos on-demand:
    - Baixo: **$24.57/mês** ($0.016 por pedido);
    - Moderado: **$92.18/mês** ($0.006 por pedido);
    - Alto: **$417.14/mês** ($0.002 por pedido).
  - Destacar o ganho de escala: redução de mais de 80% no custo unitário por pedido processado.

---

## Dicas para as Perguntas da Banca (5 Minutos)

1. *"Por que não começaram direto com microsserviços?"*
   - **Resposta:** Para evitar a sobrecarga de consistência eventual, rede distribuída e infraestrutura complexa em um estágio onde as fronteiras de domínio ainda estão sendo validadas e o time é enxuto. O monólito modular garante os mesmos limites de código sem a dor operacional.
2. *"Como vocês garantem que duas pessoas não reservam a mesma bolsa?"*
   - **Resposta:** Através da invariante do agregado protegida com constraint única no banco de dados na tabela de alocação (`UNIQUE(bolsa_id)`) associada a transações com isolamento no Spring.
3. *"De onde vieram os limites de temperatura da telemetria?"*
   - **Resposta:** São dados sintéticos e premissas configuráveis (`application.yml`), com faixas sintéticas padrão de 2°C a 6°C que foram marcadas na documentação como pendentes de validação clínica.

