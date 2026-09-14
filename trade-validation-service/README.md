# Trade Data Validation & Standardization Service

A Spring Boot microservice that ingests trade records from simulated
"source systems" that each use slightly different field names and date
formats, standardizes them into one canonical schema, validates them
against a set of data-governance rules, and persists the outcome —
either as an accepted trade or as a rejected record with reasons attached.

This is a small, self-contained illustration of a pattern used broadly
across financial market infrastructure platforms: many upstream systems,
one trusted, standardized source of truth downstream.

## Why this project

Built to demonstrate:
- Java/Spring Boot backend development (REST APIs, JPA, layered service design)
- Data standardization across heterogeneous inputs
- Data governance / validation rule design
- A thorough automated test suite (unit + integration), including edge cases

## Architecture

```
IncomingTradeRequest (raw payload, multiple possible field names)
        |
        v
TradeStandardizationService   -- maps aliases, parses dates/amounts --> Trade (canonical model)
        |
        v
TradeValidator                -- governance rules: required fields, valid
        |                         currency, sane date range, amount bounds,
        |                         duplicate trade IDs
        v
TradeIngestService            -- orchestrates the pipeline, transactional
        |
        +--> accepted --> TradeRepository (trades table)
        |
        +--> rejected --> RejectedTradeRepository (rejected_trades table, audit trail)
```

## Tech stack

- Java 17
- Spring Boot 3 (Web, Data JPA, Validation)
- H2 in-memory database (swap for Postgres/MySQL in a real deployment — just
  change the `spring.datasource` properties in `application.yml`)
- JUnit 5 + Spring's MockMvc for integration tests
- Maven

## Running it

```bash
mvn spring-boot:run
```

The service starts on `http://localhost:8080`. The H2 console (for poking
around the in-memory DB) is available at `http://localhost:8080/h2-console`
(JDBC URL: `jdbc:h2:mem:tradedb`, user `sa`, empty password).

## API

| Method | Path              | Description                                         |
|--------|-------------------|------------------------------------------------------|
| POST   | `/trades`         | Submit a raw trade payload for standardization + validation |
| GET    | `/trades`         | List all accepted trades                              |
| GET    | `/trades/{id}`    | Get one accepted trade by trade ID                     |
| GET    | `/trades/rejected`| List all rejected records with reasons (audit trail)   |

See [`samples/sample-requests.md`](samples/sample-requests.md) for ready-to-run
`curl` examples, including payloads from two different simulated source
systems and a couple of deliberately invalid payloads.

## Running the tests

```bash
mvn test
```

Test coverage includes:
- **`TradeStandardizationServiceTest`** — field-name aliasing across source
  systems, multiple date formats, thousands-separator handling, graceful
  handling of unparseable values.
- **`TradeValidatorTest`** — every governance rule individually (missing
  fields, duplicate trade ID, invalid currency, settlement date out of
  range, non-positive or over-threshold amounts), plus a test asserting
  multiple simultaneous errors are all surfaced at once.
- **`TradeControllerIntegrationTest`** — full HTTP-to-database round trips
  using the real Spring context and an in-memory database: accept-and-retrieve,
  alias-field ingestion, rejection with audit-trail verification, duplicate
  detection, and malformed-JSON handling.

## Possible extensions

- Swap H2 for Postgres and add Flyway/Liquibase migrations
- Add Kafka (or Spring Cloud Stream) to ingest trades as a stream of events
  instead of synchronous REST calls
- Add a reference-data service backing the currency/instrument allow-lists
  instead of the in-code `Set` used here
- Add pagination and filtering to `GET /trades`
- Add Spring Security if this were to sit behind real client traffic
