package com.example.wallet.entity;

import com.example.wallet.enums.TransactionStatus;
import com.example.wallet.enums.TransactionType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions",
       uniqueConstraints = @UniqueConstraint(name = "uk_transaction_id", columnNames = "transaction_id"))
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", nullable = false, unique = true)
    private UUID transactionId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Transaction() {}

    public Transaction(UUID transactionId, UUID userId, BigDecimal amount,
                        TransactionType type, TransactionStatus status,
                        LocalDateTime createdAt) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public UUID getTransactionId() { return transactionId; }
    public UUID getUserId() { return userId; }
    public BigDecimal getAmount() { return amount; }
    public TransactionType getType() { return type; }
    public TransactionStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setType(TransactionType type) { this.type = type; }
    public void setStatus(TransactionStatus status) { this.status = status; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
