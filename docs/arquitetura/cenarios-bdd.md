# Cenários BDD — Requisição e Alocação

**Projeto:** Rota Vital  
**Disciplina:** Infraestrutura de Software · CESAR School · ADS · 2026.2  
**Trabalho:** U2 · Parte 2 · Seção B — Cenário Gherkin

---

## Funcionalidade: Alocar bolsas para uma requisição hospitalar

```gherkin
# language: pt
Funcionalidade: Alocar bolsas compatíveis para uma requisição hospitalar
  Como atendente de hemoterapia responsável pelo atendimento de um hospital
  Quero solicitar bolsas de sangue informando o tipo sanguíneo e o componente necessário
  Para que o sistema selecione e reserve automaticamente as bolsas compatíveis disponíveis
       priorizando as que vencem primeiro

  Contexto:
    Dado que o hospital "Hospital São Lucas" está cadastrado no sistema
    E a usuária "Ana Beatriz" está autenticada com o perfil "Atendimento"

  # CA2 — FEFO: priorizar a bolsa com vencimento mais próximo
  Cenário: Alocar a bolsa compatível com vencimento mais próximo (FEFO)
    Dado que existem as seguintes bolsas disponíveis no estoque:
      | bolsaId | tipoSanguineo | componente | dataValidade |
      | BOLSA-A | O+            | CH         | 2026-11-25   |
      | BOLSA-B | O+            | CH         | 2026-11-15   |
      | BOLSA-C | O+            | CH         | 2026-12-01   |
    Quando Ana Beatriz registra uma requisição para "Hospital São Lucas" solicitando:
      | campo              | valor |
      | tipoSanguineo      | O+    |
      | componente         | CH    |
      | quantidade         | 1     |
      | urgencia           | ROTINA|
    Então o sistema deve alocar a bolsa "BOLSA-B" (vencimento 2026-11-15)
    E o status da requisição deve ser "Atendida"
    E a bolsa "BOLSA-B" deve ter status "Reservada" no estoque

  # CA1 — Rejeitar bolsa incompatível
  Cenário: Não alocar bolsa com tipo sanguíneo incompatível
    Dado que existe apenas a bolsa "BOLSA-X" do tipo "AB+" com componente "CH" disponível
    Quando Ana Beatriz registra uma requisição solicitando tipo "O-" componente "CH"
    Então o sistema não deve alocar nenhuma bolsa
    E o status da requisição deve ser "Pendente"
    E o sistema deve informar que não há bolsas compatíveis disponíveis

  # CA3 — Impedir reserva duplicada
  Cenário: Não alocar a mesma bolsa para duas requisições ativas
    Dado que a bolsa "BOLSA-D" do tipo "A+" componente "CH" está reservada para a requisição "REQ-001"
    Quando o sistema tentar alocar "BOLSA-D" para uma nova requisição "REQ-002" solicitando "A+" CH
    Então o sistema não deve alocar "BOLSA-D" para "REQ-002"
    E a bolsa "BOLSA-D" deve permanecer vinculada apenas à requisição "REQ-001"

  # CA4 — Requisição parcialmente atendida
  Cenário: Manter status como "Parcialmente atendida" quando não há bolsas suficientes
    Dado que existe apenas 1 bolsa "O+" de componente "CH" disponível no estoque
    Quando Ana Beatriz registra uma requisição solicitando 3 bolsas "O+" de componente "CH"
    Então o sistema deve alocar 1 bolsa
    E o status da requisição deve ser "Parcialmente atendida"
    E o sistema deve informar que apenas 1 de 3 bolsas foram alocadas

  # Regra de urgência — emergência tem prioridade (cenário adicional)
  Cenário: Requisição de emergência é processada antes de requisições de rotina
    Dado que existe apenas 1 bolsa "B-" de componente "CH" disponível
    E existe uma requisição de "ROTINA" pendente para "B-" CH
    Quando o sistema registra uma nova requisição de "EMERGENCIA" para "B-" CH
    Então o sistema deve priorizar a alocação para a requisição de "EMERGENCIA"
```

---

## Mapeamento Critérios de Aceite ↔ Cenários Gherkin

| Critério de Aceite | Cenário Gherkin | Invariante |
|-------------------|----------------|-----------|
| CA1 — Rejeitar incompatíveis | "Não alocar bolsa com tipo sanguíneo incompatível" | Invariante 1 |
| CA2 — Priorizar FEFO | "Alocar a bolsa compatível com vencimento mais próximo" | Invariante 2 |
| CA3 — Impedir duplicata | "Não alocar a mesma bolsa para duas requisições ativas" | Invariante 3 |
| CA4 — Concluir com todas alocações | "Manter status como Parcialmente atendida..." | Invariante 4 |

---

## Nota sobre a Implementação (Parte 3)

Estes cenários serão convertidos em testes executáveis com **Cucumber** (mesma ferramenta já usada no Mesa DJ) na Parte 3 do trabalho. Os steps (`@Dado`, `@Quando`, `@Então`) serão implementados em Java, usando Testcontainers para subir o PostgreSQL nos testes de aceitação.

O cenário **principal** para a Parte 3 é o FEFO (CA2) — será o primeiro cenário implementado e executado.

---

## Referências

- EVANS, E. *Domain-Driven Design*. Addison-Wesley, 2003.
- CUCUMBER. *Gherkin Reference*. Disponível em: https://cucumber.io/docs/gherkin/
- Slides das aulas 006 e 007 — Infraestrutura de Software · CESAR School · 2026.2
