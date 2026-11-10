import openpyxl
from openpyxl.styles import Font, PatternFill, Alignment, Border, Side
from openpyxl.utils import get_column_letter

wb = openpyxl.Workbook()

# Estilos
header_fill = PatternFill(start_color="1E293B", end_color="1E293B", fill_type="solid")
header_font = Font(name="Calibri", size=11, bold=True, color="FFFFFF")
sub_header_fill = PatternFill(start_color="E2E8F0", end_color="E2E8F0", fill_type="solid")
sub_header_font = Font(name="Calibri", size=11, bold=True, color="0F172A")
title_font = Font(name="Calibri", size=14, bold=True, color="0F172A")
bold_font = Font(name="Calibri", size=11, bold=True)
regular_font = Font(name="Calibri", size=11)
currency_format = '"$"#,##0.00'

thin_border = Border(
    left=Side(style='thin', color='CBD5E1'),
    right=Side(style='thin', color='CBD5E1'),
    top=Side(style='thin', color='CBD5E1'),
    bottom=Side(style='thin', color='CBD5E1')
)

# ----------------- ABA 1: RESUMO CONSOLIDADO -----------------
ws1 = wb.active
ws1.title = "Resumo e Comparativo"

ws1["A1"] = "PROJETO ROTA VITAL — ORÇAMENTO DE INFRAESTRUTURA EM NUVEM"
ws1["A1"].font = title_font
ws1["A2"] = "Fonte: AWS Pricing Calculator (us-east-1) e Render Pricing | Data da Consulta: 10/11/2026"
ws1["A2"].font = Font(name="Calibri", size=10, italic=True, color="475569")

headers_resumo = ["Cenário de Carga", "Usuários", "Pedidos/Dia", "Pedidos/Mês", "Pico (req/s)", "Instâncias App", "Banco de Dados", "Custo Mensal (USD)", "Custo / Pedido (USD)"]
for col_idx, h in enumerate(headers_resumo, start=1):
    cell = ws1.cell(row=4, column=col_idx, value=h)
    cell.fill = header_fill
    cell.font = header_font
    cell.alignment = Alignment(horizontal="center", vertical="center", wrap_text=True)

dados_resumo = [
    ["1 — Baixo (Regional)", 10, 50, 1500, 2, "1x ECS Fargate (0.25 vCPU, 0.5 GB)", "RDS PostgreSQL db.t3.micro (Single-AZ)", 24.57, 0.0164],
    ["2 — Moderado (Estadual)", 100, 500, 15000, 15, "2x ECS Fargate (0.5 vCPU, 1 GB) + ALB", "RDS PostgreSQL db.t3.small (Single-AZ)", 92.18, 0.0061],
    ["3 — Alto (Nacional)", 500, 5000, 150000, 95, "4x Core + 2x Telemetria ECS + ALB", "RDS db.t3.medium (Multi-AZ + Read Replica)", 417.14, 0.0027]
]

for row_idx, row_data in enumerate(dados_resumo, start=5):
    for col_idx, val in enumerate(row_data, start=1):
        cell = ws1.cell(row=row_idx, column=col_idx, value=val)
        cell.font = regular_font
        cell.border = thin_border
        if col_idx in [2, 3, 4, 5]:
            cell.alignment = Alignment(horizontal="center")
        elif col_idx == 8:
            cell.number_format = currency_format
            cell.font = bold_font
            cell.alignment = Alignment(horizontal="right")
        elif col_idx == 9:
            cell.number_format = '"$"#,##0.0000'
            cell.alignment = Alignment(horizontal="right")

# Adiciona linha Render
ws1.cell(row=9, column=1, value="Opção PaaS (Render - Cenário 1)").font = bold_font
ws1.cell(row=9, column=6, value="1x Web Service Starter ($7)").font = regular_font
ws1.cell(row=9, column=7, value="PostgreSQL Gerenciado ($7)").font = regular_font
c_render = ws1.cell(row=9, column=8, value=14.00)
c_render.font = bold_font
c_render.number_format = currency_format
c_render_ped = ws1.cell(row=9, column=9, value=0.0093)
c_render_ped.font = regular_font
c_render_ped.number_format = '"$"#,##0.0000'

# ----------------- ABA 2: DETALHAMENTO AWS -----------------
ws2 = wb.create_sheet(title="Detalhamento AWS")

ws2["A1"] = "DETALHAMENTO DE RECURSOS POR CENÁRIO — AWS ON-DEMAND (US-EAST-1)"
ws2["A1"].font = title_font

detalhes = [
    # Cenário 1
    ("CENÁRIO 1 — BAIXO (1.500 pedidos/mês)", [
        ["AWS ECS Fargate (Aplicação)", "0.25 vCPU, 0.5 GB RAM (730 horas/mês)", 1, 9.13],
        ["AWS RDS PostgreSQL", "db.t3.micro (Single-AZ, 1 vCPU, 1 GB RAM)", 1, 13.14],
        ["Armazenamento RDS", "General Purpose SSD (gp3) - 20 GB", 1, 2.30],
        ["Transferência de Dados (Out)", "Transferência de saída (~10 GB/mês)", 1, 0.00]
    ]),
    # Cenário 2
    ("CENÁRIO 2 — MODERADO (15.000 pedidos/mês)", [
        ["AWS ECS Fargate (Aplicação)", "0.5 vCPU, 1 GB RAM (2 tarefas ativas 730h)", 2, 36.50],
        ["AWS Application Load Balancer", "1 ALB com balanceamento HTTP/HTTPS", 1, 22.50],
        ["AWS RDS PostgreSQL", "db.t3.small (Single-AZ, 2 vCPU, 2 GB RAM)", 1, 26.28],
        ["Armazenamento RDS", "General Purpose SSD (gp3) - 60 GB", 1, 6.90],
        ["Transferência de Dados (Out)", "Transferência de saída (~50 GB/mês)", 1, 0.00]
    ]),
    # Cenário 3
    ("CENÁRIO 3 — ALTO (150.000 pedidos/mês)", [
        ["AWS ECS Fargate (Core App)", "1.0 vCPU, 2 GB RAM (4 tarefas ativas 730h)", 4, 145.71],
        ["AWS ECS Fargate (Telemetria)", "0.5 vCPU, 1 GB RAM (2 tarefas ativas 730h)", 2, 36.50],
        ["AWS Application Load Balancer", "1 ALB com regras de roteamento avançado", 1, 35.00],
        ["AWS RDS PostgreSQL (Master)", "db.t3.medium (Multi-AZ, 2 vCPU, 4 GB RAM)", 1, 105.12],
        ["AWS RDS Read Replica", "db.t3.medium (Single-AZ para leitura de telemetria)", 1, 52.56],
        ["Armazenamento RDS", "General Purpose SSD (gp3) - 250 GB", 1, 28.75],
        ["Transferência de Dados (Out)", "Transferência de saída (~250 GB/mês)", 1, 13.50]
    ])
]

current_row = 3
for secao, itens in detalhes:
    ws2.cell(row=current_row, column=1, value=secao).font = sub_header_font
    for c in range(1, 5):
        ws2.cell(row=current_row, column=c).fill = sub_header_fill
    current_row += 1
    
    ws2.cell(row=current_row, column=1, value="Item").font = bold_font
    ws2.cell(row=current_row, column=2, value="Especificação Técnica").font = bold_font
    ws2.cell(row=current_row, column=3, value="Quantidade").font = bold_font
    ws2.cell(row=current_row, column=4, value="Custo Mensal (USD)").font = bold_font
    current_row += 1
    
    subtotal_start = current_row
    for item in itens:
        ws2.cell(row=current_row, column=1, value=item[0]).font = regular_font
        ws2.cell(row=current_row, column=2, value=item[1]).font = regular_font
        ws2.cell(row=current_row, column=3, value=item[2]).alignment = Alignment(horizontal="center")
        c_val = ws2.cell(row=current_row, column=4, value=item[3])
        c_val.number_format = currency_format
        c_val.alignment = Alignment(horizontal="right")
        current_row += 1
        
    ws2.cell(row=current_row, column=2, value="Subtotal").font = bold_font
    c_tot = ws2.cell(row=current_row, column=4, value=f"=SUM(D{subtotal_start}:D{current_row-1})")
    c_tot.font = bold_font
    c_tot.number_format = currency_format
    current_row += 2

# Ajuste automático de largura de colunas
for ws in [ws1, ws2]:
    for col in ws.columns:
        max_len = max(len(str(cell.value or '')) for cell in col)
        col_letter = get_column_letter(col[0].column)
        ws.column_dimensions[col_letter].width = max(max_len + 3, 12)

excel_path = r"C:\Users\lucas.luna\Desktop\Github - Projetos\PI2\docs\arquitetura\orcamento.xlsx"
wb.save(excel_path)
print("Planilha Excel criada com sucesso em:", excel_path)

