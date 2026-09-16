# Wallet Transaction Processor

A Spring Boot service for idempotent wallet payment event processing.

## Stack
- Java 17
- Spring Boot 3.2.5
- Spring Data JPA
- H2
- JUnit 5
- Maven

## Endpoint

POST `/api/v1/transactions/process`

Example:
```json
{
  "transactionId": "11111111-1111-1111-1111-111111111111",
  "userId": "22222222-2222-2222-2222-222222222222",
  "amount": 250.00,
  "type": "DEBIT"
}
```

## Run

```bash
mvn spring-boot:run
```

## Test

```bash
mvn test
```

The integration tests cover:
1. Single valid debit.
2. Three simultaneous duplicate transaction IDs.
3. Ten concurrent debits against a ₹500 wallet.
