# 🛠️ Oficina OS Service

## Autor
- **Nome:** Lucas Eduardo Peters Scuissiatto
- **RM:** RM365455
- **Curso:** Pós-Graduação em Arquitetura de Software

---

## 📖 Descrição

O **OS Service** é o núcleo (Core) do ecossistema da oficina. Ele atua como o orquestrador do fluxo de negócio, gerenciando desde o cadastro inicial de clientes e veículos até o ciclo de vida completo das Ordens de Serviço (OS).

## 🚀 Tecnologias e Dependências
* **Java 21 / Spring Boot 3**
* **Spring Data JPA** Camada de persistência para Clientes, Veículos e OS.
* **RabbitMQ** Comunicação orientada a eventos com os demais microsserviços.
* **JUnit 5 & Mockito** Testes unitários de alta cobertura.
* **Cucumber** Testes de aceitação (BDD) para validação de regras de negócio.
* **Jacoco & SonarQube** Garantia de qualidade e métricas de cobertura.

## 🏗️ Arquitetura e Fluxo de Negócio

Este serviço funciona como o ponto de partida e de chegada de todo o processo:

1. **Abertura e Orçamento**: O usuário cadastra o cliente, o veículo e os itens da OS.
2. **Publicação de Cobrança**: Ao finalizar o orçamento, o serviço publica um evento na fila `os-created-queue` para que o `Billing Service` processe o pagamento.
3. **Atualização por Pagamento**: O serviço consome a fila `os-payment-approved-queue`. Ao receber a confirmação, o status da OS é alterado para `PAID` ou `IN_PROGRESS`.
4. **Finalização de Serviço**: O serviço consome a fila `execution-finished-queue`. Quando o mecânico termina o trabalho no `Execution Service`, a OS é marcada automaticamente como `COMPLETED` aqui.

## 📥 Filas de Entrada (Consumer)

* `os-payment-approved-queue`: Recebe a confirmação de pagamento do Billing Service.
* `execution-finished-queue`: Recebe a notificação de serviço concluído do Execution Service.

## 📤 Fila de Saída (Producer)

* `os-created-queue`: Dispara os dados da OS (ID e Valor Total) para que o sistema de cobrança inicie o checkout.


## 🧪 Qualidade, BDD e Testes
O **OS Service** possui uma estratégia de testes em camadas para garantir a estabilidade do fluxo principal:

### Testes Unitários e SonarQube

Focado na cobertura de Controllers, Services e Repositories.


```bash
# Executar testes e gerar relatório JaCoCo
mvn clean verify

# Enviar para o SonarQube local
mvn sonar:sonar \
  -Dsonar.projectKey=oficina-os-service \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=admin \
  -Dsonar.password=admin123
```

## Testes BDD (Behavior Driven Development)
Utilizamos **Cucumber** para validar cenários de negócio em linguagem natural (Gherkin). Isso garante que o software atenda exatamente aos requisitos especificados.
* **Cenário Validado**: Criação de Ordem de Serviço com sucesso, garantindo a integridade dos dados de Cliente e Veículo.
* **Localização**: `src/test/resources/features/service_order.feature

## 🐳 Como rodar a aplicação

1. Certifique-se de que o **RabbitMQ** e o **Banco de Dados** (MySQL/PostgreSQL) estejam rodando (via Docker Compose na raiz do projeto).
2. Execute o comando:

```bash
mvn spring-boot:run
```