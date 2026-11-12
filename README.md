# Rota Vital

**Sistema de Gestão Logística de Hemocomponentes**  
CESAR School · Infraestrutura de Software · ADS · 2026.2

---

## Integrantes

| Nome | GitHub |
|------|--------|
| Gabriel Farias | — |
| Gabriel Nogueira | — |
| Lucas Mateus | [@Lucas-Alves123](https://github.com/Lucas-Alves123) |
| Mirella Emily | — |
| Myllena Cristiane | — |

---

## O que é o Rota Vital

O Rota Vital organiza a logística de hemocomponentes desde o controle de estoque até a entrega hospitalar. O sistema recebe requisições clínicas, verifica compatibilidade imunematológica ABO/Rh, aplica a regra **FEFO** (*First-Expired, First-Out*) para alocar prioritariamente as bolsas com vencimento mais próximo e acompanha toda a cadeia de expedição e monitoramento térmico (cadeia fria).

> **Aviso importante:** Todos os dados utilizados no sistema, migrações e testes (hospitais, bolsas, responsáveis e leituras) são **100% sintéticos e fictícios**, elaborados exclusivamente para fins acadêmicos.

---

## Arquitetura (DDD e Três Camadas)

O sistema foi remodelado e implementado como um **Monolito Modular orientado ao domínio (DDD)**, seguindo o planejamento documentado em [`docs/arquitetura/`](docs/arquitetura/) e guiado pela especificação funcional em [`specs/requisicao/spec.md`](specs/requisicao/spec.md) (SDD).

### 1. Bounded Contexts Implementados

Cada contexto delimitado possui seu próprio pacote raiz em `src/main/java/br/cesar/rotavital/` e se comunica com outros contextos exclusivamente pela camada pública de aplicação (`*Service`) ou por **eventos de domínio** em memória (`ApplicationEventPublisher` / `@EventListener`), sem acesso cruzado a repositórios ou tabelas:

| Bounded Context | Pacote | Tipo | Responsabilidade e Modelo |
|-----------------|--------|------|---------------------------|
| **Requisição** | `br.cesar.rotavital.requisicao` | Core | Recebimento de pedidos hospitalares, proteção de invariantes de atendimento e alocação FEFO por compatibilidade ABO/Rh. |
| **Estoque** | `br.cesar.rotavital.estoque` | Core | Custódia das bolsas físicas (`Bolsa`), controle de validade, reserva e descarte sanitário. |
| **Expedição** | `br.cesar.rotavital.expedicao` | Suporte | Emissão de `GuiaExpedicao`, despacho de veículos e confirmação de entrega no destino. |
| **Telemetria** | `br.cesar.rotavital.telemetria` | Suporte | Registro de leituras térmicas (`LeituraTelemetria`) da cadeia fria (2 °C a 6 °C) e detecção de alertas. |
| **Hospitais** | `br.cesar.rotavital.hospitais` | Suporte | Cadastro e consulta de instituições solicitantes (`Hospital` e `Unidade`). |
| **Notificação** | `br.cesar.rotavital.notificacao` | Genérico | Registro de avisos operacionais disparados por eventos de domínio (`Notificacao`). |
| **Identidade** | `br.cesar.rotavital.identidade` | Genérico | Autenticação e controle de acesso por perfil (`Usuario`, `PerfilUsuario`). |
| **Painel** | `br.cesar.rotavital.painel` | Apresentação | Composição de indicadores consolidados do dashboard consultando os serviços públicos de cada contexto. |

### 2. As Três Camadas Lógicas em Cada Contexto

Dentro de cada Bounded Context, o código respeita estritamente a separação em três camadas lógicas:

```text
br.cesar.rotavital/
├── requisicao/                           (Bounded Context Core)
│   ├── apresentacao/
│   │   └── RequisicoesWebController.java (Camada de Apresentação — HTTP/Thymeleaf)
│   ├── negocio/
│   │   ├── aplicacao/
│   │   │   ├── RequisicaoService.java    (Casos de uso e fronteira pública do contexto)
│   │   │   ├── AlocacaoFefoService.java  (Orquestração FEFO + publicação de evento)
│   │   │   └── ConclusaoDaRequisicaoListener.java (@EventListener de GuiaEntregue)
│   │   └── dominio/
│   │       ├── Requisicao.java           (Raiz do Agregado — protege invariantes)
│   │       ├── Alocacao.java             (Entidade interna do agregado)
│   │       ├── StatusRequisicao.java
│   │       ├── StatusAlocacao.java
│   │       ├── Urgencia.java
│   │       └── eventos/
│   │           └── RequisicaoAtendida.java (Evento de domínio publicado)
│   └── dados/
│       ├── RequisicaoRepositoryJpa.java  (Camada de Dados — Spring Data JPA)
│       └── AlocacaoRepositoryJpa.java
├── estoque/
│   ├── apresentacao/EstoqueWebController.java
│   ├── negocio/
│   │   ├── aplicacao/EstoqueService.java, BolsaCandidata.java, ResumoEstoque.java
│   │   └── dominio/Bolsa.java, StatusBolsa.java
│   └── dados/BolsaRepositoryJpa.java
├── expedicao/
│   ├── apresentacao/ExpedicaoWebController.java
│   ├── negocio/
│   │   ├── aplicacao/ExpedicaoService.java
│   │   └── dominio/GuiaExpedicao.java, StatusExpedicao.java, eventos/GuiaEntregue.java
│   └── dados/GuiaExpedicaoRepositoryJpa.java
├── hospitais/
│   ├── apresentacao/HospitaisWebController.java
│   ├── negocio/
│   │   ├── aplicacao/HospitalService.java
│   │   └── dominio/Hospital.java, Unidade.java
│   └── dados/HospitalRepositoryJpa.java
├── telemetria/
│   ├── apresentacao/TelemetriaWebController.java
│   ├── negocio/
│   │   ├── aplicacao/TelemetriaService.java
│   │   └── dominio/LeituraTelemetria.java
│   └── dados/LeituraTelemetriaRepositoryJpa.java
├── notificacao/
│   ├── negocio/
│   │   ├── aplicacao/NotificacaoService.java, EventosDeDominioListener.java
│   │   └── dominio/Notificacao.java, TipoNotificacao.java
│   └── dados/NotificacaoRepositoryJpa.java
├── identidade/
│   ├── apresentacao/SecurityConfig.java
│   ├── negocio/
│   │   ├── aplicacao/AutenticacaoService.java
│   │   └── dominio/Usuario.java, PerfilUsuario.java
│   └── dados/UsuarioRepositoryJpa.java
└── shared/
    ├── TipoSanguineo.java                (Value Object imutável — compatibilidade ABO/Rh)
    ├── Validade.java                     (Value Object imutável — regras de vencimento/FEFO)
    ├── TipoAbo.java, TipoRh.java, Componente.java
    └── RegraDeNegocioException.java
```

*Nota sobre relação com o plano da Parte 2:* Os Value Objects compartilhados entre `estoque` e `requisicao` (`TipoSanguineo` e `Validade`) foram centralizados no pacote `shared` (núcleo compartilhado / *Shared Kernel*) para evitar duplicação da matriz imunematológica e da regra de expiração. As regras de dependência entre camadas e o isolamento entre contextos são verificados automaticamente na esteira por testes **ArchUnit** (`RegrasDeArquiteturaTest`).

### 3. Onde Estão o Agregado, Objetos de Valor e Eventos

- **Raiz do Agregado (`Requisicao`):** [`src/main/java/br/cesar/rotavital/requisicao/negocio/dominio/Requisicao.java`](src/main/java/br/cesar/rotavital/requisicao/negocio/dominio/Requisicao.java)
  - Único ponto de alteração das alocações (`getAlocacoes()` devolve lista não modificável).
  - Rejeita alocação acima da quantidade solicitada, bolsas vencidas, bolsas repetidas e transições inválidas de status (`PENDENTE` $\rightarrow$ `PARCIALMENTE_ATENDIDA` $\rightarrow$ `ATENDIDA` $\rightarrow$ `EM_EXPEDICAO` $\rightarrow$ `CONCLUIDA`).
- **Objetos de Valor Imutáveis:**
  - [`TipoSanguineo`](src/main/java/br/cesar/rotavital/shared/TipoSanguineo.java): valida ABO e Rh no construtor e encapsula a matriz de compatibilidade por hemocomponente (`podeDoarPara`).
  - [`Validade`](src/main/java/br/cesar/rotavital/shared/Validade.java): valida data não nula, exige vencimento posterior à coleta (`Validade.posteriorA`), calcula dias restantes e compara qual bolsa vence primeiro para o FEFO.
- **Eventos de Domínio entre Contextos:**
  - `RequisicaoAtendida`: publicado por `AlocacaoFefoService` quando todas as bolsas da requisição são alocadas; consumido por `EventosDeDominioListener` no contexto de Notificação.
  - `GuiaEntregue`: publicado por `ExpedicaoService` ao confirmar a entrega; consumido por `ConclusaoDaRequisicaoListener` (contexto Requisição, que conclui o pedido) e `EventosDeDominioListener` (contexto Notificação).
- **Documentação completa da arquitetura:** [`docs/arquitetura/`](docs/arquitetura/) e especificação SDD em [`specs/requisicao/spec.md`](specs/requisicao/spec.md).

---

## Como Executar os Testes (TDD, ArchUnit e BDD Cucumber)

```bash
# 1. Testes unitários do domínio e objetos de valor + regras de arquitetura (ArchUnit)
#    Roda sem banco e sem subir o Spring nos testes de domínio:
./mvnw test

# 2. Suíte completa (unitários + ArchUnit + cenários Gherkin com Cucumber):
./mvnw verify
```

- **Testes Unitários (TDD):**
  - `RequisicaoTest` — invariantes do agregado `Requisicao` (limite de quantidade, rejeição de bolsa vencida, proteção da coleção interna, transições de status).
  - `ValidadeTest` — validação no construtor, comparação FEFO e cálculo de expiração do VO `Validade`.
  - `CompatibilidadeTest` — matriz parametrizada de compatibilidade ABO/Rh (`TipoSanguineo`).
  - `BolsaTest` — ciclo de vida da bolsa no estoque.
  - `RegrasDeArquiteturaTest` — garante com ArchUnit que o domínio não depende de Spring/Web/Dados e que nenhum contexto acessa repositórios de outro.
- **Cenários BDD (Cucumber + Gherkin em português):**
  - Arquivo: [`src/test/resources/features/h1_alocacao_fefo.feature`](src/test/resources/features/h1_alocacao_fefo.feature)
  - Runner e Steps: `AceitacaoIT` e `AlocacaoSteps` em `src/test/java/br/cesar/rotavital/aceitacao/`
  - Executa os 3 cenários (alocação FEFO com evento de domínio, rejeição de incompatível e alocação parcial). Com Docker ativo (como no GitHub Actions), sobe PostgreSQL 16 via Testcontainers; sem Docker local, utiliza banco em memória compatível automaticamente.

---

## Como Executar a Aplicação Localmente

```bash
git clone https://github.com/Lucas-Alves123/PI2.git
cd PI2
cp .env.example .env
docker compose up -d --build
```

- **Interface Web:** `http://localhost:8080`
- **Health Check (Smoke Test):** `http://localhost:8080/actuator/health`
- **Credenciais sintéticas de acesso:**
  - `admin` / `admin123` (Administrador)
  - `estoque1` / `admin123` (Estoque)
  - `atend1` / `admin123` (Atendimento)
  - `exped1` / `admin123` (Expedição)

---

## Esteira CI/CD e Deploy

A esteira em [`.github/workflows/pipeline.yml`](.github/workflows/pipeline.yml) executa em três estágios:
1. **`testes`**: roda `./mvnw verify -B` (testes unitários TDD, ArchUnit e cenários BDD Cucumber). Qualquer teste vermelho bloqueia os estágios seguintes.
2. **`imagem`**: constrói a imagem Docker multi-stage e publica no GitHub Container Registry (`ghcr.io/lucas-alves123/rotavital:latest`).
3. **`deploy`**: aciona o deploy da nova versão e executa o **teste de fumaça (smoke test)** consultando `GET /actuator/health` e validando `"status":"UP"`.

- **URL do Ambiente / Repositório:** `https://github.com/Lucas-Alves123/PI2` (configurável via secrets `RENDER_DEPLOY_HOOK_URL` e `RENDER_APP_URL` no Render).

---

## Uso de IA (PO-CESAR-021 — Categoria Condicionado)

Em conformidade com a política acadêmica **PO-CESAR-021 (categoria Condicionado)**, registramos abaixo o uso de ferramentas de Inteligência Artificial Generativa durante o projeto, detalhando a finalidade e o processo de validação humana realizado pelo squad:

| Etapa / Artefato | Ferramenta Utilizada | Finalidade do Uso | Como Foi Validado pelo Squad |
|------------------|---------------------|-------------------|------------------------------|
| Modelagem DDD e Documentação (`docs/arquitetura/`) | Assistente de IA Generativa (LLM) | Apoio na estruturação redacional dos subdomínios, glossário da linguagem ubíqua, ADRs e diagramas Mermaid. | Revisão integral pelos integrantes confrontando cada definição com os slides das aulas 006 e 007 e com as regras de hemoterapia do Rota Vital. |
| Especificação SDD e Cenários Gherkin (`specs/`, `.feature`) | Assistente de IA Generativa (LLM) | Rascunho inicial da especificação funcional e dos passos Dado/Quando/Então para a história âncora de alocação FEFO. | Validação manual dos critérios de aceite e execução automatizada via `cucumber-java` e `cucumber-spring` na esteira. |
| Refatoração em Contextos, Agregado e VOs (`Requisicao`, `Validade`, `TipoSanguineo`) | Assistente de IA Generativa (LLM) | Sugestão de estrutura de pacotes em três camadas, implementação de objetos de valor imutáveis e publicação de eventos Spring. | Verificação linha a linha das invariantes de negócio, execução do ciclo TDD (vermelho $\rightarrow$ verde $\rightarrow$ refatoração) e validação estrutural com ArchUnit. |
| Testes Automatizados e Pipeline CI/CD (`*Test`, `pipeline.yml`) | Assistente de IA Generativa (LLM) | Apoio na escrita de testes parametrizados JUnit 5, regras ArchUnit e configuração do workflow GitHub Actions. | Execução local com `./mvnw verify` e inspeção dos logs de build e smoke test (`/actuator/health`) no GitHub Actions. |
| Orçamento de Nuvem (`docs/arquitetura/orcamento.md`) | Calculadoras oficiais AWS / GCP | Os preços unitários **não** foram gerados por IA; o assistente auxiliou apenas na formatação de tabelas a partir das premissas da Lei de Little. | Conferência direta nas calculadoras públicas oficiais (AWS Pricing Calculator) com registro de região e data de consulta. |
