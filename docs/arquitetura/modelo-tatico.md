# Modelo Tático — Bounded Context: Requisição e Alocação

**Projeto:** Rota Vital  
**Disciplina:** Infraestrutura de Software · CESAR School · ADS · 2026.2  
**Trabalho:** U2 · Parte 2 · Seção B — Modelo Tático de um Contexto Core

---

## Contexto escolhido: Requisição e Alocação

Este é o Bounded Context principal (Core) do Rota Vital. Ele concentra as regras de negócio mais críticas: receber solicitações de hemocomponentes, verificar compatibilidade ABO/Rh, aplicar a regra FEFO e reservar bolsas de maneira segura e exclusiva.

---

## Persona

**Nome fictício:** Ana Beatriz Costa  
**Papel:** Atendente de Hemoterapia — responsável por registrar solicitações hospitalares no Rota Vital  
**Contexto:** Ana trabalha no banco de sangue. Quando um hospital liga pedindo hemocomponentes, ela acessa o sistema, informa o hospital, o tipo sanguíneo, o componente e a urgência. O sistema faz o restante: verifica estoque, checa compatibilidade, aplica FEFO e reserva as bolsas.  
**Frustração atual (sem sistema):** Sem o Rota Vital, Ana consulta planilhas, verifica o estoque manualmente e corre o risco de reservar a mesma bolsa para dois hospitais ao mesmo tempo.

---

## História de Usuário Âncora

> **Como** atendente de hemoterapia responsável pelo atendimento de um hospital,  
> **Quero** solicitar bolsas de sangue informando o tipo sanguíneo e o componente necessário,  
> **Para que** o sistema selecione e reserve automaticamente as bolsas compatíveis disponíveis, priorizando as que vencem primeiro.

---

## Critérios de Aceite

Os critérios abaixo são testáveis e estão vinculados às invariantes do agregado.

**CA1 — Rejeitar bolsas incompatíveis**  
Dado que existe uma bolsa do tipo AB+ disponível,  
quando o sistema processar uma requisição solicitando O-,  
então a bolsa AB+ não deve ser alocada para essa requisição.

**CA2 — Priorizar a validade mais próxima (FEFO)**  
Dado que existem duas bolsas O+ disponíveis com datas de validade diferentes,  
quando o sistema alocar bolsas para uma requisição O+,  
então a bolsa com vencimento mais próximo deve ser alocada primeiro.

**CA3 — Impedir reserva duplicada**  
Dado que uma bolsa já está reservada para a Requisição #001,  
quando o sistema tentar alocar a mesma bolsa para a Requisição #002,  
então o sistema deve rejeitar a operação e manter a bolsa vinculada apenas à Requisição #001.

**CA4 — Concluir requisição somente com todas as alocações realizadas**  
Dado que uma requisição solicita 3 bolsas e apenas 2 foram alocadas,  
quando o sistema verificar o status da requisição,  
então o status deve permanecer como "Parcialmente atendida" e não "Atendida".

---

## Agregado: Requisição

### Raiz do Agregado — `Requisicao`

A `Requisicao` é a raiz do agregado. Ela controla todo o ciclo de vida da solicitação e garante que as invariantes sejam respeitadas.

```java
// Representação conceitual (não é código de produção final)
public class Requisicao {
    private UUID id;
    private UUID hospitalId;           // referência ao contexto Hospitais
    private UUID unidadeId;            // referência à unidade solicitante
    private TipoSanguineo tipoSanguineo; // Value Object
    private Componente componente;     // enum: CH, PFC, CP, CRIO
    private int quantidadeSolicitada;
    private Urgencia urgencia;         // enum: ROTINA, URGENCIA, EMERGENCIA
    private StatusRequisicao status;
    private LocalDateTime criadaEm;
    private List<Alocacao> alocacoes;  // entidades internas
}
```

**Invariantes protegidas pela raiz:**
- Não permite adicionar uma alocação com bolsa já alocada em outra requisição ativa.
- Não permite mudar o status para ATENDIDA se `alocacoes.size() < quantidadeSolicitada`.
- Não permite cancelar uma requisição que já está em expedição.

---

### Entidade Interna — `Alocacao`

A `Alocacao` representa a reserva de uma bolsa específica para esta requisição. Ela não tem existência fora do agregado `Requisicao`.

```java
// Representação conceitual
public class Alocacao {
    private UUID id;
    private UUID bolsaId;              // referência ao contexto Estoque
    private Componente componente;
    private LocalDate dataVencimentoBolsa; // copiado no momento da alocação (FEFO)
    private LocalDateTime dataAlocacao;
    private StatusAlocacao status;     // enum: RESERVADA, CONFIRMADA, CANCELADA
}
```

**Regra:** `bolsaId` é uma referência ao contexto de Estoque — não é um objeto `Bolsa` importado. Os contextos não compartilham classes de domínio.

---

### Value Object — `TipoSanguineo`

Imutável, sem identidade própria. Dois objetos com o mesmo ABO e Rh são considerados iguais.

```java
// Representação conceitual
public final class TipoSanguineo {
    private final ABO abo;      // enum: A, B, AB, O
    private final Rh rh;        // enum: POSITIVO, NEGATIVO
    
    // Construtor valida que abo e rh não são nulos
    // equals e hashCode baseados em abo + rh
    // toString: "O-", "AB+", "A+"
    
    public boolean ehCompativel(TipoSanguineo receptor, Componente componente) {
        // Consulta a MatrizCompatibilidade (configurável)
        // Retorna true se este tipo pode ser doado para o receptor
    }
}
```

**Nota sobre a matriz de compatibilidade:**  
A tabela abaixo é **sintética e deve ser validada por profissional habilitado** antes de qualquer uso real. É uma premissa do projeto para fins acadêmicos.

| Tipo do doador | Pode ser recebido por (Concentrado de Hemácias) |
|---------------|------------------------------------------------|
| O- | O-, O+, A-, A+, B-, B+, AB-, AB+ |
| O+ | O+, A+, B+, AB+ |
| A- | A-, A+, AB-, AB+ |
| A+ | A+, AB+ |
| B- | B-, B+, AB-, AB+ |
| B+ | B+, AB+ |
| AB- | AB-, AB+ |
| AB+ | AB+ |

> ⚠️ **Premissa não validada:** Esta matriz representa a compatibilidade para Concentrado de Hemácias. Para outros componentes (plaquetas, plasma), as regras diferem. A `MatrizCompatibilidade` deve ser configurável no sistema.

---

## Invariantes do Agregado

| # | Invariante | Onde é verificada |
|---|-----------|------------------|
| 1 | Só bolsas compatíveis com o tipo sanguíneo solicitado podem ser alocadas | `Requisicao.alocar()` |
| 2 | Entre as bolsas compatíveis, priorizar a que vence primeiro (FEFO) | `ServicoAlocacao.selecionarBolsas()` |
| 3 | Uma bolsa não pode estar reservada em duas requisições ativas simultaneamente | `BolsaRepository` + constraint de banco |
| 4 | Uma requisição só pode ter status ATENDIDA quando todas as alocações estão confirmadas | `Requisicao.concluir()` |
| 5 | Bolsas vencidas não podem ser alocadas | `Estoque` (verificado antes de retornar bolsas disponíveis) |

---

## Enumerações

```java
enum Componente {
    CH,   // Concentrado de Hemácias
    PFC,  // Plasma Fresco Congelado
    CP,   // Concentrado de Plaquetas
    CRIO  // Crioprecipitado
}

enum Urgencia {
    ROTINA,
    URGENCIA,
    EMERGENCIA
}

enum StatusRequisicao {
    PENDENTE,
    EM_ANALISE,
    PARCIALMENTE_ATENDIDA,
    ATENDIDA,
    EM_EXPEDICAO,
    CONCLUIDA,
    CANCELADA
}

enum StatusAlocacao {
    RESERVADA,
    CONFIRMADA,
    CANCELADA
}
```

---

## Referências

- EVANS, E. *Domain-Driven Design*. Addison-Wesley, 2003.
- VERNON, V. *Implementing Domain-Driven Design*. Addison-Wesley, 2013.
- Slides das aulas 006 e 007 — Infraestrutura de Software · CESAR School · 2026.2
