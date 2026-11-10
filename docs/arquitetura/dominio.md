# Domínio — Rota Vital

**Projeto:** Rota Vital — Sistema de Gestão Logística de Hemocomponentes  
**Disciplina:** Infraestrutura de Software · CESAR School · ADS · 2026.2  
**Trabalho:** U2 · Parte 2 · Seção A — Remodelagem do Domínio com DDD

---

## O que é o Rota Vital

O Rota Vital é um sistema de gestão logística de hemocomponentes. Ele organiza o controle de estoque de bolsas de sangue, o atendimento de solicitações hospitalares, a alocação de bolsas compatíveis e o acompanhamento da expedição e do transporte em cadeia fria.

O sistema permite que hospitais registrem solicitações de hemocomponentes, que o estoque seja consultado e que todo o processo — desde a separação até a entrega — seja acompanhado com rastreabilidade.

**Todos os dados são sintéticos e fictícios.**

---

## Subdomínios

O domínio do Rota Vital foi dividido em sete subdomínios, classificados em três categorias: Core, Suporte e Genérico.

### Core — Subdomínios que diferenciam o Rota Vital

São os subdomínios que contêm as regras de negócio exclusivas e estratégicas do sistema. Sem eles, o Rota Vital não existe.

#### 1. Requisição e Alocação
**Responsabilidade:** Receber solicitações de hemocomponentes dos hospitais, verificar a compatibilidade ABO/Rh, aplicar a regra de prioridade FEFO (First Expired, First Out) e alocar as bolsas disponíveis para cada requisição.

**Justificativa:** É o coração do sistema. Nenhum outro software de logística de hemocomponentes toma as mesmas decisões — compatibilidade sanguínea, FEFO e controle de reserva simultânea são regras exclusivas deste domínio.

---

#### 2. Estoque de Hemocomponentes
**Responsabilidade:** Controlar o ciclo de vida das bolsas de sangue — entrada, validade, localização, disponibilidade e descarte. Garantir que bolsas vencidas não sejam alocadas.

**Justificativa:** O controle de estoque de hemocomponentes tem regras próprias: vencimento crítico, rastreabilidade por bolsa individual e restrições de uso. Não é um estoque genérico.

---

#### 3. Expedição e Cadeia Fria
**Responsabilidade:** Organizar guias de expedição, registrar rotas, acompanhar entregas e monitorar as condições de temperatura durante o transporte.

**Justificativa:** A cadeia fria é requisito regulatório no transporte de hemocomponentes. A lógica de emissão de guias, controle de temperatura e registro de entrega é específica deste domínio.

---

### Suporte — Subdomínios que apoiam os contextos core

São importantes para o funcionamento do sistema, mas não são o diferencial competitivo.

#### 4. Hospitais
**Responsabilidade:** Cadastrar e manter informações dos hospitais e unidades solicitantes. Fornecer dados de identidade para os outros contextos.

**Justificativa:** Sem o cadastro de hospitais, nenhuma solicitação pode ser feita. Mas as regras de cadastro são simples e não exigem lógica especializada.

---

#### 5. Telemetria
**Responsabilidade:** Registrar leituras de temperatura, localização GPS e dados de veículos durante o transporte. Gerar alertas quando os valores saem da faixa configurada.

**Justificativa:** Apoia o contexto de Expedição e Cadeia Fria, mas pode ser separado fisicamente em um serviço independente — especialmente em cenários de alto volume, onde o número de leituras cresce muito mais rápido do que o número de requisições.

---

### Genérico — Subdomínios que podem ser comprados ou reutilizados

São subdomínios cujas soluções já existem no mercado e não precisam ser desenvolvidas do zero.

#### 6. Notificação
**Responsabilidade:** Enviar alertas e notificações internas — nova solicitação, bolsa próxima do vencimento, expedição iniciada, entrega concluída, temperatura fora dos limites.

**Justificativa:** O mecanismo de envio de notificações (e-mail, SMS, push) é genérico. O que é específico do Rota Vital é *quando* notificar — e essa lógica pertence aos outros contextos.

---

#### 7. Identidade e Acesso
**Responsabilidade:** Gerenciar usuários, autenticação, perfis e permissões. Controlar quem pode acessar cada parte do sistema.

**Justificativa:** Autenticação e autorização são resolvidos por soluções de mercado (Spring Security, OAuth2, Keycloak). Não há lógica de negócio exclusiva aqui.

---

## Bounded Contexts

Cada subdomínio é implementado como um Bounded Context independente, com seu próprio modelo de domínio e linguagem ubíqua.

| # | Bounded Context | Tipo | Responsabilidade em uma frase |
|---|----------------|------|-------------------------------|
| 1 | **Requisição** | Core | Recebe e gerencia solicitações de hemocomponentes dos hospitais |
| 2 | **Alocação** | Core | Verifica compatibilidade, aplica FEFO e reserva bolsas para cada requisição |
| 3 | **Estoque** | Core | Controla o ciclo de vida das bolsas desde a entrada até o descarte |
| 4 | **Expedição** | Core | Emite guias, organiza o transporte e registra a entrega |
| 5 | **Hospitais** | Suporte | Cadastra e mantém hospitais e unidades solicitantes |
| 6 | **Telemetria** | Suporte | Registra leituras de temperatura e localização durante o transporte |
| 7 | **Notificação** | Genérico | Envia alertas e avisos para os usuários do sistema |
| 8 | **Identidade** | Genérico | Gerencia usuários, autenticação e permissões de acesso |

> **Nota:** Os contextos de Requisição e Alocação são separados na classificação de responsabilidade, mas na implementação atual (monolito modular) compartilham o mesmo módulo. A separação física em serviços independentes é descrita na projeção de microsserviços.

---

## Referências

- EVANS, E. *Domain-Driven Design*. Addison-Wesley, 2003.
- VERNON, V. *Implementing Domain-Driven Design*. Addison-Wesley, 2013.
- Slides das aulas 006 e 007 — Infraestrutura de Software · CESAR School · 2026.2
