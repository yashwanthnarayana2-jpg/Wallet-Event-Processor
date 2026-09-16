package com.example.wallet.service;

import com.example.wallet.dto.*;
import com.example.wallet.entity.*;
import com.example.wallet.enums.*;
import com.example.wallet.exception.InsufficientBalanceException;
import com.example.wallet.repository.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransactionService {
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public TransactionService(WalletRepository walletRepository,
                              TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public TransactionResponse processTransaction(TransactionRequest request) {
        var existing = transactionRepository.findByTransactionId(request.getTransactionId());
        if (existing.isPresent()) {
            return buildResponse(existing.get(), "Duplicate transaction. Original result returned.");
        }

        Wallet wallet = walletRepository.findByUserIdForUpdate(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        existing = transactionRepository.findByTransactionId(request.getTransactionId());
        if (existing.isPresent()) {
            return buildResponse(existing.get(), "Duplicate transaction. Original result returned.");
        }

        BigDecimal currentBalance = wallet.getBalance();

        if (request.getType() == TransactionType.DEBIT &&
                currentBalance.compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient wallet balance");
        }

        if (request.getType() == TransactionType.DEBIT) {
            wallet.setBalance(currentBalance.subtract(request.getAmount()));
        } else {
            wallet.setBalance(currentBalance.add(request.getAmount()));
        }
        walletRepository.save(wallet);

        Transaction transaction = new Transaction(
                request.getTransactionId(), request.getUserId(), request.getAmount(),
                request.getType(), TransactionStatus.SUCCESS, LocalDateTime.now());

        try {
            transactionRepository.saveAndFlush(transaction);
        } catch (DataIntegrityViolationException ex) {
            Transaction original = transactionRepository
                    .findByTransactionId(request.getTransactionId())
                    .orElseThrow(() -> ex);
            return buildResponse(original, "Duplicate transaction. Original result returned.");
        }

        return new TransactionResponse(
                transaction.getTransactionId(), transaction.getUserId(),
                transaction.getAmount(), transaction.getStatus(),
                "Transaction processed successfully", wallet.getBalance());
    }

    private TransactionResponse buildResponse(Transaction transaction, String message) {
        Wallet wallet = walletRepository.findById(transaction.getUserId()).orElse(null);
        BigDecimal balance = wallet == null ? null : wallet.getBalance();
        return new TransactionResponse(
                transaction.getTransactionId(), transaction.getUserId(),
                transaction.getAmount(), transaction.getStatus(), message, balance);
    }
}
