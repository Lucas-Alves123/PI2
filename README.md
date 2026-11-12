# Rota Vital

Sistema de Gestão Logística de Hemocomponentes  
CESAR School · Infraestrutura de Software · ADS · 2026.2

## Integrantes

| Nome | GitHub |
|------|--------|
| Gabriel Farias | - |
| Gabriel Nogueira | - |
| Lucas Mateus | [@Lucas-Alves123](https://github.com/Lucas-Alves123) |
| Mirella Emily | - |
| Myllena Cristiane | - |

## Sobre o projeto

O Rota Vital gerencia o fluxo de distribuição de hemocomponentes entre o hemocentro e os hospitais atendidos. O sistema recebe requisições hospitalares, valida a compatibilidade ABO/Rh, aplica a regra FEFO (*First-Expired, First-Out*) para reservar primeiro as bolsas com vencimento mais próximo e acompanha a expedição e a temperatura de transporte.

Todos os dados do repositório (hospitais, bolsas, motoristas, usuários e leituras) são sintéticos e criados apenas para uso acadêmico.

## Arquitetura

O sistema foi construído como um monolito modular seguindo DDD e arquitetura em três camadas. O detalhamento de domínio, glossário, ADRs e cenários de carga está em [`docs/arquitetura/`](docs/arquitetura/), e a especificação funcional usada na implementação está em [`specs/requisicao/spec.md`](specs/requisicao/spec.md).

### Contextos (Bounded Contexts)

Cada contexto fica no seu próprio pacote dentro de `src/main/java/br/cesar/rotavital/` e conversa com os demais apenas pelos serviços da camada de aplicação ou por eventos de domínio (`ApplicationEventPublisher` e `@EventListener`), sem acessar repositórios de outro contexto:

- `requisicao` (Core): registro de pedidos hospitalares, proteção das invariantes do agregado e alocação FEFO.
- `estoque` (Core): cadastro de bolsas, controle de validade, reserva e descarte.
- `expedicao` (Suporte): emissão de guias de expedição, saída para transporte e confirmação de entrega.
- `telemetria` (Suporte): registro de temperatura durante o transporte (faixa de 2 °C a 6 °C) e geração de alertas.
- `hospitais` (Suporte): cadastro e consulta de hospitais e unidades solicitantes.
- `notificacao` (Genérico): avisos internos gerados a partir dos eventos de domínio.
- `identidade` (Genérico): autenticação e perfis de acesso.
- `painel`: controller do dashboard principal que consolida indicadores consultando os serviços de cada contexto.

### Camadas e estrutura de pacotes

Cada contexto é dividido nas camadas `apresentacao`, `negocio` (`aplicacao` e `dominio`) e `dados`:

```text
br.cesar.rotavital/
├── requisicao/
│   ├── apresentacao/RequisicoesWebController.java
│   ├── negocio/
│   │   ├── aplicacao/RequisicaoService.java, AlocacaoFefoService.java, ConclusaoDaRequisicaoListener.java
│   │   └── dominio/Requisicao.java, Alocacao.java, StatusRequisicao.java, StatusAlocacao.java, Urgencia.java
│   │       └── eventos/RequisicaoAtendida.java
│   └── dados/RequisicaoRepositoryJpa.java, AlocacaoRepositoryJpa.java
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
    ├── TipoSanguineo.java
    ├── Validade.java
    ├── TipoAbo.java, TipoRh.java, Componente.java
    └── RegraDeNegocioException.java
```

Em relação à árvore planejada na Parte 2, os objetos de valor `TipoSanguineo` e `Validade` ficaram no pacote `shared` para serem compartilhados entre `estoque` e `requisicao` sem duplicar regra de negócio.

### Onde estão o agregado, os objetos de valor e os eventos

- **Agregado `Requisicao`:** [`src/main/java/br/cesar/rotavital/requisicao/negocio/dominio/Requisicao.java`](src/main/java/br/cesar/rotavital/requisicao/negocio/dominio/Requisicao.java) é a raiz do agregado e o único ponto que altera a lista de `Alocacao`. Impede alocação além da quantidade pedida, bloqueia bolsa vencida ou duplicada e valida as mudanças de status.
- **Objetos de valor imutáveis:**
  - [`TipoSanguineo`](src/main/java/br/cesar/rotavital/shared/TipoSanguineo.java): valida ABO e Rh no construtor e verifica compatibilidade entre doador e receptor.
  - [`Validade`](src/main/java/br/cesar/rotavital/shared/Validade.java): valida a data no construtor, garante que o vencimento é posterior à coleta e compara qual bolsa vence antes no FEFO.
- **Eventos de domínio:**
  - `RequisicaoAtendida`: disparado quando a requisição completa todas as alocações e ouvido pelo contexto de Notificação.
  - `GuiaEntregue`: disparado ao confirmar a entrega da guia e ouvido pelos contextos de Requisição (para concluir o pedido) e Notificação.

## Como rodar os testes

```bash
# Testes unitários do domínio, objetos de valor e regras de arquitetura (ArchUnit)
./mvnw test

# Suíte completa: unitários + ArchUnit + cenários BDD com Cucumber
./mvnw verify
```

- Os testes unitários ficam em `src/test/java/br/cesar/rotavital/` (`RequisicaoTest`, `ValidadeTest`, `CompatibilidadeTest`, `BolsaTest` e `RegrasDeArquiteturaTest`).
- Os cenários Gherkin ficam em [`src/test/resources/features/h1_alocacao_fefo.feature`](src/test/resources/features/h1_alocacao_fefo.feature) e são executados pelo Cucumber (`AceitacaoIT` e `AlocacaoSteps`).

## Como subir a aplicação com Docker

```bash
cp .env.example .env
docker compose up -d --build
```

- Aplicação web: `http://localhost:8080`
- Health check: `http://localhost:8080/actuator/health`
- Logins de teste (senha `admin123`): `admin`, `estoque1`, `atend1`, `exped1`

## Deploy e esteira CI/CD

O workflow em [`.github/workflows/pipeline.yml`](.github/workflows/pipeline.yml) roda a cada push/PR executando `./mvnw verify -B`. Quando o push é na `main`, publica a imagem no GitHub Container Registry (`ghcr.io/lucas-alves123/rotavital:latest`), dispara o deploy no Render e valida a aplicação com um teste de fumaça em `/actuator/health`.

URL da aplicação publicada:

> **<https://rotavital.onrender.com>**

## Uso de IA (PO-CESAR-021 - Categoria Condicionado)

Conforme a política PO-CESAR-021 (categoria Condicionado), o uso de ferramentas de IA generativa no projeto seguiu o registro abaixo:

| Etapa | Ferramenta | Finalidade | Validação feita pelo grupo |
|-------|------------|------------|----------------------------|
| Documentação de arquitetura (`docs/arquitetura/`) | Assistente de IA (LLM) | Apoio na formatação dos textos de subdomínios, glossário, ADRs e diagramas Mermaid. | Revisão pelo grupo comparando com os conteúdos das aulas 006 e 007. |
| Especificação e BDD (`specs/`, `.feature`) | Assistente de IA (LLM) | Estruturação inicial da spec e escrita dos cenários Dado/Quando/Então. | Conferência das regras de negócio e execução dos cenários no Cucumber. |
| Código de domínio e camadas | Assistente de IA (LLM) | Apoio na refatoração de pacotes, criação dos objetos de valor e eventos do Spring. | Testes unitários cobrindo as invariantes do agregado e verificação de dependências com ArchUnit. |
| Testes e pipeline CI/CD | Assistente de IA (LLM) | Apoio na configuração do JUnit 5, Cucumber e GitHub Actions. | Execução local com `./mvnw verify` e checagem da esteira no GitHub. |
| Orçamento (`docs/arquitetura/orcamento.md`) | Calculadoras oficiais de nuvem | Os valores não foram gerados por IA; o assistente ajudou apenas na montagem das tabelas. | Consulta direta na calculadora oficial da AWS com registro de região e data. |
