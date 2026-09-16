package com.example.wallet.controller;

import com.example.wallet.dto.*;
import com.example.wallet.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/process")
    public ResponseEntity<TransactionResponse> process(@Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.processTransaction(request));
    }
}
