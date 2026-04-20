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

### Pipeline diagrams (same style as end-to-end flow)

**Real RocketMQ** — run with `spring.profiles.active=docker` and Docker RocketMQ up. Five steps in one line:

```
┌────────────┐     ┌──────────────────┐     ┌─────────────────────────────┐     ┌────────────────────┐     ┌─────────────────────────────┐
│  REST API  │────▶│  Kafka Producer  │────▶│      Kafka Consumer         │────▶│ RocketMQ Producer  │────▶│     RocketMQ Consumer       │
│    POST    │     │  event-outcomes  │     │  event-outcomes             │     │  bet-settlements   │     │  bet-settlements            │
│            │     │                  │     │  + Bet Matching             │     │                    │     │  + Bet Settlement           │
└────────────┘     └──────────────────┘     └─────────────────────────────┘     └────────────────────┘     └─────────────────────────────┘
```

**Mock RocketMQ (default)** — Kafka is real; RocketMQ broker is skipped. After bet matching, settlement runs in the same app (log + `settleBet()` → H2):

```
┌────────────┐     ┌──────────────────┐     ┌─────────────────────────────┐     ┌─────────────────────────────┐
│  REST API  │────▶│  Kafka Producer  │────▶│      Kafka Consumer         │────▶│  Mock producer (no broker)  │
│    POST    │     │  event-outcomes  │     │  event-outcomes             │     │  log payload + settle → H2  │
│            │     │                  │     │  + Bet Matching             │     │                             │
└────────────┘     └──────────────────┘     └─────────────────────────────┘     └─────────────────────────────┘
```

| Mode | Spring profile | RocketMQ topic `bet-settlements` |
|------|----------------|----------------------------------|
| Real | `docker` | Used (producer → broker → consumer) |
| Mock | *(none / default)* | Not used (in-process only) |

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
- **RocketMQ Dashboard** on http://localhost:8091 *(browse RocketMQ topics/messages in the browser)*

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

**Host IDE + Docker RocketMQ:** The broker loads **`docker/rocketmq/broker-docker.conf`**, which sets **`brokerIP1 = 127.0.0.1`**, so NameServer returns an address your **Windows/Mac host** can use with published ports **10909** and **10911**. (The JVM flag `-Drocketmq.broker.ip1` is not reliably picked up by the `apache/rocketmq` image entrypoint.) After changing this file, recreate the broker: `docker-compose up -d --force-recreate rocketmq-broker` (and restart the app). Re-create the topic if needed: `.\scripts\create-rocketmq-topic.ps1`.

If you see **`sendDefaultImpl call timeout`** / **`RemotingTooMuchRequestException`**, allow **TCP 10909 and 10911** through the firewall; **`send-message-timeout`** is raised in **`application.yml`**.

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

### RocketMQ in the browser (no terminal)

After `docker-compose up -d`, open **http://localhost:8091** (RocketMQ Dashboard). The image listens on **8082** inside Docker; compose maps **8091 → 8082** — if you changed the mapping, use the host port you set. Use the **Topic** tab (not **Producer**) to list topics. Inspect **`bet-settlements`** after you publish an event outcome (Spring profile `docker` for real RocketMQ).

**If the dashboard will not create a topic:** create it with the broker CLI (reliable on Windows):

```powershell
.\scripts\create-rocketmq-topic.ps1
```

Or manually:

```text
docker exec sporty-rocketmq-broker sh mqadmin updateTopic -n rocketmq-namesrv:9876 -t bet-settlements -c DefaultCluster -r 4 -w 4
```

Cluster name is **`DefaultCluster`** for this Docker setup. Producer group: **`bet-settlement-producer-group`**. Consumer group: **`bet-settlement-consumer-group`**.

Kafka has no UI in this compose file; watch **application logs** for `Publishing event outcome` / `Received event outcome from Kafka`, or use `kafka-console-consumer.sh` inside the Kafka container if you need raw topic inspection.

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
