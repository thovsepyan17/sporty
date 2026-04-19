# Sporty Betting — Settlement Trigger Service

A Spring Boot backend service that simulates sports betting event outcome handling and bet settlement via **Kafka** and **Apache RocketMQ**.

## Architecture

```
 ┌──────────┐       ┌─────────────────┐       ┌──────────────────┐       ┌───────────────────┐       ┌──────────────────┐
 │ REST API │──────▶│ Kafka Producer  │──────▶│ Kafka Consumer   │──────▶│ RocketMQ Producer │──────▶│ RocketMQ Consumer│
 │ POST     │       │ event-outcomes  │       │ event-outcomes   │       │ bet-settlements   │       │ bet-settlements  │
 └──────────┘       └─────────────────┘       │ + Bet Matching   │       └───────────────────┘       │ + Bet Settlement │
                                              └──────────────────┘                                   └──────────────────┘
```

**Flow:**
1. REST endpoint receives an event outcome and publishes it to Kafka topic `event-outcomes`.
2. Kafka consumer picks up the message and queries the H2 database for pending bets matching the event ID.
3. For each matched bet, it determines WIN/LOSS and sends a settlement message to RocketMQ topic `bet-settlements`.
4. RocketMQ consumer receives the settlement message and updates the bet status in the database.

## Tech Stack

| Component       | Technology                          |
|-----------------|-------------------------------------|
| Framework       | Spring Boot 3.2.5                   |
| Language        | Java 17                             |
| Messaging       | Apache Kafka (Spring Kafka)         |
| Messaging       | Apache RocketMQ (rocketmq-spring)   |
| Database        | H2 (in-memory)                      |
| API Docs        | Swagger / OpenAPI 3 (springdoc)     |
| Testing         | JUnit 5, Mockito, Embedded Kafka, Awaitility |
| Build           | Maven                               |
| Infrastructure  | Docker Compose                      |

## Prerequisites

- **Java 17+**
- **Maven 3.8+**
- **Docker & Docker Compose** (for Kafka; RocketMQ optional)

## Quick Start

### 1. Start Infrastructure

```bash
docker-compose up -d
```

This starts:
- **Kafka** (KRaft mode, no Zookeeper) on `localhost:9092`
- **RocketMQ NameServer** on `localhost:9876` *(optional — only needed for full mode)*
- **RocketMQ Broker** on `localhost:10911` *(optional)*
- **Kafka UI** on http://localhost:8090 *(browse Kafka topics/messages in the browser)*
- **RocketMQ Dashboard** on http://localhost:8091 *(browse RocketMQ in the browser)*

### 2. Run the Application

**Option A — Default mode (Kafka + mock RocketMQ):**

```bash
./mvnw spring-boot:run
```

By default, RocketMQ is mocked: the producer logs the payload and directly triggers bet settlement, simulating the full end-to-end flow without requiring RocketMQ infrastructure.

**Option B — Full mode (Kafka + real RocketMQ via Docker):**

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=docker
```

Requires all Docker Compose services running, including RocketMQ.

### 3. Test the Flow

**Check pre-loaded bets:**

```bash
curl http://localhost:8080/api/v1/bets
```

**Publish an event outcome** (TEAM-A wins EVT-001):

```bash
curl -X POST http://localhost:8080/api/v1/event-outcomes \
  -H "Content-Type: application/json" \
  -d '{"eventId":"EVT-001","eventName":"Champions League Final","eventWinnerId":"TEAM-A"}'
```

**Verify settlement** (BET-001 should be WON, BET-002 should be LOST):

```bash
curl http://localhost:8080/api/v1/bets
```

## Sample Data

The application starts with 5 pre-loaded bets:

| Bet ID  | User    | Event   | Bet On   | Amount  |
|---------|---------|---------|----------|---------|
| BET-001 | USER-001| EVT-001 | TEAM-A   | 50.00   |
| BET-002 | USER-002| EVT-001 | TEAM-B   | 100.00  |
| BET-003 | USER-003| EVT-002 | TEAM-C   | 75.00   |
| BET-004 | USER-001| EVT-002 | TEAM-D   | 200.00  |
| BET-005 | USER-004| EVT-003 | PLAYER-X | 150.00  |

## API Reference

### Publish Event Outcome

```
POST /api/v1/event-outcomes
```

**Request Body:**

```json
{
  "eventId": "EVT-001",
  "eventName": "Champions League Final",
  "eventWinnerId": "TEAM-A"
}
```

**Response (202 Accepted):**

```json
{
  "success": true,
  "message": "Event outcome published to Kafka successfully"
}
```

### List All Bets

```
GET /api/v1/bets
```

### Get Bet by ID

```
GET /api/v1/bets/{betId}
```

### Swagger UI

Interactive API documentation available at:

```
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON spec at `http://localhost:8080/v3/api-docs`.

### H2 Console

Available at `http://localhost:8080/h2-console` with JDBC URL `jdbc:h2:mem:bettingdb`.

### Kafka & RocketMQ in the browser (no terminal)

After `docker-compose up -d`, open:

| Tool | URL | What you see |
|------|-----|--------------|
| **Kafka UI** | http://localhost:8090 | Topics (e.g. `event-outcomes`), messages, consumer groups |
| **RocketMQ Dashboard** | http://localhost:8091 | Topics (e.g. `bet-settlements`), producers/consumers |

In **Kafka UI**: select cluster **local** → **Topics** → `event-outcomes` → **Messages** — trigger **POST** `/api/v1/event-outcomes` from Swagger and refresh to see new records.

In **RocketMQ Dashboard**: **Topic** → search `bet-settlements` → inspect message traffic after publishing an event outcome (use Spring profile `docker` for real RocketMQ).

## Project Structure

```
src/main/java/com/sporty/betting/
├── SportyBettingApplication.java          # Entry point
├── config/
│   └── KafkaConfig.java                   # Kafka topic configuration
├── controller/
│   ├── EventOutcomeController.java        # REST API for event outcomes
│   ├── BetController.java                 # REST API for bet queries
│   └── GlobalExceptionHandler.java        # Centralized error handling
├── model/
│   ├── dto/
│   │   ├── EventOutcomeRequest.java       # API / Kafka message payload
│   │   ├── BetSettlementMessage.java      # RocketMQ message payload
│   │   └── ApiResponse.java              # Generic API response wrapper
│   ├── entity/
│   │   └── Bet.java                       # JPA entity
│   └── enums/
│       └── BetStatus.java                 # PENDING, WON, LOST
├── repository/
│   └── BetRepository.java                 # Spring Data JPA repository
├── service/
│   ├── BetSettlementService.java          # Bet settlement logic
│   ├── kafka/
│   │   ├── EventOutcomeProducer.java      # Publishes to Kafka
│   │   └── EventOutcomeConsumer.java      # Consumes from Kafka + matches bets
│   └── rocketmq/
│       ├── BetSettlementProducer.java     # Interface (strategy pattern)
│       ├── RocketMQBetSettlementProducer.java  # Real RocketMQ impl
│       ├── MockBetSettlementProducer.java      # Mock impl (logs + settles)
│       └── BetSettlementConsumerListener.java  # RocketMQ consumer
└── init/
    └── DataInitializer.java               # Seeds sample bets on startup
```

## Testing

Run the full test suite (unit + integration):

```bash
./mvnw test
```

| Type        | Class                              | What it covers                                    |
|-------------|------------------------------------|---------------------------------------------------|
| Unit        | `EventOutcomeControllerTest`       | REST validation, 202/400 responses                |
| Unit        | `BetControllerTest`                | GET bets, 200/404 responses                       |
| Unit        | `EventOutcomeProducerTest`         | Kafka template invocation                         |
| Unit        | `EventOutcomeConsumerTest`         | Bet matching, win/loss logic, settlement dispatch |
| Unit        | `BetSettlementServiceTest`         | DB update on settle, not-found exception          |
| Unit        | `MockBetSettlementProducerTest`    | Mock producer delegates to settlement service     |
| Integration | `BettingFlowIntegrationTest`       | Full end-to-end: REST -> Kafka -> settle in DB    |

Integration tests use **Embedded Kafka** and the `mock` RocketMQ profile so they run without any external infrastructure.

## Design Decisions

- **Strategy Pattern for RocketMQ**: The `BetSettlementProducer` interface allows swapping between real and mock implementations via Spring `@ConditionalOnProperty`, keeping the Kafka consumer decoupled from the messaging infrastructure.
- **In-Memory H2**: As required, bets are stored in H2 for simplicity and zero-setup persistence.
- **JSON Serialization**: Both Kafka and RocketMQ messages use JSON for human-readable, debuggable payloads.
- **Win Multiplier**: A simplified 2x payout multiplier is used for winning bets; this would be replaced by market odds in a real system.
