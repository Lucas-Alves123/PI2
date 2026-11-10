import os
import markdown
import subprocess

docs_dir = r"C:\Users\lucas.luna\Desktop\Github - Projetos\PI2\docs\arquitetura"
output_dir = r"C:\Users\lucas.luna\Desktop\Github - Projetos\PI2\docs"

order = [
    ("dominio.md", "Seção A — Remodelagem do Domínio com DDD"),
    ("glossario.md", "Seção A — Glossário da Linguagem Ubíqua"),
    ("mapa-contextos.md", "Seção A — Mapa de Contextos"),
    ("modelo-tatico.md", "Seção B — Modelo Tático do Agregado Principal"),
    ("cenarios-bdd.md", "Seção B — Cenários de Aceite em Gherkin"),
    ("tres-camadas.md", "Seção C — Arquitetura em Três Camadas"),
    ("microsservicos.md", "Seção D — Projeção para Microsserviços e Saga"),
    (os.path.join("adr", "0001-decomposicao.md"), "Seção D — ADR 0001: Decomposição"),
    (os.path.join("adr", "0002-arquitetura-inicial.md"), "Seção D — ADR 0002: Arquitetura Inicial"),
    ("cenarios-carga.md", "Seção E — Cenários de Carga e Lei de Little"),
    ("orcamento.md", "Seção E — Orçamento e Custos em Nuvem"),
]

combined_html = """<!DOCTYPE html>
<html lang="pt-BR">
<head>
<meta charset="UTF-8">
<title>Rota Vital — Planejamento da Remodelagem</title>
<style>
  @page {
    size: A4;
    margin: 20mm 15mm 20mm 15mm;
    @bottom-right {
      content: counter(page);
    }
  }
  body {
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
    color: #1e293b;
    line-height: 1.45;
    font-size: 10.5pt;
  }
  .capa {
    page-break-after: always;
    text-align: center;
    padding-top: 60px;
  }
  .capa h1 {
    font-size: 26pt;
    color: #0f172a;
    margin-bottom: 5px;
  }
  .capa h2 {
    font-size: 14pt;
    color: #b91c1c;
    font-weight: 600;
    margin-bottom: 30px;
  }
  .capa .meta {
    font-size: 11pt;
    color: #475569;
    margin-bottom: 40px;
    line-height: 1.6;
  }
  .capa .squad {
    margin: 40px auto;
    width: 75%;
    border-collapse: collapse;
    font-size: 10pt;
  }
  .capa .squad th, .capa .squad td {
    border: 1px solid #cbd5e1;
    padding: 8px 12px;
    text-align: left;
  }
  .capa .squad th {
    background-color: #f1f5f9;
  }
  .section-break {
    page-break-before: always;
  }
  h1 { font-size: 16pt; color: #0f172a; border-bottom: 2px solid #0284c7; padding-bottom: 4px; margin-top: 20px; }
  h2 { font-size: 13pt; color: #1e293b; margin-top: 16px; }
  h3 { font-size: 11pt; color: #334155; margin-top: 12px; }
  table {
    width: 100%;
    border-collapse: collapse;
    margin: 12px 0;
    font-size: 9pt;
  }
  table th, table td {
    border: 1px solid #cbd5e1;
    padding: 6px 8px;
    text-align: left;
  }
  table th {
    background-color: #f8fafc;
    font-weight: 600;
  }
  code {
    background-color: #f1f5f9;
    padding: 2px 4px;
    border-radius: 3px;
    font-family: Consolas, Monaco, monospace;
    font-size: 9pt;
  }
  pre {
    background-color: #0f172a;
    color: #f8fafc;
    padding: 10px;
    border-radius: 5px;
    overflow-x: auto;
    font-size: 8.5pt;
    line-height: 1.3;
  }
  pre code {
    background: none;
    color: inherit;
    padding: 0;
  }
  blockquote {
    border-left: 3px solid #0284c7;
    background-color: #f0f9ff;
    padding: 8px 12px;
    margin: 10px 0;
    font-size: 9.5pt;
  }
</style>
</head>
<body>

<div class="capa">
  <div style="font-size: 40pt; margin-bottom: 10px;">🩸</div>
  <h1>PROJETO ROTA VITAL</h1>
  <h2>Sistema de Gestão Logística de Hemocomponentes</h2>
  <div class="meta">
    <strong>CESAR School · Análise e Desenvolvimento de Sistemas</strong><br>
    Disciplina: Infraestrutura de Software (2026.2)<br>
    Trabalho U2 — Parte 2: Planejamento da Remodelagem Arquitetural<br>
    Repositório: <a href="https://github.com/Lucas-Alves123/PI2">github.com/Lucas-Alves123/PI2</a>
  </div>

  <table class="squad">
    <thead>
      <tr>
        <th>Integrante</th>
        <th>Papel no Squad</th>
      </tr>
    </thead>
    <tbody>
      <tr><td>Gabriel Farias</td><td>Engenharia de Software</td></tr>
      <tr><td>Gabriel Nogueira</td><td>Infraestrutura e Banco de Dados</td></tr>
      <tr><td>Lucas Mateus (@Lucas-Alves123)</td><td>Arquiteto de Software & Full-Stack</td></tr>
      <tr><td>Mirella Emily</td><td>Qualidade de Software & BDD</td></tr>
      <tr><td>Myllena Cristiane</td><td>Análise de Requisitos & Domínio</td></tr>
    </tbody>
  </table>
</div>
"""

for fname, title in order:
    fpath = os.path.join(docs_dir, fname)
    if os.path.exists(fpath):
        with open(fpath, "r", encoding="utf-8") as f:
            raw = f.read()
        body_html = markdown.markdown(raw, extensions=['tables', 'fenced_code'])
        combined_html += f'<div class="section-break">\n{body_html}\n</div>\n'

combined_html += "</body>\n</html>"

html_file = os.path.join(output_dir, "documento_completo.html")
pdf_file = os.path.join(output_dir, "documento_final_u2.pdf")

with open(html_file, "w", encoding="utf-8") as f:
    f.write(combined_html)

print("HTML compilado com sucesso:", html_file)

# Executa o Edge headless para imprimir diretamente para PDF
edge_path = r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
cmd = [
    edge_path,
    "--headless",
    "--disable-gpu",
    "--no-pdf-header-footer",
    f"--print-to-pdf={pdf_file}",
    html_file
]

res = subprocess.run(cmd, capture_output=True, text=True)
print("Execução Edge PDF:", res.returncode)
if os.path.exists(pdf_file):
    print("PDF GERADO COM SUCESSO:", pdf_file, "Tamanho:", os.path.getsize(pdf_file), "bytes")
else:
    print("Erro ao gerar PDF:", res.stderr)

