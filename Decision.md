# Decision Log

## 1. How did you handle the concurrency race condition?

The application uses database-level pessimistic locking to handle concurrent wallet updates.

When a transaction request is received:

1. The service starts a database transaction using `@Transactional`.
2. It checks whether the transaction ID has already been processed.
3. The wallet is fetched using a pessimistic write lock:

   ```java
   @Lock(LockModeType.PESSIMISTIC_WRITE)
   @Query("select w from Wallet w where w.userId = :userId")
   Optional<Wallet> findByUserIdForUpdate(@Param("userId") UUID userId);
   ```
4. The pessimistic lock ensures that only one concurrent request can update the same wallet at a time.
5. Other requests wait until the lock is released and then read the latest wallet balance.
6. Before applying a debit, the service checks whether the wallet has sufficient funds.
7. If the balance is insufficient, the request fails and the balance is not changed.
8. The transaction ID has a unique database constraint, which prevents the same transaction from being recorded more than once.

For example, if ten concurrent debit requests of ₹100 are sent for a wallet containing ₹500:

* Five requests successfully debit ₹100.
* The wallet balance reaches ₹0.
* The remaining five requests fail because the balance is insufficient.
* The final balance remains ₹0 and never becomes negative.

This approach prevents lost updates, duplicate balance deductions, and negative wallet balances during concurrent requests.

## 2. Where did your AI assistant give you an incorrect or sub-optimal suggestion?

Initially, the AI assistant suggested checking whether the transaction already exists before locking the wallet and then inserting the transaction after updating the balance.

This approach is not completely safe by itself because two identical requests can pass the initial transaction-existence check at the same time. Both requests may then continue processing before either transaction record is committed.

The improved implementation addresses this using:

* A unique database constraint on `transaction_id`.
* A second transaction-existence check after acquiring the wallet lock.
* Database-level pessimistic locking for wallet updates.
* Transactional processing so that the wallet update and transaction record are handled together.

The AI assistant's initial suggestion was therefore useful as a starting point, but it was sub-optimal because an application-level existence check alone cannot guarantee idempotency under concurrent requests. Database constraints and proper transaction handling are required for reliable concurrency control.
