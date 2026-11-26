# Plano de Refatoração (DDD, Clean Architecture, TDD e SDD)

**Projeto:** Rota Vital  
**Disciplina:** Infraestrutura de Software · CESAR School · ADS · 2026.2

Este documento consolida o planejamento de refatoração do Rota Vital entre a versão inicial (Parte 1) e a entrega final (Parte 3), respondendo diretamente aos cinco pontos de evolução arquitetural definidos para a refatoração.

---

## 1. Modelo de Domínio Rico (Fim das Entidades Anêmicas)

Na versão inicial, classes de entidade tendiam a concentrar apenas atributos com getters e setters, deixando regras de validação espalhadas em controllers ou serviços. Na remodelagem com DDD:

- **Nenhum setter público livre no domínio:** nenhuma entidade em `negocio.dominio` expõe métodos `set...` genéricos. Toda alteração de estado ocorre por métodos de negócio com nomes do glossário ubíquo.
- **Raiz do Agregado (`Requisicao`):** único ponto de mutação das alocações (`getAlocacoes()` devolve `Collections.unmodifiableList`). Protege cinco invariantes diretamente no domínio:
  1. `adicionarAlocacao(...)` rejeita alocação acima da quantidade solicitada.
  2. Rejeita alocação de bolsa vencida usando o objeto de valor `Validade`.
  3. Rejeita vinculação duplicada da mesma bolsa na mesma requisição.
  4. Bloqueia novas alocações quando o status é `CANCELADA`, `EM_EXPEDICAO` ou `CONCLUIDA`.
  5. Protege as transições de estado (`marcarEmExpedicao()` exige `ATENDIDA`; `marcarConcluida()` exige `EM_EXPEDICAO`; `cancelar(...)` cancela todas as alocações vinculadas).
- **Entidade `Bolsa` (`estoque`):** concentra as regras de `reservar()`, `liberar()`, `descartar(motivo)`, `marcarComoVencida()` e `estaVencida(referencia)`.
- **Entidade `GuiaExpedicao` (`expedicao`):** protege o ciclo `PREPARANDO -> EM_TRANSITO -> ENTREGUE` via `iniciarTransito()` e `confirmarEntrega()`.
- **Objetos de Valor imutáveis (`shared`):** `TipoSanguineo` (encapsula a matriz ABO/Rh em `podeDoarPara`) e `Validade` (valida data não nula e posterior à coleta em `Validade.posteriorA`, além de `estaVencidaEm`, `venceAntesDe` e `diasRestantes`).

---

## 2. Arquitetura em Camadas e Clean Architecture

Cada Bounded Context (`requisicao`, `estoque`, `expedicao`, `hospitais`, `telemetria`, `notificacao`, `identidade`) foi estruturado em camadas com direção única de dependência voltada para o centro (regra de dependência da Clean Architecture):

```text
Apresentação (Controllers Web)
        │
        ▼
Negócio / Aplicação (Casos de uso, Serviços públicos e Listeners de eventos)
        │
        ▼
Negócio / Domínio (Agregado, Entidades, Value Objects e Eventos de domínio)
        ▲
        │
Dados (Repositórios Spring Data JPA e migrações Flyway)
```

Para evitar regressão arquitetural ao longo do desenvolvimento, essas regras são verificadas automaticamente a cada build pela classe `RegrasDeArquiteturaTest` com **ArchUnit**:
- `dominioNaoConheceApresentacaoNemDados`: impede qualquer classe em `..negocio.dominio..` de importar `apresentacao`, `dados` ou `aplicacao`.
- `dominioNaoDependeDoSpring`: garante que o núcleo de domínio não importa pacotes `org.springframework..`.
- `controllersNaoAcessamRepositorios`: impede que a camada de apresentação consulte diretamente `..dados..`.
- `nenhumContextoAcessaOsDadosDeOutro`: garante que os 7 contextos não acessem os repositórios uns dos outros; a integração entre contextos ocorre apenas pela camada de aplicação ou por eventos de domínio (`RequisicaoAtendida` e `GuiaEntregue`).

---

## 3. O que Muda Efetivamente na Refatoração (Antes x Depois)

| Dimensão | Antes (Versão Inicial) | Depois (Versão Refatorada para 01/12) |
|----------|------------------------|---------------------------------------|
| Organização de pacotes | Controllers concentrados em um único pacote `apresentacao/` global | Cada Bounded Context possui suas próprias camadas `apresentacao/`, `negocio/aplicacao/`, `negocio/dominio/` e `dados/` |
| Acoplamento entre contextos | `DashboardWebController`, `ExpedicaoWebController` e `AlocacaoFefoService` injetavam repositórios JPA de outros contextos | Comunicação apenas por serviços públicos de aplicação (`EstoqueService`, `RequisicaoService`, `HospitalService`, etc.) e eventos de domínio |
| Proteção de invariantes | `Requisicao` permitia adicionar alocações além da quantidade, aceitava bolsa vencida e expunha lista mutável; `Bolsa` tinha `setStatus` público | `Requisicao` barra excesso de quantidade, bolsa vencida e transições inválidas; lista imutável; `setStatus` removido da `Bolsa` |
| Objetos de valor | Apenas `TipoSanguineo`; validade tratada como `LocalDate` solto | Criado objeto de valor imutável `Validade` com validação no construtor e comparação para FEFO |
| Integração orientada a eventos | Chamadas diretas e acopladas entre módulos | Publicação de `RequisicaoAtendida` e `GuiaEntregue` via `ApplicationEventPublisher` / `@EventListener` |
| Verificação de arquitetura | Manual (revisão de código) | Automatizada na esteira CI/CD com ArchUnit (`RegrasDeArquiteturaTest`) |
| Execução de BDD e CI | Permissão do `mvnw` quebrava no Linux e cenário BDD falhava por colisão de dados | `mvnw` com permissão `+x`, `PostgresTestConfig` estável com Testcontainers no CI e suíte verde em `./mvnw verify` |

---

## 4. Uso de TDD e Cobertura de Testes (Atual x Planejada)

A evolução das novas invariantes do agregado `Requisicao` e do objeto de valor `Validade` seguiu o ciclo **TDD (Vermelho -> Verde -> Refatoração)** registrado no histórico de commits:
1. `test(red)`: escrita de `ValidadeTest` e novos testes de invariante em `RequisicaoTest` antes da implementação (10 falhas esperadas).
2. `feat(green)`: implementação do VO `Validade` e reforço das invariantes em `Requisicao` e `Bolsa` (37 testes unitários passando).
3. `refactor(ddd)`: isolamento dos contextos, eventos de domínio, 10 regras ArchUnit e 3 cenários Gherkin no Cucumber.

### Cobertura Atual (Medida via JaCoCo em `./mvnw verify`)

O plugin **JaCoCo** está configurado no `pom.xml` e gera o relatório de cobertura a cada execução da esteira (`target/site/jacoco/index.html`). Nos testes unitários puros (sem banco e sem Spring):

| Escopo / Pacote | Cobertura Atual (Linhas) | Meta Planejada | Observação |
|-----------------|--------------------------|----------------|------------|
| `shared` (`Validade`, `TipoSanguineo`, enums) | **87,7%** (50/57 linhas) | >= 85% | Meta atingida (`Validade` 88,9%, `TipoSanguineo` 83,3%) |
| `requisicao.negocio.dominio` (`Requisicao`, `Alocacao`) | **67,4%** (87/129 linhas) | >= 80% | Todas as invariantes centrais cobertas; falta cobrir getters/ramos auxiliares |
| `estoque.negocio.dominio` (`Bolsa`, `StatusBolsa`) | **68,0%** (34/50 linhas) | >= 80% | Regras de reserva, liberação e vencimento cobertas |
| **Total Núcleo Core + VOs (TDD puro)** | **72,5%** (171/236 linhas) | >= 80% | 37 testes unitários sem banco e sem Spring |
| Regras de arquitetura (ArchUnit) | **10 regras** (100% passando) | 100% | Bloqueia violações de camadas e acesso cruzado no CI |
| Fluxos de aceitação (Cucumber BDD) | **3 cenários** (100% passando) | >= 3 cenários | Cobre FEFO + evento de domínio, incompatibilidade ABO/Rh e atendimento parcial |
| Contextos de suporte (`expedicao`, `telemetria`, `hospitais`) | Em evolução (cobertos parcialmente no BDD) | >= 60% na camada de domínio | Próxima etapa de expansão de testes unitários |

---

## 5. Uso de SDD (Desenvolvimento Guiado por Especificação)

Adotamos a **Opção 3 (Especificação manual em Markdown)** antes da escrita dos testes e do código de refatoração, versionada em [`specs/requisicao/spec.md`](../../specs/requisicao/spec.md).

A especificação serviu como contrato único entre domínio, TDD e BDD:
- **Linguagem Ubíqua:** listou os termos exatos do glossário (`Hospital`, `Requisição`, `Alocação`, `Bolsa`, `TipoSanguineo`, `Validade`, `FEFO`) que viraram nomes de classes e métodos.
- **Regras e Invariantes:** cada regra numerada na spec originou diretamente um teste unitário no commit `test(red)` do ciclo TDD.
- **Critérios de Aceite:** os critérios CA1, CA2 e CA3 da spec foram traduzidos diretamente para os 3 cenários Gherkin em `src/test/resources/features/h1_alocacao_fefo.feature`.
- **Eventos e Checklist:** orientou a criação dos eventos `RequisicaoAtendida` e `GuiaEntregue` e o sequenciamento de commits até a entrega final de 01/12.
