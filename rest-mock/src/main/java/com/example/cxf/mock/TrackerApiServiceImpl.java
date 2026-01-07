package com.example.cxf.mock;

import com.example.cxf.api.TrackerFrontEndApi;
import com.example.cxf.model.PaymentTransaction166;
import jakarta.ws.rs.NotFoundException;
import java.util.HashMap;
import java.util.Map;

public class TrackerApiServiceImpl implements TrackerFrontEndApi {

    private final Map<String, PaymentTransaction166> transactionDb = new HashMap<>();

    public TrackerApiServiceImpl() {
        // Seed data using UETR from examples
        PaymentTransaction166 tx = new PaymentTransaction166();
        tx.setUETR("00f4be35-76f2-45c8-b4b3-565bbac5e86b");
        tx.setTransactionStatus("ACCC");
        tx.setTransactionStatusDescription("Payment credited to beneficiary bank");
        transactionDb.put(tx.getUETR(), tx);
    }

    @Override
    public PaymentTransaction166 getPaymentTransactionInfo(String uetr) {
        if ("error".equals(uetr)) {
            throw new RuntimeException("Simulated Server Error");
        }
        PaymentTransaction166 tx = transactionDb.get(uetr);
        if (tx == null) {
            throw new NotFoundException("Transaction not found: " + uetr);
        }
        return tx;
    }
}
