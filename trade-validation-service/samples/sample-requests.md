# Sample requests

These illustrate the "multiple source systems, one canonical schema" scenario
the service is built around. Run the app (`mvn spring-boot:run`) then try
these with curl, Postman, or the same commands below.

## 1. Valid trade — primary field names (e.g. "EQUITIES_CORE" system)

```bash
curl -X POST http://localhost:8080/trades \
  -H "Content-Type: application/json" \
  -d '{
    "sourceSystem": "EQUITIES_CORE",
    "tradeId": "T-1001",
    "counterparty": "Acme Capital",
    "instrumentId": "US0378331005",
    "settlementDate": "2026-09-20",
    "amount": "15000.50",
    "currency": "USD"
  }'
```

## 2. Valid trade — alias field names, alternate date format (e.g. "DERIVATIVES_LEGACY" system)

```bash
curl -X POST http://localhost:8080/trades \
  -H "Content-Type: application/json" \
  -d '{
    "sourceSystem": "DERIVATIVES_LEGACY",
    "txnRef": "TXN-9988",
    "counterpartyName": "Beta Bank",
    "securityCode": "SEC-778",
    "valueDate": "20/09/2026",
    "amount": "2,500,000.00",
    "currency": "eur"
  }'
```

## 3. Invalid trade — bad currency code (gets rejected + logged to audit trail)

```bash
curl -X POST http://localhost:8080/trades \
  -H "Content-Type: application/json" \
  -d '{
    "sourceSystem": "EQUITIES_CORE",
    "tradeId": "T-1002",
    "counterparty": "Acme Capital",
    "instrumentId": "US0378331005",
    "settlementDate": "2026-09-20",
    "amount": "15000.50",
    "currency": "ZZZ"
  }'
```

## 4. Invalid trade — settlement date too far in the future

```bash
curl -X POST http://localhost:8080/trades \
  -H "Content-Type: application/json" \
  -d '{
    "sourceSystem": "EQUITIES_CORE",
    "tradeId": "T-1003",
    "counterparty": "Acme Capital",
    "instrumentId": "US0378331005",
    "settlementDate": "2027-12-31",
    "amount": "15000.50",
    "currency": "USD"
  }'
```

## 5. Retrieve accepted trades

```bash
curl http://localhost:8080/trades
```

## 6. Retrieve a single trade

```bash
curl http://localhost:8080/trades/T-1001
```

## 7. Retrieve rejected records (audit trail)

```bash
curl http://localhost:8080/trades/rejected
```
