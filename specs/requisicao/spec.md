# Spec: Alocar bolsa compatível (contexto Requisição)

## Objetivo
Permitir que o atendente registre uma requisição de hemocomponentes para um hospital e que o sistema aloque automaticamente as bolsas compatíveis disponíveis no estoque, priorizando as que vencem primeiro (FEFO).

## Linguagem
- **Hospital:** instituição de saúde solicitante cadastrada no sistema.
- **Requisição:** agregado que representa o pedido de hemocomponentes (tipo ABO/Rh, quantidade e urgência).
- **Alocação:** entidade interna da requisição que vincula uma bolsa reservada ao pedido.
- **Bolsa:** unidade de hemocomponente armazenada no estoque com código, tipo sanguíneo e validade.
- **TipoSanguineo:** objeto de valor imutável com `TipoAbo` e `TipoRh`.
- **Validade:** objeto de valor imutável que guarda a data de vencimento da bolsa e verifica expiração.
- **FEFO:** critério de seleção que prioriza a bolsa com menor data de validade.

## Regras
1. Uma requisição não aceita mais alocações do que a quantidade solicitada.
2. Uma mesma bolsa não pode ser vinculada mais de uma vez à mesma requisição.
3. Bolsas vencidas não podem ser alocadas.
4. Toda bolsa alocada precisa ser compatível em ABO/Rh para o componente solicitado.
5. Status da requisição conforme atendimento:
   - Criada como `PENDENTE`.
   - Com atendimento incompleto, fica `PARCIALMENTE_ATENDIDA`.
   - Quando atinge a quantidade pedida, passa para `ATENDIDA`.
   - Requisições `CANCELADA`, `EM_EXPEDICAO` ou `CONCLUIDA` não aceitam novas alocações.
   - Só avança para `EM_EXPEDICAO` se estiver `ATENDIDA`, e só conclui se estiver `EM_EXPEDICAO`.

## Critérios
- **CA1 (FEFO):** Dado que há bolsas compatíveis com validades diferentes, quando a requisição é registrada, a bolsa que vence mais cedo é alocada primeiro.
- **CA2 (Incompatibilidade):** Quando só existem bolsas incompatíveis no estoque, nenhuma bolsa é alocada e a requisição segue `PENDENTE`.
- **CA3 (Atendimento parcial):** Quando há menos bolsas compatíveis do que o total solicitado, o sistema aloca o que estiver disponível e deixa a requisição como `PARCIALMENTE_ATENDIDA`.

## Eventos
- `RequisicaoAtendida`: publicado pelo contexto de Requisição quando o pedido é totalmente atendido e consumido pelo contexto de Notificação.
- `GuiaEntregue`: publicado pelo contexto de Expedição na entrega e consumido por Requisição e Notificação.

## Fora do escopo
- Roteirização de frota externa.
- Triagem clínica de doadores e coleta.

## Tarefas
- [x] Especificação funcional do fluxo de alocação.
- [x] Objeto de valor imutável `Validade` com validação no construtor.
- [x] Testes unitários do agregado `Requisicao` e do VO `Validade` (ciclo vermelho).
- [x] Implementação das invariantes na raiz `Requisicao` e na `Bolsa` (ciclo verde).
- [x] Isolamento entre contextos por serviços de aplicação e eventos de domínio.
- [x] Validação das camadas com ArchUnit e execução dos cenários no Cucumber.
