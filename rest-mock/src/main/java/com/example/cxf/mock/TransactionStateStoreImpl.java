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
        tx.setTransactionInitiationDateTime("2026-01-07T14:50:00Z");
        tx.setTransactionLastUpdateDateTime("2026-01-07T14:55:00Z");

        List<TransactionRouting1> routing = new ArrayList<>();
        routing.add(new TransactionRouting1().from("BANKBEBICXX").to("BANKUSBICXX"));
        tx.setTransactionRouting(routing);

        tx.setTransactionInstructedAmount(new PaymentTransaction166TransactionInstructedAmount()
                .currency("USD").amount("1000.00"));

        if (currentCall == 1) {
            tx.setTransactionStatus("INIT");
            tx.setTransactionStatusDescription("Initial Status");
        } else if (currentCall <= 3) {
            tx.setTransactionStatus("PDNG");
            tx.setTransactionStatusDescription("Payment Pending");
        } else {
            tx.setTransactionStatus("ACCC");
            tx.setTransactionStatusDescription("Settlement Completed");
            tx.setTransactionCompletionDateTime("2026-01-07T15:00:00Z");
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
