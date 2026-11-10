# Diagramas de Arquitetura — Rota Vital

**Projeto:** Rota Vital — Gestão Logística de Hemocomponentes  
**Disciplina:** Infraestrutura de Software · CESAR School · ADS · 2026.2  
**Trabalho:** U2 · Parte 2

Estes diagramas estão em formato Mermaid. Você pode visualizá-los diretamente no GitHub, no VS Code / Antigravity ou exportá-los em imagem / PDF para o relatório final e slides da apresentação.

---

## 1. Mapa de Contextos (Context Map — DDD)

Mostra os 7 Bounded Contexts e seus relacionamentos (Upstream/Downstream, Camada Anticorrupção - ACL, Conformista e Publicador-Assinante).

```mermaid
flowchart TD
    subgraph Core ["Subdomínios Core"]
        REQ["Bounded Context: Requisição"]
        ALOC["Bounded Context: Alocação"]
        EST["Bounded Context: Estoque"]
        EXP["Bounded Context: Expedição"]
    end

    subgraph Suporte ["Subdomínios de Suporte"]
        HOSP["Bounded Context: Hospitais"]
        TEL["Bounded Context: Telemetria"]
    end

    subgraph Generico ["Subdomínios Genéricos"]
        NOTIF["Bounded Context: Notificação"]
        ID["Bounded Context: Identidade e Acesso"]
    end

    HOSP -- "Fornecedor / ACL [hospitalId]" --> REQ
    REQ <--> |"Cliente-Fornecedor (FEFO / Reserva)"| ALOC
    ALOC -- "Consulta / Reserva com ACL" --> EST
    REQ -- "Evento: RequisicaoAtendida" --> EXP
    EXP <--> |"BolsaTransportada (Volume)"| EST
    EXP -- "Dados de Rota" --> TEL
    
    TEL -- "Evento: AlertaTemperatura" --> NOTIF
    REQ -- "Evento: RequisicaoRegistrada" --> NOTIF
    EST -- "Evento: BolsaVencendo" --> NOTIF
    
    ID -. "Autenticação e Perfis (Conformista)" .-> REQ
    ID -. "Autenticação e Perfis (Conformista)" .-> EST
    ID -. "Autenticação e Perfis (Conformista)" .-> EXP
    ID -. "Autenticação e Perfis (Conformista)" .-> HOSP
```

---

## 2. Modelo Tático do Agregado Principal (Requisição)

Representa a raiz do agregado (`Requisicao`), entidade interna (`Alocacao`) e Objeto de Valor (`TipoSanguineo`).

```mermaid
classDiagram
    class Requisicao {
        -Long id
        -Long hospitalId
        -Long unidadeId
        -TipoSanguineo tipoSanguineo
        -Componente componente
        -int quantidade
        -Urgencia urgencia
        -StatusRequisicao status
        -OffsetDateTime criadoEm
        -List~Alocacao~ alocacoes
        +adicionarAlocacao(Long bolsaId, LocalDate validade)
        +atualizarStatusAtendimento()
        +cancelar(String justificativa)
        +marcarEmExpedicao()
        +marcarConcluida()
    }

    class Alocacao {
        -Long id
        -Long bolsaId
        -LocalDate dataValidade
        -StatusAlocacao status
        -OffsetDateTime alocadoEm
        +confirmar()
        +cancelar()
    }

    class TipoSanguineo {
        <<Value Object>>
        -TipoAbo abo
        -TipoRh rh
        +podeDoarPara(TipoSanguineo receptor, Componente comp) boolean
    }

    Requisicao "1" *-- "0..*" Alocacao : Contém
    Requisicao ..> TipoSanguineo : Usa
```

---

## 3. Arquitetura Lógica em Três Camadas

Destaca a regra de dependência (apenas para baixo, domínio isolado).

```mermaid
flowchart TD
    subgraph Apresentacao ["1. Camada de Apresentação"]
        C1["DashboardWebController"]
        C2["RequisicoesWebController"]
        C3["EstoqueWebController"]
        C4["ExpedicaoWebController"]
        C5["TelemetriaWebController"]
        V["Templates Thymeleaf (HTML / CSS)"]
    end

    subgraph Negocio ["2. Camada de Negócio"]
        subgraph Aplicacao ["Aplicação / Casos de Uso"]
            S1["AlocacaoFefoService"]
            S2["AutenticacaoService"]
        end
        subgraph Dominio ["Domínio Puro (Entidades, VOs, Regras)"]
            D1["Requisicao (Agregado)"]
            D2["Bolsa (Entidade)"]
            D3["TipoSanguineo (Value Object)"]
            D4["GuiaExpedicao (Entidade)"]
            D5["Invariantes e Regras FEFO"]
        end
    end

    subgraph Dados ["3. Camada de Dados"]
        R1["RequisicaoRepositoryJpa"]
        R2["BolsaRepositoryJpa"]
        R3["GuiaExpedicaoRepositoryJpa"]
        R4["Flyway Migrations (SQL)"]
        BD[("PostgreSQL 16")]
    end

    Apresentacao --> Aplicacao
    Aplicacao --> Dominio
    Aplicacao --> Dados
    Dados --> BD
```

---

## 4. Projeção de Microsserviços e Saga Coreografada

Demonstra o fluxo assíncrono com compensação (caso de indisponibilidade de veículo).

```mermaid
sequenceDiagram
    autonumber
    actor Atendente
    participant MS_Req as Serviço de Requisição & Estoque
    participant Broker as Message Broker (Filas/Eventos)
    participant MS_Exp as Serviço de Expedição
    participant MS_Tel as Serviço de Telemetria
    participant MS_Notif as Serviço de Notificação

    Atendente->>MS_Req: Solicita Hemocomponentes
    MS_Req->>MS_Req: Reserva Bolsas compatíveis (FEFO)
    MS_Req->>Broker: Publica RequisicaoAtendida
    
    Broker->>MS_Exp: Consome RequisicaoAtendida
    MS_Exp->>MS_Exp: Verifica Veículo Disponível
    
    alt Veículo Disponível (Sucesso)
        MS_Exp->>Broker: Publica GuiaEmitida
        Broker->>MS_Tel: Inicia monitoramento de temperatura
        Broker->>MS_Notif: Notifica Hospital sobre envio
    else Veículo Indisponível (Falha -> Compensação)
        MS_Exp->>Broker: Publica ExpedicaoCancelada
        Broker->>MS_Req: Consome ExpedicaoCancelada
        MS_Req->>MS_Req: Compensa: Cancela reserva e libera bolsas no estoque!
        Broker->>MS_Notif: Alerta operadores sobre falha logística
    end
```

---

## 5. Diagramas Físicos de Carga (Cenários 1, 2 e 3)

### Cenário 1 — Carga Baixa (Pequeno porte / 1 instância)
```mermaid
flowchart LR
    User["10 Usuários (Pico: 2 req/s)"] --> App["1x App Spring Boot (512MB RAM / 1 vCPU)"]
    App --> DB[("PostgreSQL 16 (Pool: 5 conexões)")]
```

### Cenário 2 — Carga Moderada (20-40 hospitais / 2 instâncias + LB)
```mermaid
flowchart LR
    Users["100 Usuários (Pico: 15 req/s)"] --> LB["Application Load Balancer"]
    LB --> App1["App Instância 1 (1GB RAM)"]
    LB --> App2["App Instância 2 (1GB RAM)"]
    App1 --> DB[("PostgreSQL 16 (Pool: 20 conexões)")]
    App2 --> DB
```

### Cenário 3 — Carga Alta (100+ hospitais / Microsserviço de Telemetria + Read Replica)
```mermaid
flowchart TD
    Users["500 Usuários (Pico: 80 req/s app + 15 req/s telemetria)"] --> LB["Load Balancer"]
    LB --> CorePool["Cluster App Core (4x Instâncias)"]
    LB --> TelPool["Cluster Telemetria (2x Instâncias)"]
    CorePool --> DB_Master[("PostgreSQL Master (Escritas/Transações)")]
    TelPool --> DB_Replica[("PostgreSQL Read Replica (Telemetria/Leituras)")]
    DB_Master -. "Replicação contínua" .-> DB_Replica
```
