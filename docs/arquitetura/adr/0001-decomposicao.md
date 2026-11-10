# ADR 0001: Decomposição do Rota Vital

**Status:** Proposto  
**Data:** Novembro de 2026  
**Projeto:** Rota Vital · CESAR School · ADS · 2026.2

---

## Contexto

O Rota Vital gerencia operações logísticas de hemocomponentes, cobrindo desde o controle de estoque até a expedição com cadeia fria e telemetria de temperatura.

Precisamos decidir como decompor o sistema em unidades de software — seja como módulos dentro de um único processo ou como serviços independentes.

As premissas do contexto são:
- Equipe pequena (5 estudantes), sem experiência operacional com microsserviços em produção.
- Prazo acadêmico curto (Partes 2 e 3 do PI).
- O sistema precisa ser compreensível, testável e implantável com facilidade.
- O volume de requisições é pequeno a moderado — não há necessidade imediata de escala independente por serviço.
- A telemetria tem potencial de crescimento de volume muito maior que os outros contextos (leituras contínuas de GPS + temperatura).

Consideramos duas alternativas:
1. **Monolito modular**: um único processo com módulos bem definidos por Bounded Context.
2. **Microsserviços**: cada Bounded Context como um serviço independente com banco próprio.

---

## Decisão

Adotamos o **monolito modular** como arquitetura inicial, com separação clara de módulos por Bounded Context (hospitais, estoque, requisição, expedição, telemetria, notificação, identidade).

Os módulos seguem a mesma separação de responsabilidades que teriam como microsserviços — domain, application, infrastructure — mas rodam no mesmo processo.

---

## Consequências

**O que melhora:**
- Implantação simples: um único `docker compose up` sobe tudo.
- Sem overhead de rede entre serviços (chamadas locais são mais rápidas e confiáveis).
- Consistência transacional garantida pelo banco único — sem necessidade de Sagas distribuídas.
- Mais fácil de depurar, testar e entender para uma equipe acadêmica.
- A separação por módulos mantém o caminho aberto para extração futura como microsserviço.

**O que piora / riscos:**
- Escalar apenas um módulo (ex.: Telemetria) exige escalar toda a aplicação.
- Um bug crítico em qualquer módulo pode afetar toda a aplicação.
- À medida que o sistema cresce, o risco de acoplamento entre módulos aumenta.

**Alternativa descartada:**
Microsserviços foram descartados nesta fase porque adicionariam complexidade operacional (service discovery, comunicação assíncrona, consistência distribuída) desproporcional ao tamanho e ao prazo do projeto acadêmico. A projeção para microsserviços está documentada separadamente e pode ser materializada na Parte 3 ou em versões futuras.
