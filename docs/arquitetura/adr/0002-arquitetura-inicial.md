# ADR 0002: Escolha da Arquitetura Técnica Inicial

**Status:** Aceito  
**Data:** Novembro de 2026  
**Projeto:** Rota Vital · CESAR School · ADS · 2026.2

---

## Contexto

Tendo decidido pelo monolito modular (ADR 0001), precisamos escolher a stack tecnológica, o banco de dados, a estratégia de testes e a plataforma de deploy.

As restrições são:
- A equipe já domina Java 21 + Spring Boot 3.3.5, PostgreSQL, Docker, GitHub Actions e Cucumber (entregues e aprovados na Parte 1 — Mesa DJ).
- Não há justificativa para trocar tecnologias funcionando por outras desconhecidas.
- O sistema precisa de deploy automatizado (CI/CD), testes em múltiplos níveis e banco de dados relacional.
- O orçamento de infraestrutura é zero (plano gratuito do Render).

Alternativas consideradas:
- **Backend:** Python/Django, Node.js/Express, Java/Spring Boot
- **Banco:** MySQL, MongoDB, PostgreSQL, H2
- **Frontend:** React, Vue.js, Thymeleaf, HTML estático
- **Deploy:** Fly.io, Railway, Render, Heroku

---

## Decisão

Mantemos a mesma stack da Parte 1, adaptada para o novo domínio:

| Componente | Escolha |
|-----------|---------|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.3.5 |
| Persistência | Spring Data JPA + Hibernate |
| Banco | PostgreSQL 16 |
| Migrações | Flyway |
| Testes unitários | JUnit 5 + Mockito |
| Testes de integração | Testcontainers |
| Testes BDD | Cucumber (cenários Gherkin) |
| Build | Maven (mvnw wrapper) |
| Container | Docker + Docker Compose |
| CI/CD | GitHub Actions |
| Deploy | Render (plano gratuito) |
| Frontend | HTML + CSS + JavaScript (estático) |

A estrutura de pacotes segue o padrão DDD por Bounded Context: `br.cesar.rotavital.<contexto>.<camada>`.

---

## Consequências

**O que melhora:**
- Curva de aprendizado zero — a equipe já conhece toda a stack.
- Ambiente de desenvolvimento e produção idênticos (mesmo Dockerfile e compose).
- Testes em três níveis já configurados e funcionando.
- Pipeline CI/CD reutilizado e adaptado do Mesa DJ.
- Integração natural entre Spring Boot e DDD (módulos por pacote, interfaces de repositório).

**O que piora / riscos:**
- O frontend HTML estático é funcional, mas limita a riqueza visual do dashboard. Se a equipe quiser gráficos interativos, precisará adicionar uma biblioteca JS (Chart.js) manualmente.
- O Render gratuito hiberna após 15 minutos de inatividade — aceitável para ambiente acadêmico.

**Alternativas descartadas:**
- **React:** Adicionaria uma segunda linguagem (JavaScript/TypeScript) e um segundo pipeline de build, sem benefício proporcional para o escopo da Parte 2.
- **MongoDB:** O domínio do Rota Vital tem relacionamentos fortes (bolsa ↔ alocação ↔ requisição) que se beneficiam de banco relacional. Constraints de banco (unicidade de bolsa reservada) são mais fáceis em PostgreSQL.
- **H2:** Descartado pelos mesmos motivos do Mesa DJ — não captura problemas reais de SQL dialect e constraints.
