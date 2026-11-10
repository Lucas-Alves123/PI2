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

O Rota Vital organiza a logística de hemocomponentes desde o controle de estoque até a entrega hospitalar. O sistema recebe solicitações, verifica compatibilidade ABO/Rh, aplica a regra FEFO (First Expired, First Out) para selecionar as bolsas que vencem primeiro e acompanha toda a cadeia de expedição — incluindo monitoramento de temperatura (cadeia fria).

**Todos os dados são sintéticos e fictícios.**

---

## Funcionalidades

- **Dashboard** — indicadores em tempo real de estoque, solicitações e alertas
- **Hospitais** — cadastro e consulta de hospitais parceiros
- **Estoque** — controle de bolsas de sangue, validade, localização e status
- **Solicitações** — criação e acompanhamento de requisições hospitalares
- **Alocação FEFO** — seleção automática de bolsas compatíveis pelo vencimento mais próximo
- **Expedição** — emissão de guias, controle de transporte e confirmação de entrega
- **Telemetria** — registro e alerta de temperatura durante o transporte
- **Autenticação** — controle de acesso por perfil (Administrador, Estoque, Atendimento, Expedição)

---

## Tecnologias

| Componente | Escolha |
|-----------|---------|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.3.5 |
| Frontend | Thymeleaf + HTML/CSS |
| Persistência | Spring Data JPA + Hibernate |
| Banco de dados | PostgreSQL 16 |
| Migrações | Flyway |
| Segurança | Spring Security (BCrypt) |
| Testes unitários | JUnit 5 + Mockito |
| Testes de integração | Testcontainers |
| Testes BDD | Cucumber (Gherkin em pt-BR) |
| Build | Maven |
| Container | Docker + Docker Compose |
| CI/CD | GitHub Actions |
| Deploy | Render |

---

## Como instalar e executar

### Pré-requisitos

- Docker e Docker Compose instalados
- Git

### Do zero em um único comando

```bash
git clone https://github.com/<seu-usuario>/rotavital.git
cd rotavital
cp .env.example .env        # edite a senha se quiser
docker compose up -d --build
```

Após o build, acesse:

- **Interface web:** http://localhost:8080
- **Login padrão:** `admin` / `admin123`
- **Health:** http://localhost:8080/actuator/health

### Parar

```bash
docker compose down          # para sem apagar dados
docker compose down -v       # para e apaga o volume do banco
```

---

## Como executar os testes

```bash
# Unitários (rápido, sem Docker)
./mvnw test

# Suite completa: unitários + integração + BDD (precisa do Docker para Testcontainers)
./mvnw verify
```

Os testes de integração e BDD sobem um PostgreSQL descartável via **Testcontainers**. Não é necessário ter o banco instalado separadamente.

---

## Estrutura do Projeto

```
PI2/
├── docs/
│   └── arquitetura/
│       ├── dominio.md
│       ├── glossario.md
│       ├── mapa-contextos.md
│       ├── modelo-tatico.md
│       ├── cenarios-bdd.md
│       ├── tres-camadas.md
│       ├── microsservicos.md
│       ├── cenarios-carga.md
│       └── adr/
│           ├── 0001-decomposicao.md
│           └── 0002-arquitetura-inicial.md
├── src/
│   ├── main/
│   │   ├── java/br/cesar/rotavital/
│   │   │   ├── hospitais/        (Bounded Context)
│   │   │   ├── estoque/          (Bounded Context)
│   │   │   ├── requisicao/       (Bounded Context principal)
│   │   │   ├── expedicao/        (Bounded Context)
│   │   │   ├── telemetria/       (Bounded Context)
│   │   │   ├── notificacao/      (Bounded Context)
│   │   │   ├── identidade/       (Bounded Context)
│   │   │   └── apresentacao/     (Controllers web)
│   │   └── resources/
│   │       ├── templates/        (HTML Thymeleaf)
│   │       ├── static/css/
│   │       └── db/migration/     (Flyway SQL)
│   └── test/
│       ├── features/             (Gherkin BDD)
│       └── java/.../
│           ├── BolsaTest.java
│           ├── RequisicaoTest.java
│           ├── CompatibilidadeTest.java
│           └── aceitacao/AlocacaoSteps.java
├── Dockerfile
├── compose.yaml
└── pom.xml
```

---

## Perfis de Usuário

| Login | Senha | Perfil |
|-------|-------|--------|
| `admin` | `admin123` | Administrador |
| `estoque1` | `admin123` | Estoque |
| `atend1` | `admin123` | Atendimento |
| `exped1` | `admin123` | Expedição |

---

## Uso de Inteligência Artificial

Utilizamos o **Antigravity** (assistente de programação com IA da Google DeepMind) durante o desenvolvimento desta entrega.

| Etapa | O que a IA gerou | O que revisamos / validamos |
|-------|-----------------|----------------------------|
| Modelagem DDD | Subdomínios, BCs, glossário, mapa de contextos | Verificamos com o material da aula e o enunciado |
| Modelo tático | Agregado Requisição, invariantes, Gherkin | Validamos as regras de negócio e a matriz de compatibilidade |
| Arquitetura 3 camadas | Árvore de pacotes, regra de dependência, lógico vs. físico | Confrontamos com exemplos dos slides |
| Código de domínio | Entidades, VOs, enums, regras nas entidades | Testamos as invariantes nos testes unitários |
| Testes | BolsaTest, RequisicaoTest, CompatibilidadeTest, Steps BDD | Revisamos casos de borda e regras de compatibilidade |
| Banco de dados | Schema SQL com constraints e dados sintéticos | Verificamos integridade referencial e constraints |
| Frontend HTML | Templates Thymeleaf + CSS | Testamos navegação e formulários manualmente |
| Documentação | Todos os .md em docs/arquitetura/ | Revisamos e ajustamos com base nas aulas |

> **Nota:** Preços de nuvem na seção de orçamento foram indicados como pendentes de validação — a IA não inventou valores; a equipe deve consultar os calculadores oficiais e preencher com data de consulta.
