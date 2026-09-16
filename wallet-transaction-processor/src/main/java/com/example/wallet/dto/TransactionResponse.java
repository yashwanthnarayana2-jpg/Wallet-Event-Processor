package com.example.wallet.dto;

import com.example.wallet.enums.TransactionStatus;
import java.math.BigDecimal;
import java.util.UUID;

public class TransactionResponse {
    private UUID transactionId;
    private UUID userId;
    private BigDecimal amount;
    private TransactionStatus status;
    private String message;
    private BigDecimal balance;

    public TransactionResponse() {}

    public TransactionResponse(UUID transactionId, UUID userId, BigDecimal amount,
                               TransactionStatus status, String message, BigDecimal balance) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.amount = amount;
        this.status = status;
        this.message = message;
        this.balance = balance;
    }

    public UUID getTransactionId() { return transactionId; }
    public UUID getUserId() { return userId; }
    public BigDecimal getAmount() { return amount; }
    public TransactionStatus getStatus() { return status; }
    public String getMessage() { return message; }
    public BigDecimal getBalance() { return balance; }
}
