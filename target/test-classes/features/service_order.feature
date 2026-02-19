# language: pt
Funcionalidade: Gerenciamento de Ordens de Servico

  Cenario: Criar uma nova Ordem de Servico com sucesso
    Dado que existe um cliente com ID 1 e um veiculo com ID 1
    Quando eu solicito a criacao de uma Ordem de Servico para este cliente e veiculo
    Entao a Ordem de Servico e criada com status "RECEIVED"