# Decision Log

## 1. Concurrency Race Condition

The application uses JPA pessimistic write locking with
`@Lock(LockModeType.PESSIMISTIC_WRITE)` on the wallet query.

The wallet row is locked before its balance is read or changed. Concurrent
debit requests therefore wait for the previous transaction to finish and
always read the latest balance. This prevents lost updates and negative
balances.

## 2. Idempotency

The transaction table has a unique constraint on `transaction_id`. The
service checks for an existing transaction before processing. A duplicate
request returns the original result without deducting the balance again.

The database unique constraint provides an additional protection against
concurrent duplicate webhook requests.

## 3. AI Assistant Limitation

An initial approach that only checked transaction existence before updating
the wallet was not sufficient because concurrent requests could both pass
the check. The implementation was improved with database uniqueness and
pessimistic wallet locking.

## 4. H2

H2 was selected because it is an in-memory database and requires no external
setup, allowing tests to run directly in IntelliJ.

## 5. BigDecimal

BigDecimal is used for financial amounts to avoid floating-point precision
issues.
