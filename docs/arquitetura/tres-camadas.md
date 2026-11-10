# Arquitetura em Três Camadas — Rota Vital

**Projeto:** Rota Vital  
**Disciplina:** Infraestrutura de Software · CESAR School · ADS · 2026.2  
**Trabalho:** U2 · Parte 2 · Seção C — Arquitetura em Três Camadas

---

## Visão Geral

O Rota Vital é organizado como um **monolito modular** com arquitetura em três camadas lógicas. Essa separação garante que cada parte do sistema tenha uma responsabilidade clara e que as regras de negócio fiquem isoladas de detalhes de infraestrutura.

```
┌────────────────────────────────────────────────────┐
│                 APRESENTAÇÃO                        │
│  Controllers REST · Formulários · Validação de     │
│  entrada · Respostas HTTP (JSON/HTML)               │
├────────────────────────────────────────────────────┤
│                   NEGÓCIO                           │
│  Casos de uso · Serviços de aplicação ·            │
│  Entidades · Agregados · Value Objects ·           │
│  Regras de negócio · Eventos de domínio            │
├────────────────────────────────────────────────────┤
│                    DADOS                            │
│  Repositórios JPA · Migrações Flyway ·             │
│  Entidades de banco · Integrações externas         │
└────────────────────────────────────────────────────┘
```

---

## Camada 1: Apresentação

**Responsabilidade:** Receber requisições HTTP, validar os dados de entrada, chamar a camada de negócio e devolver respostas no formato adequado (JSON para APIs, HTML para interfaces web).

**O que fica aqui:**
- Controllers REST (`@RestController`, `@Controller`)
- DTOs de entrada e saída (Request / Response)
- Validação de campos (`@Valid`, `@NotNull`, `@NotBlank`)
- Tratamento de erros HTTP (`@ControllerAdvice`)
- Arquivos estáticos (HTML, CSS, JavaScript)

**O que NÃO fica aqui:**
- Regras de negócio
- Acesso direto ao banco de dados
- Lógica de compatibilidade ABO/Rh ou FEFO

**Exemplo no Rota Vital:**
```
RequisicaoController  → recebe POST /api/requisicoes
                       → valida os campos obrigatórios
                       → chama RegistrarRequisicaoUseCase
                       → retorna 201 Created com o id da requisição
```

---

## Camada 2: Negócio

**Responsabilidade:** Conter toda a lógica de negócio do sistema. É o coração do Rota Vital.

A camada de negócio é dividida em duas subcamadas:

### 2.1. Aplicação (Casos de Uso)

Orquestra o domínio para executar uma operação de negócio completa. Um caso de uso não tem regras de negócio — ele coordena entidades, repositórios e eventos.

**O que fica aqui:**
- Casos de uso / serviços de aplicação (`RegistrarRequisicao`, `AlocarBolsas`, `ConsultarEstoque`)
- Orquestração de chamadas entre entidades e repositórios
- Publicação de eventos de domínio
- Controle de transações (`@Transactional`)

**Exemplo:**
```
RegistrarRequisicaoUseCase:
  1. Busca hospital pelo hospitalId
  2. Cria o agregado Requisicao
  3. Chama AlocarBolsasUseCase (compatibilidade + FEFO)
  4. Salva via RequisicaoRepository
  5. Publica evento RequisicaoRegistrada
  6. Retorna RequisicaoResponse
```

### 2.2. Domínio (Entidades, Agregados, Value Objects)

Contém o modelo de domínio puro. As entidades de domínio têm comportamento — não são apenas estruturas de dados (anemic domain model é um antipadrão aqui).

**O que fica aqui:**
- Entidades (`Requisicao`, `Alocacao`, `Bolsa`, `Hospital`)
- Agregados (raiz: `Requisicao`)
- Value Objects (`TipoSanguineo`, `CNPJ`, `Validade`)
- Enumerações de domínio (`StatusRequisicao`, `Componente`, `Urgencia`)
- Exceções de negócio (`RequisicaoNaoEncontradaException`, `BolsaIncompativelException`)
- Interfaces de repositório (apenas a interface — a implementação fica na camada de Dados)
- Eventos de domínio (`RequisicaoRegistrada`, `BolsaReservada`)

**Regra fundamental:** O domínio **não deve depender** de controllers, banco de dados ou frameworks web. Uma entidade de domínio é Java puro.

---

## Camada 3: Dados

**Responsabilidade:** Persistir e recuperar dados. Implementa as interfaces de repositório definidas no domínio.

**O que fica aqui:**
- Implementações de repositório JPA (`RequisicaoRepositoryJpa`, `BolsaRepositoryJpa`)
- Entidades JPA (podem ser as mesmas do domínio ou classes separadas com `@Entity`)
- Migrações de banco de dados (Flyway — `V1__cria_hospitais.sql`, `V2__cria_bolsas.sql`, etc.)
- Configuração de conexão com banco (`application.yml`)
- Integrações externas (APIs de terceiros, mensageria)

**O que NÃO fica aqui:**
- Regras de negócio
- Lógica de compatibilidade
- Código de apresentação

---

## Regra de Dependência

```
Apresentação  →  Negócio (Aplicação)  →  Negócio (Domínio)
     ↓                    ↓
   Dados              Dados (implementa interfaces do Domínio)
```

**Em palavras:**
- A **Apresentação** usa a camada de **Negócio** (casos de uso) — nunca acessa o banco diretamente.
- A camada de **Negócio (Aplicação)** coordena o **Domínio** e usa interfaces de repositório.
- O **Domínio** não depende de nada externo — nem de Spring, nem de JPA, nem de HTTP.
- A camada de **Dados** implementa as interfaces definidas no Domínio e conhece o Spring Data JPA.

Essa regra garante que as regras de negócio possam ser testadas **sem banco de dados e sem servidor web** — exatamente como fizemos nos testes unitários do Mesa DJ.

---

## Estrutura de Pacotes — Por Bounded Context e Camada

```
br.cesar.rotavital/
│
├── hospitais/
│   ├── apresentacao/
│   │   └── HospitalController.java
│   ├── negocio/
│   │   ├── aplicacao/
│   │   │   ├── CadastrarHospital.java        (caso de uso)
│   │   │   └── ConsultarHospital.java
│   │   └── dominio/
│   │       ├── Hospital.java                 (entidade)
│   │       ├── Unidade.java                  (entidade)
│   │       ├── CNPJ.java                     (value object)
│   │       └── HospitalRepository.java       (interface)
│   └── dados/
│       └── HospitalRepositoryJpa.java        (implementação)
│
├── estoque/
│   ├── apresentacao/
│   │   └── BolsaController.java
│   ├── negocio/
│   │   ├── aplicacao/
│   │   │   ├── RegistrarBolsa.java
│   │   │   ├── ConsultarEstoque.java
│   │   │   └── DescartarBolsa.java
│   │   └── dominio/
│   │       ├── Bolsa.java                    (entidade — raiz do agregado)
│   │       ├── TipoSanguineo.java            (value object)
│   │       ├── Validade.java                 (value object)
│   │       ├── StatusBolsa.java              (enum)
│   │       └── BolsaRepository.java          (interface)
│   └── dados/
│       └── BolsaRepositoryJpa.java
│
├── requisicao/
│   ├── apresentacao/
│   │   └── RequisicaoController.java
│   ├── negocio/
│   │   ├── aplicacao/
│   │   │   ├── RegistrarRequisicao.java      (caso de uso âncora)
│   │   │   ├── AlocarBolsas.java             (aplica FEFO + compatibilidade)
│   │   │   └── ConcluirRequisicao.java
│   │   └── dominio/
│   │       ├── Requisicao.java               (raiz do agregado)
│   │       ├── Alocacao.java                 (entidade interna)
│   │       ├── TipoSanguineo.java            (value object — cópia local)
│   │       ├── MatrizCompatibilidade.java    (regra configurável)
│   │       ├── StatusRequisicao.java         (enum)
│   │       └── RequisicaoRepository.java     (interface)
│   └── dados/
│       └── RequisicaoRepositoryJpa.java
│
├── expedicao/
│   ├── apresentacao/
│   │   └── ExpedicaoController.java
│   ├── negocio/
│   │   ├── aplicacao/
│   │   │   └── EmitirGuia.java
│   │   └── dominio/
│   │       ├── GuiaExpedicao.java
│   │       ├── BolsaTransportada.java        (VO — Bolsa no contexto Expedição)
│   │       └── GuiaRepository.java
│   └── dados/
│       └── GuiaRepositoryJpa.java
│
├── telemetria/
│   ├── apresentacao/
│   │   └── TelemetriaController.java
│   ├── negocio/
│   │   ├── aplicacao/
│   │   │   └── RegistrarLeitura.java
│   │   └── dominio/
│   │       ├── LeituraTemperatura.java
│   │       └── LeituraRepository.java
│   └── dados/
│       └── LeituraRepositoryJpa.java
│
├── notificacao/
│   └── dominio/
│       ├── Notificacao.java
│       └── NotificacaoService.java
│
└── identidade/
    ├── apresentacao/
    │   └── AuthController.java
    ├── negocio/
    │   ├── aplicacao/
    │   │   └── Autenticar.java
    │   └── dominio/
    │       ├── Usuario.java
    │       ├── Perfil.java
    │       └── UsuarioRepository.java
    └── dados/
        └── UsuarioRepositoryJpa.java
```

---

## Diferença entre Camadas Lógicas e Tiers Físicos

### Camadas lógicas (layers)
São divisões **conceituais** dentro do mesmo processo. No Rota Vital (monolito modular), as três camadas — apresentação, negócio e dados — rodam dentro do **mesmo processo Java** (JVM), no **mesmo container Docker**.

A separação é de responsabilidade e dependência, não de deploy.

### Tiers físicos (tiers)
São divisões **físicas** de infraestrutura — máquinas, processos ou serviços diferentes.

No Rota Vital, temos **dois tiers físicos**:

| Tier | O que roda | Onde |
|------|-----------|------|
| **Tier 1** | Aplicação Java (Spring Boot) — apresentação + negócio + dados | Container `api` |
| **Tier 2** | Banco de dados PostgreSQL | Container `db` |

```
[ Browser / Cliente REST ]
          ↓ HTTP
  ┌───────────────────────────────────────────┐
  │   Tier 1: Container api (JVM / Spring)    │
  │  ┌─────────────┐                          │
  │  │ Apresentação│                          │
  │  ├─────────────┤   (camadas lógicas)      │
  │  │   Negócio   │   dentro do mesmo        │
  │  ├─────────────┤   processo               │
  │  │    Dados    │                          │
  │  └─────────────┘                          │
  └───────────────────────────────────────────┘
          ↓ JDBC / TCP 5432
  ┌───────────────────────────────────────────┐
  │   Tier 2: Container db (PostgreSQL 16)    │
  └───────────────────────────────────────────┘
```

**Resumo prático:**  
- 3 camadas lógicas → separação de responsabilidade (dentro do mesmo processo)  
- 2 tiers físicos → app + banco (processos/máquinas distintos)  
- Na projeção de microsserviços, cada serviço teria seu próprio tier físico

---

## Referências

- EVANS, E. *Domain-Driven Design*. Addison-Wesley, 2003.
- FOWLER, M. *Patterns of Enterprise Application Architecture*. Addison-Wesley, 2002.
- Slides das aulas 006 e 007 — Infraestrutura de Software · CESAR School · 2026.2
