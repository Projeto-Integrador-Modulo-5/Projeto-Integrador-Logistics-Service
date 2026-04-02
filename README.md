# Projeto-Integrador-Logistics-Service

Microsserviço de logística do e-commerce de camisetas — Projeto Integrador ADS 4º Período · PUC Goiás · 2026/1.

Responsável por consumir eventos de pedido do Apache Kafka, gerenciar a máquina de estados do pedido e publicar eventos de status para o `notifications-topic`.

---

## Responsabilidades

- Consumir eventos `OrderCreatedEvent` do tópico `orders-topic`
- Gerenciar a máquina de estados do pedido: `PROCESSANDO → ENVIADO → ENTREGUE`
- Persistir transições de estado no PostgreSQL
- Publicar eventos de atualização de status no tópico `notifications-topic`

---

## Fluxo de eventos

```
Backend Service
  └─► [orders-topic] ──► Logistics Service
                              └─► persiste estado no PostgreSQL
                              └─► [notifications-topic] ──► Notification Service
```

O Logistics Service **não conhece** o Notification Service — publica no tópico e qualquer consumidor interessado reage. Esse é o desacoplamento assíncrono central da arquitetura.

---

## Stack

| Camada      | Tecnologia                                        |
|-------------|---------------------------------------------------|
| Linguagem   | Java 21                                           |
| Framework   | Spring Boot 3.x (Web, Data JPA, Kafka)            |
| Banco       | PostgreSQL 15                                     |
| Mensageria  | Apache Kafka — consumer `orders-topic`, producer `notifications-topic` |
| Build       | Maven (Wrapper incluído)                          |
| Container   | Docker (orquestrado via `Projeto-Integrador-Infra`) |

---

## Estrutura de pacotes

```
com.ecommerce.logistics/
├── controller/   # Endpoints REST (ex: atualização manual de status pelo admin)
├── service/      # Lógica de negócio e máquina de estados
├── repository/   # JpaRepository
├── messaging/    # @KafkaListener (consumer) + KafkaTemplate (producer)
├── domain/       # Entidades, OrderStatus (sealed interface) — zero framework
└── dto/          # Java Records
```

---

## Máquina de estados do pedido

```
Processing → Confirmed → Shipped → Delivered
                   ↘
               Cancelled
```

Implementada com `sealed interface OrderStatus` do Java 21:

```java
public sealed interface OrderStatus
    permits Processing, Confirmed, Shipped, Delivered, Cancelled {}
```

---

## Configuração Kafka

```yaml
spring:
  kafka:
    bootstrap-servers: kafka:9092
    consumer:
      group-id: logistics-group
      auto-offset-reset: earliest
      value-deserializer: JsonDeserializer
    producer:
      value-serializer: JsonSerializer
      acks: all
```

---

## Configuração

```bash
cp .env.example .env
# configure KAFKA_BOOTSTRAP_SERVERS, DATABASE_URL
```

---

## Executando localmente

> Recomendado subir a infraestrutura pelo repositório `Projeto-Integrador-Infra` antes.

```bash
./mvnw spring-boot:run
```

A aplicação sobe na porta `8081`.

---

## Testes

```bash
./mvnw verify
```

Testes de integração usam **Testcontainers** para subir PostgreSQL e Kafka reais durante a execução.

---

## Repositórios relacionados

| Repositório | Responsabilidade |
|---|---|
| [Projeto-Integrador-Infra](https://github.com/Projeto-Integrador-Modulo-5/Projeto-Integrador-Infra) | Docker Compose e infraestrutura |
| [Projeto-Integrador-Backend](https://github.com/Projeto-Integrador-Modulo-5/Projeto-Integrador-Backend) | Publica eventos em `orders-topic` |
| [Projeto-Integrador-Notification-Service](https://github.com/Projeto-Integrador-Modulo-5/Projeto-Integrador-Notification-Service) | Consome `notifications-topic` e notifica clientes |
| [Projeto-Integrador-Frontend](https://github.com/Projeto-Integrador-Modulo-5/Projeto-Integrador-Frontend) | Exibe atualizações de status em tempo real |
