package com.example.cxf.mock;

import com.example.cxf.model.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TransactionStateStoreImpl implements TransactionStateStore {
    private final Map<String, AtomicInteger> callCounts = new ConcurrentHashMap<>();

    @Override
    public PaymentTransaction166 getOrUpdate(String uetr) {
        AtomicInteger count = callCounts.computeIfAbsent(uetr, k -> new AtomicInteger(0));
        int currentCall = count.incrementAndGet();

        PaymentTransaction166 tx = new PaymentTransaction166();
        tx.setUETR(uetr);
        tx.setTransactionInitiationDateTime("2025-05-23T10:00:40Z");

        List<TransactionRouting1> routing = new ArrayList<>();
        routing.add(new TransactionRouting1().from("BANKBEBICXX").to("BANKUSBICXX"));

        tx.setTransactionInstructedAmount(new PaymentTransaction166TransactionInstructedAmount()
                .currency("USD").amount("100000"));

        if (currentCall == 1) {
            tx.setTransactionStatus("INIT");
            tx.setTransactionStatusDescription("Initial Status");
            tx.setTransactionStatusReason("Initialized");
            tx.setTransactionLastUpdateDateTime("2025-05-23T10:00:40Z");
            tx.setTransactionRouting(routing);
        } else if (currentCall <= 3) {
            tx.setTransactionStatus("PDNG");
            tx.setTransactionStatusDescription("Payment Pending");
            tx.setTransactionStatusReason("Processing");
            tx.setTransactionLastUpdateDateTime("2025-05-23T10:02:40Z");
            tx.setTransactionRouting(routing);
        } else {
            tx.setTransactionStatus("ACCC");
            tx.setTransactionStatusDescription("Payment credited to beneficiary bank");
            tx.setTransactionStatusReason("Credited");
            tx.setTransactionLastUpdateDateTime("2025-05-23T10:05:40Z");
            tx.setTransactionCompletionDateTime("2025-05-23T10:05:40Z");

            // Add the second hop for ACCC
            routing.add(new TransactionRouting1().from("BANKUSBICXX").to("BANKFRBICXX"));
            tx.setTransactionRouting(routing);

            tx.setTransactionConfirmedAmount(new PaymentTransaction166TransactionConfirmedAmount()
                    .currency("USD").amount("99550"));
        }

        return tx;
    }

    @Override
    public List<String> getActiveUetrs() {
        return callCounts.entrySet().stream()
                .filter(e -> {
                    // To check if status is ACCC, we'd need more logic or a separate state map.
                    // For simplicity, we'll re-check the count in this mock logic.
                    return e.getValue().get() < 4;
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
