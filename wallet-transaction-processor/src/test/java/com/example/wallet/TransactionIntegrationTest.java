package com.example.wallet;

import com.example.wallet.dto.TransactionRequest;
import com.example.wallet.entity.Wallet;
import com.example.wallet.enums.TransactionType;
import com.example.wallet.repository.TransactionRepository;
import com.example.wallet.repository.WalletRepository;
import com.example.wallet.service.TransactionService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TransactionIntegrationTest {

    @Autowired TransactionService transactionService;
    @Autowired WalletRepository walletRepository;
    @Autowired TransactionRepository transactionRepository;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        walletRepository.saveAndFlush(new Wallet(userId, new BigDecimal("1000.00")));
    }

    @Test
    @Order(1)
    @DisplayName("Processes a single valid debit transaction successfully.")
    void happyPath() {
        System.out.println("\n[HAPPY PATH] Processing one valid debit transaction");
        var response = transactionService.processTransaction(
                request(UUID.randomUUID(), userId, "250.00"));
        Wallet wallet = walletRepository.findById(userId).orElseThrow();

        assertEquals(new BigDecimal("750.00"), wallet.getBalance());
        assertEquals("SUCCESS", response.getStatus().name());

        System.out.println("[RESULT] Balance = " + wallet.getBalance());
    }

    @Test
    @Order(2)
    @DisplayName("Sends 3 identical transactionIDs simultaneously. Ensures the balance is only deducted once.")
    void idempotency() throws Exception {
        System.out.println("\n[IDEMPOTENCY] Sending 3 identical transaction IDs");
        UUID transactionId = UUID.randomUUID();
        TransactionRequest request = request(transactionId, userId, "250.00");

        ExecutorService executor = Executors.newFixedThreadPool(3);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            futures.add(executor.submit(() -> {
                try {
                    start.await();
                    return transactionService.processTransaction(request);
                } catch (Exception ex) {
                    return ex;
                }
            }));
        }
        start.countDown();
        for (Future<?> future : futures) {
            System.out.println("[REQUEST RESULT] " + future.get().getClass().getSimpleName());
        }
        executor.shutdown();

        Wallet wallet = walletRepository.findById(userId).orElseThrow();
        assertEquals(new BigDecimal("750.00"), wallet.getBalance());
        assertEquals(1, transactionRepository.count());

        System.out.println("[RESULT] Balance = " + wallet.getBalance());
        System.out.println("[RESULT] Transaction records = " + transactionRepository.count());
    }

    @Test
    @Order(3)
    @DisplayName("Sends 10 concurrent debit requests of ₹100 for a wallet with a ₹500 balance. Ensures the final balance is exactly ₹0 and 5 requests fail with insufficient funds.")
    void raceCondition() throws Exception {
        System.out.println("\n[RACE CONDITION] Sending 10 concurrent ₹100 debits against ₹500");

        walletRepository.deleteAll();
        userId = UUID.randomUUID();
        walletRepository.saveAndFlush(new Wallet(userId, new BigDecimal("500.00")));

        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            UUID transactionId = UUID.randomUUID();
            futures.add(executor.submit(() -> {
                start.await();
                try {
                    transactionService.processTransaction(
                            request(transactionId, userId, "100.00"));
                    return true;
                } catch (Exception ex) {
                    return false;
                }
            }));
        }

        start.countDown();

        int success = 0;
        int failure = 0;
        for (Future<Boolean> future : futures) {
            if (future.get()) success++;
            else failure++;
        }
        executor.shutdown();

        Wallet wallet = walletRepository.findById(userId).orElseThrow();
        assertEquals(new BigDecimal("0.00"), wallet.getBalance());
        assertEquals(5, success);
        assertEquals(5, failure);

        System.out.println("[RESULT] Successful requests = " + success);
        System.out.println("[RESULT] Failed requests = " + failure);
        System.out.println("[RESULT] Final balance = " + wallet.getBalance());
    }

    private TransactionRequest request(UUID transactionId, UUID userId, String amount) {
        TransactionRequest request = new TransactionRequest();
        request.setTransactionId(transactionId);
        request.setUserId(userId);
        request.setAmount(new BigDecimal(amount));
        request.setType(TransactionType.DEBIT);
        return request;
    }
}
