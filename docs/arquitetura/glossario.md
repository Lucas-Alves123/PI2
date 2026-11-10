# Glossário — Linguagem Ubíqua do Rota Vital

**Projeto:** Rota Vital  
**Disciplina:** Infraestrutura de Software · CESAR School · ADS · 2026.2  
**Trabalho:** U2 · Parte 2 · Seção A — Glossário

---

## Introdução

A linguagem ubíqua é o vocabulário compartilhado entre desenvolvedores e especialistas do domínio. No Rota Vital, ela garante que termos como "bolsa" e "requisição" signifiquem a mesma coisa para todos — ou que, quando um termo tem significados diferentes em contextos distintos, essa diferença seja explicitamente documentada.

**Atenção:** Alguns termos têm significados diferentes dependendo do contexto. Esses casos são identificados com o símbolo ⚠️.

---

## Termos Gerais

### Hospital
**Definição:** Instituição de saúde cadastrada no Rota Vital que pode solicitar hemocomponentes para atender suas necessidades clínicas.  
**Contexto:** Hospitais  
**Atributos relevantes:** nome, CNPJ (fictício), endereço, cidade, estado, responsável, status (ativo/inativo).  
**Usado em:** Hospitais, Requisição

---

### Unidade Solicitante
**Definição:** Setor ou unidade interna de um hospital habilitada a realizar solicitações de hemocomponentes. Exemplos: UTI, Pronto-Socorro, Bloco Cirúrgico.  
**Contexto:** Hospitais  
**Nota:** Um hospital pode ter várias unidades solicitantes. A unidade identifica a origem da requisição dentro do hospital.  
**Usado em:** Hospitais, Requisição

---

### Requisição
**Definição:** Solicitação formal registrada por um hospital para receber uma quantidade específica de hemocomponentes com determinado tipo sanguíneo e nível de urgência.  
**Contexto:** Requisição  
**Ciclo de status:** Pendente → Em análise → Parcialmente atendida → Atendida → Em expedição → Concluída / Cancelada  
**Usado em:** Requisição, Alocação, Expedição

---

### Alocação
**Definição:** Ato de reservar uma bolsa compatível para uma requisição específica. Uma alocação registra qual bolsa foi reservada, quando e para qual requisição.  
**Contexto:** Alocação  
**Invariante:** Uma bolsa só pode estar alocada em uma requisição ativa por vez.  
**Usado em:** Alocação, Estoque

---

### Compatibilidade
**Definição:** Regra que determina se uma bolsa pode ser transfundida para o receptor considerando os tipos sanguíneos ABO e Rh. A compatibilidade varia conforme o componente (concentrado de hemácias, plaquetas, plasma, etc.).  
**Contexto:** Alocação  
**Nota:** A matriz de compatibilidade utilizada neste sistema é sintética e deve ser validada por profissional habilitado antes de qualquer uso real.  
**Usado em:** Alocação

---

### Tipo Sanguíneo
**Definição:** Classificação do sangue segundo os sistemas ABO (A, B, AB, O) e Rh (positivo ou negativo). Exemplo: "O negativo", "AB positivo".  
**Contexto:** Alocação, Estoque  
**Implementação:** Value Object com atributos `abo` (enum: A, B, AB, O) e `rh` (enum: POSITIVO, NEGATIVO).  
**Usado em:** Alocação, Estoque, Requisição

---

### FEFO (First Expired, First Out)
**Definição:** Regra de priorização que determina que, entre as bolsas compatíveis disponíveis, a que tem a data de validade mais próxima deve ser alocada primeiro. Minimiza o descarte por vencimento.  
**Contexto:** Alocação  
**Analogia:** Similar ao FIFO (First In, First Out) dos estoques convencionais, mas baseado em validade, não em ordem de entrada.  
**Usado em:** Alocação

---

### ⚠️ Bolsa (Estoque)
**Definição no contexto Estoque:** Item individual armazenado no banco de sangue. Identificada por um código único, possui tipo sanguíneo, tipo de hemocomponente, data de coleta, data de validade, localização física e status (Disponível, Reservada, Em separação, Em expedição, Entregue, Descartada, Vencida).  
**Contexto:** Estoque  
**Regra:** Bolsas vencidas não podem ser alocadas. O sistema verifica a validade automaticamente.

---

### ⚠️ Bolsa (Expedição)
**Definição no contexto Expedição:** Volume transportado vinculado a uma guia de expedição. Neste contexto, o que importa não é o conteúdo interno da bolsa, mas sua associação com a guia, o veículo e as condições de temperatura registradas durante o transporte.  
**Contexto:** Expedição  
**Diferença crítica:** No Estoque, "bolsa" é um item de estoque com validade e localização. Na Expedição, "bolsa" é um volume em trânsito, monitorado pela cadeia fria.

> ⚠️ **Atenção:** "Bolsa" tem significados diferentes em Estoque e Expedição. Os dois modelos não devem ser misturados. Um controller de Expedição não deve acessar diretamente o repositório de bolsas do Estoque.

---

### Hemocomponente
**Definição:** Produto derivado do processamento do sangue total. Os principais tipos são: Concentrado de Hemácias (CH), Plasma Fresco Congelado (PFC), Concentrado de Plaquetas (CP) e Crioprecipitado.  
**Contexto:** Estoque, Requisição  
**Nota:** A compatibilidade ABO/Rh varia conforme o hemocomponente.  
**Usado em:** Estoque, Requisição, Alocação

---

### Guia de Expedição
**Definição:** Documento (ou registro digital) que formaliza o envio de bolsas de um banco de sangue para um hospital. Contém as bolsas embarcadas, o veículo, o responsável, a data/hora de saída e o destino.  
**Contexto:** Expedição  
**Usado em:** Expedição, Telemetria

---

### Cadeia Fria
**Definição:** Conjunto de procedimentos e controles que garantem que os hemocomponentes sejam mantidos dentro da faixa de temperatura adequada desde a coleta até a entrega no hospital.  
**Contexto:** Expedição, Telemetria  
**Nota:** Os limites de temperatura são configuráveis no sistema e devem ser definidos pela equipe com base em premissas do domínio. Os valores padrão são sintéticos.  
**Usado em:** Expedição, Telemetria

---

### Telemetria
**Definição:** Registro automático de dados do transporte — temperatura, localização GPS, identificador do veículo e data/hora da leitura. Permite monitorar se a cadeia fria está sendo mantida.  
**Contexto:** Telemetria  
**Usado em:** Telemetria, Expedição

---

### Alerta de Temperatura
**Definição:** Notificação gerada automaticamente quando uma leitura de telemetria indica temperatura fora da faixa configurada para a cadeia fria.  
**Contexto:** Telemetria, Notificação  
**Usado em:** Telemetria, Notificação

---

## Resumo — Termos com Duplo Significado

| Termo | Contexto A | Significado A | Contexto B | Significado B |
|-------|-----------|---------------|-----------|---------------|
| **Bolsa** | Estoque | Item armazenado com validade, localização e status | Expedição | Volume em trânsito vinculado a uma guia e monitorado por temperatura |

---

## Referências

- EVANS, E. *Domain-Driven Design*. Addison-Wesley, 2003.
- VERNON, V. *Implementing Domain-Driven Design*. Addison-Wesley, 2013.
