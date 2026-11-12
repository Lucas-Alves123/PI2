# language: pt
Funcionalidade: Alocar bolsas compatíveis para uma requisição hospitalar
  Como atendente de hemoterapia responsável pelo atendimento de um hospital
  Quero solicitar bolsas de sangue informando o tipo sanguíneo e o componente necessário
  Para que o sistema selecione e reserve automaticamente as bolsas compatíveis disponíveis
       priorizando as que vencem primeiro (FEFO)

  Cenário: Alocar a bolsa compatível com vencimento mais próximo (FEFO)
    Dado que existem três bolsas O+ de CH disponíveis com datas de vencimento diferentes
    Quando uma requisição for registrada solicitando 1 bolsa O+ de CH
    Então a bolsa com data de validade mais próxima deve ser alocada
    E o status da requisição deve ser "ATENDIDA"
    E a bolsa alocada deve ficar reservada no estoque
    E o contexto de Notificação deve registrar que a requisição foi atendida

  Cenário: Não alocar bolsa com tipo sanguíneo incompatível
    Dado que existe apenas uma bolsa AB+ de CH disponível no estoque
    Quando uma requisição for registrada solicitando 1 bolsa O- de CH
    Então nenhuma bolsa deve ser alocada
    E o status da requisição deve ser "PENDENTE"
    E nenhuma notificação de requisição atendida deve ser registrada

  Cenário: Manter status como PARCIALMENTE_ATENDIDA quando não há bolsas suficientes
    Dado que existe apenas 1 bolsa O+ de CH disponível no estoque
    Quando uma requisição for registrada solicitando 3 bolsas O+ de CH
    Então apenas 1 bolsa deve ser alocada
    E o status da requisição deve ser "PARCIALMENTE_ATENDIDA"
    E nenhuma notificação de requisição atendida deve ser registrada
