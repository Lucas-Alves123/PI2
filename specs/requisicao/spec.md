# Spec: Alocar bolsa compatível (contexto Requisição)

## Objetivo
Permitir que o atendente hospitalar registre uma requisição de hemocomponentes para um hospital e que o sistema selecione e aloque automaticamente as bolsas compatíveis disponíveis no estoque, priorizando rigorosamente as bolsas com data de validade mais próxima de expirar (política First-Expired, First-Out — FEFO).

## Linguagem Ubíqua
- **Hospital:** Instituição de saúde solicitante cadastrada no sistema.
- **Requisição:** Agregado que formaliza o pedido de hemocomponentes com tipo ABO/Rh, quantidade e urgência.
- **Alocação:** Vínculo interno da requisição com uma bolsa física reservada.
- **Bolsa:** Unidade física de hemocomponente armazenada no estoque com código, tipo sanguíneo e validade.
- **TipoSanguineo:** Objeto de valor composto por TipoAbo (A, B, AB, O) e TipoRh (+, -).
- **Validade:** Objeto de valor que encapsula a data de vencimento da bolsa e suas regras de expiração.
- **FEFO (First-Expired, First-Out):** Critério de ordenação prioritária por menor data de validade.

## Regras e Invariantes do Agregado
1. **Invariante de Quantidade:** Uma requisição não pode aceitar alocações além da quantidade total solicitada.
2. **Invariante de Unicidade:** Uma mesma bolsa física não pode ser vinculada mais de uma vez à mesma requisição.
3. **Invariante de Validade:** Nenhuma bolsa com data de validade expirada pode ser alocada.
4. **Invariante de Compatibilidade:** Toda bolsa alocada deve ser estritamente compatível pelo padrão imunematológico ABO/Rh para o hemocomponente requisitado.
5. **Invariante de Transição de Estado:**
   - Ao criar a requisição: status inicial `PENDENTE`.
   - Com alocações parciais: status `PARCIALMENTE_ATENDIDA`.
   - Com todas as cotas supridas: status `ATENDIDA`.
   - Requisições em status `CANCELADA` ou `CONCLUIDA` rejeitam novas alocações.
   - O avanço para `EM_EXPEDICAO` exige que o status prévio seja `ATENDIDA`.

## Critérios de Aceite (Gherkin / BDD)
- **CA1 (FEFO):** Dado que existem múltiplas bolsas compatíveis no estoque com validades distintas, quando a requisição for processada, a bolsa com data de validade mais próxima deve ser reservada primeiro.
- **CA2 (Incompatibilidade):** Quando houver apenas bolsas de tipo sanguíneo incompatível no estoque, nenhuma alocação deve ser realizada e a requisição deve permanecer `PENDENTE`.
- **CA3 (Alocação Parcial):** Quando o estoque possuir menos bolsas compatíveis do que a quantidade solicitada, o sistema aloca as disponíveis e transiciona o status para `PARCIALMENTE_ATENDIDA`.

## Eventos de Domínio
- **RequisicaoAtendidaEvent:** Publicado quando a requisição atinge o status `ATENDIDA` (todas as cotas alocadas). Consumido assincronamente pelo contexto de Expedição para preparação da Guia de Expedição.

## Fora do Escopo
- Gestão de rotas de transporte e telemetria de veículos (pertencente ao contexto Expedição e Telemetria).
- Triagem clínica de doadores e coleta externa (pertencente a sistemas legados de hemocentro).

## Tarefas de Implementação
- [x] Criar especificação SDD guiando a modelagem do domínio.
- [ ] Implementar Value Object imutável `Validade` com validações no construtor.
- [ ] Escrever testes unitários TDD verificando rejeição de alocação excessiva e validade expirada (Ciclo Vermelho).
- [ ] Refatorar agregado `Requisicao` e entidade `Bolsa` para proteger as invariantes (Ciclo Verde).
- [ ] Implementar desacoplamento entre contextos através de interfaces públicas (`EstoquePublicService`).
- [ ] Publicar evento de domínio `RequisicaoAtendidaEvent` e consumidor no contexto de Expedição.
- [ ] Mover controllers web para os pacotes de apresentação de seus respectivos contextos.

