import os
import subprocess

docs_dir = r"C:\Users\lucas.luna\Desktop\Github - Projetos\PI2\docs"

html_slides = """<!DOCTYPE html>
<html lang="pt-BR">
<head>
<meta charset="UTF-8">
<style>
  @page {
    size: 297mm 210mm;
    margin: 15mm;
  }
  body {
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Arial, sans-serif;
    color: #0f172a;
    margin: 0;
    padding: 0;
  }
  .slide {
    page-break-after: always;
    height: 175mm;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    justifyContent: flex-start;
    padding: 10px 20px;
  }
  .slide:last-child {
    page-break-after: avoid;
  }
  .slide-header {
    border-bottom: 3px solid #0284c7;
    padding-bottom: 8px;
    margin-bottom: 20px;
    display: flex;
    justify-content: space-between;
    align-items: baseline;
  }
  .slide-header h2 {
    margin: 0;
    font-size: 20pt;
    color: #0f172a;
  }
  .slide-header .tag {
    font-size: 11pt;
    font-weight: 600;
    color: #0284c7;
  }
  .slide-body {
    flex: 1;
    font-size: 13pt;
    line-height: 1.5;
  }
  .capa-slide {
    text-align: center;
    justify-content: center;
    align-items: center;
  }
  .capa-slide h1 {
    font-size: 32pt;
    color: #0f172a;
    margin: 10px 0;
  }
  .capa-slide h3 {
    font-size: 16pt;
    color: #b91c1c;
    font-weight: 500;
    margin-bottom: 30px;
  }
  .capa-slide .meta {
    font-size: 12pt;
    color: #475569;
    line-height: 1.6;
  }
  ul {
    margin-top: 10px;
    padding-left: 25px;
  }
  li {
    margin-bottom: 12px;
  }
  .highlight {
    background-color: #f0f9ff;
    border-left: 4px solid #0284c7;
    padding: 12px 18px;
    border-radius: 4px;
    margin: 15px 0;
  }
  table {
    width: 100%;
    border-collapse: collapse;
    margin-top: 15px;
    font-size: 11pt;
  }
  table th, table td {
    border: 1px solid #cbd5e1;
    padding: 8px 12px;
    text-align: left;
  }
  table th {
    background-color: #f1f5f9;
  }
  .footer-bar {
    margin-top: auto;
    font-size: 9pt;
    color: #94a3b8;
    display: flex;
    justify-content: space-between;
  }
</style>
</head>
<body>

<div class="slide capa-slide">
  <div style="font-size: 45pt;">🩸</div>
  <h1>ROTA VITAL</h1>
  <h3>Sistema de Gestão Logística de Hemocomponentes</h3>
  <div class="meta">
    <strong>Trabalho U2 — Parte 2: Planejamento da Remodelagem Arquitetural</strong><br>
    CESAR School · Infraestrutura de Software (2026.2)<br>
    <strong>Squad:</strong> Gabriel Farias, Gabriel Nogueira, Lucas Mateus, Mirella Emily, Myllena Cristiane<br>
    <strong>Repositório:</strong> github.com/Lucas-Alves123/PI2
  </div>
</div>

<div class="slide">
  <div class="slide-header">
    <h2>1. Onde Estamos & O Problema</h2>
    <span class="tag">1 Minuto · Introdução</span>
  </div>
  <div class="slide-body">
    <ul>
      <li><strong>O Rota Vital:</strong> Plataforma para controle rigoroso de bolsas de sangue, compatibilidade ABO/Rh e rastreamento de entregas refrigeradas.</li>
      <li><strong>Evolução da Parte 1:</strong> A Parte 1 entregou a base de CI/CD, esteira de testes e containerização Docker (Mesa DJ).</li>
      <li><strong>Objetivo da Parte 2:</strong> Remodelar o sistema com <em>Domain-Driven Design (DDD)</em>, organizá-lo em 3 camadas e projetar a infraestrutura em nuvem e microsserviços para sustentar a escala da Parte 3.</li>
    </ul>
    <div class="highlight">
      <strong>Diretriz Central:</strong> Segurança máxima no atendimento hospitalar com zero tolerância a bolsas incompatíveis ou vencidas.
    </div>
  </div>
  <div class="footer-bar"><span>Rota Vital · Apresentação U2</span><span>Slide 2</span></div>
</div>

<div class="slide">
  <div class="slide-header">
    <h2>2. Remodelagem com DDD — Subdomínios e Contextos</h2>
    <span class="tag">3 Minutos · Seção A</span>
  </div>
  <div class="slide-body">
    <ul>
      <li><strong>Core Domain (Diferencial):</strong> <em>Requisição & Alocação</em> (regras clínicas), <em>Estoque</em> (rastreabilidade física) e <em>Expedição</em> (cadeia fria).</li>
      <li><strong>Suporte:</strong> <em>Hospitais</em> (cadastro institucional) e <em>Telemetria</em> (sensores térmicos e GPS).</li>
      <li><strong>Genérico:</strong> <em>Identidade & Acesso</em> (Spring Security) e <em>Notificação</em> (alertas).</li>
      <li><strong>Linguagem Ubíqua — O Termo "Bolsa":</strong>
        <ul>
          <li><em>No Estoque:</em> Unidade física, com código único, validade e tipo sanguíneo.</li>
          <li><em>Na Expedição:</em> Volume de carga unitizado, vinculado a uma guia de transporte e termômetro.</li>
        </ul>
      </li>
    </ul>
  </div>
  <div class="footer-bar"><span>Rota Vital · Apresentação U2</span><span>Slide 3</span></div>
</div>

<div class="slide">
  <div class="slide-header">
    <h2>3. Modelo Tático do Agregado & BDD</h2>
    <span class="tag">Seção B · Regras de Negócio</span>
  </div>
  <div class="slide-body">
    <ul>
      <li><strong>Agregado Principal:</strong> Raiz <code>Requisicao</code> gerenciando entidades internas <code>Alocacao</code> e o Value Object <code>TipoSanguineo</code>.</li>
      <li><strong>4 Invariantes Fundamentais:</strong>
        <ol>
          <li>Apenas bolsas compatíveis ABO/Rh são selecionadas;</li>
          <li><strong>Regra FEFO estrita:</strong> Prioridade à bolsa com validade mais próxima;</li>
          <li><strong>Reserva Exclusiva:</strong> Impossibilidade de duplicidade em requisições ativas (protegido por constraint única);</li>
          <li>Status só avança para <em>Atendida</em> após 100% dos hemocomponentes alocados.</li>
        </ol>
      </li>
      <li><strong>Cenário BDD (Cucumber):</strong> Teste automatizado validando a seleção prioritária da bolsa com vencimento mais curto.</li>
    </ul>
  </div>
  <div class="footer-bar"><span>Rota Vital · Apresentação U2</span><span>Slide 4</span></div>
</div>

<div class="slide">
  <div class="slide-header">
    <h2>4. Arquitetura em Três Camadas</h2>
    <span class="tag">3 Minutos · Seção C</span>
  </div>
  <div class="slide-body">
    <ul>
      <li><strong>Apresentação:</strong> Controllers Spring MVC (web) e templates Thymeleaf.</li>
      <li><strong>Negócio:</strong>
        <ul>
          <li><em>Aplicação:</em> Casos de uso transacionais (<code>AlocacaoFefoService</code>).</li>
          <li><em>Domínio Puro:</em> Entidades e Value Objects isolados de frameworks externos.</li>
        </ul>
      </li>
      <li><strong>Dados:</strong> Repositórios Spring Data JPA e versionamento de banco via Flyway.</li>
      <li><strong>Lógico vs. Físico:</strong>
        <ul>
          <li><em>3 Camadas Lógicas:</em> Executadas no mesmo processo JVM (Monólito Modular).</li>
          <li><em>2 Tiers Físicos:</em> Container da Aplicação (Tier 1) conectado via rede ao PostgreSQL 16 (Tier 2).</li>
        </ul>
      </li>
    </ul>
  </div>
  <div class="footer-bar"><span>Rota Vital · Apresentação U2</span><span>Slide 5</span></div>
</div>

<div class="slide">
  <div class="slide-header">
    <h2>5. Projeção para Microsserviços e ADRs</h2>
    <span class="tag">Seção D · Sistemas Distribuídos</span>
  </div>
  <div class="slide-body">
    <ul>
      <li><strong>Por que Monólito Modular primeiro?</strong> Reduz custo e complexidade operacional na fase acadêmica (ADR 0001 e ADR 0002).</li>
      <li><strong>Candidato a Microsserviço:</strong> <em>Telemetria</em> — alta frequência de escrita (leituras GPS/temperatura a cada minuto).</li>
      <li><strong>Padrão Saga Coreografada com Compensação:</strong>
        <ul>
          <li><em>Fluxo Normal:</em> Requisição Atendida ➔ Expedição emite Guia ➔ Telemetria monitora.</li>
          <li><em>Compensação (Falha de Veículo):</em> Falta de veículo refrigerado dispara <code>ExpedicaoCancelada</code> ➔ Estoque libera imediatamente as bolsas reservadas.</li>
        </ul>
      </li>
    </ul>
  </div>
  <div class="footer-bar"><span>Rota Vital · Apresentação U2</span><span>Slide 6</span></div>
</div>

<div class="slide">
  <div class="slide-header">
    <h2>6. Cenários de Carga — Aplicação da Lei de Little</h2>
    <span class="tag">3 Minutos · Seção E</span>
  </div>
  <div class="slide-body">
    <p>Fórmula: <strong>L = &lambda; &times; W</strong> (Tempo médio assumido: W = 0,20 s)</p>
    <table>
      <thead>
        <tr>
          <th>Cenário</th>
          <th>Usuários</th>
          <th>Pedidos/Dia</th>
          <th>Pico (&lambda;)</th>
          <th>Concorrência (L)</th>
          <th>Instâncias Recomendadas</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td><strong>1 — Baixo (Regional)</strong></td>
          <td>10</td>
          <td>50</td>
          <td>2 req/s</td>
          <td>0,4 req simultâneas</td>
          <td>1x App (512 MB) · 5 conexões BD</td>
        </tr>
        <tr>
          <td><strong>2 — Moderado (Estadual)</strong></td>
          <td>100</td>
          <td>500</td>
          <td>15 req/s</td>
          <td>3,0 req simultâneas</td>
          <td>2x App (1 GB) + ALB · 20 conexões BD</td>
        </tr>
        <tr>
          <td><strong>3 — Alto (Nacional)</strong></td>
          <td>500</td>
          <td>5.000</td>
          <td>95 req/s</td>
          <td>19,0 req simultâneas</td>
          <td>4x Core + 2x Telemetria + ALB · 90 conexões</td>
        </tr>
      </tbody>
    </table>
  </div>
  <div class="footer-bar"><span>Rota Vital · Apresentação U2</span><span>Slide 7</span></div>
</div>

<div class="slide">
  <div class="slide-header">
    <h2>7. Orçamento de Nuvem (AWS On-Demand / N. Virginia)</h2>
    <span class="tag">Seção E · Custos Reais</span>
  </div>
  <div class="slide-body">
    <table>
      <thead>
        <tr>
          <th>Cenário</th>
          <th>Pedidos/Mês</th>
          <th>Recursos AWS Principais</th>
          <th>Custo Mensal (USD)</th>
          <th>Custo / Pedido</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td><strong>1 — Baixo</strong></td>
          <td>1.500</td>
          <td>ECS Fargate (0.25 vCPU) + RDS PostgreSQL db.t3.micro</td>
          <td><strong>$24.57</strong></td>
          <td>$0.0164</td>
        </tr>
        <tr>
          <td><strong>2 — Moderado</strong></td>
          <td>15.000</td>
          <td>2x ECS Fargate (0.5 vCPU) + ALB + RDS db.t3.small</td>
          <td><strong>$92.18</strong></td>
          <td>$0.0061</td>
        </tr>
        <tr>
          <td><strong>3 — Alto</strong></td>
          <td>150.000</td>
          <td>6x ECS Fargate + ALB + RDS db.t3.medium (Multi-AZ + Replica)</td>
          <td><strong>$417.14</strong></td>
          <td>$0.0027</td>
        </tr>
      </tbody>
    </table>
    <div class="highlight" style="margin-top: 20px;">
      <strong>Economia de Escala:</strong> Redução de <strong>83%</strong> no custo por pedido ($0.016 ➔ $0.002), viabilizando a sustentabilidade financeira da solução em nível nacional.
    </div>
  </div>
  <div class="footer-bar"><span>Rota Vital · Apresentação U2</span><span>Slide 8</span></div>
</div>

<div class="slide">
  <div class="slide-header">
    <h2>8. Conclusão & Status da Entrega</h2>
    <span class="tag">Fechamento</span>
  </div>
  <div class="slide-body">
    <ul>
      <li><strong>Repositório Ativo:</strong> Código e artefatos versionados em <code>github.com/Lucas-Alves123/PI2</code>.</li>
      <li><strong>Qualidade & Testes:</strong> 25 testes unitários e de integração passando com 100% de sucesso (JUnit 5 + AssertJ).</li>
      <li><strong>Pronto para a Parte 3:</strong> Base arquitetural consolidada para conectar o deploy contínuo e telemetria em tempo real.</li>
    </ul>
    <div style="text-align: center; margin-top: 40px;">
      <h3 style="color: #0284c7; font-size: 18pt;">Obrigado! Estamos abertos a perguntas da banca.</h3>
    </div>
  </div>
  <div class="footer-bar"><span>Rota Vital · Apresentação U2</span><span>Slide 9</span></div>
</div>

</body>
</html>
"""

html_slides_path = os.path.join(docs_dir, "apresentacao_slides.html")
pdf_slides_path = os.path.join(docs_dir, "apresentacao_slides.pdf")

with open(html_slides_path, "w", encoding="utf-8") as f:
    f.write(html_slides)

edge_path = r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
cmd = [
    edge_path,
    "--headless",
    "--disable-gpu",
    "--no-pdf-header-footer",
    f"--print-to-pdf={pdf_slides_path}",
    html_slides_path
]
subprocess.run(cmd, check=True)
print("Slides em PDF gerados com sucesso em:", pdf_slides_path)
