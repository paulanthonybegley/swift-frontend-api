package com.example.cxf.mock;

import com.example.cxf.api.TrackerFrontEndApi;
import com.example.cxf.model.PaymentTransaction166;

public class TrackerApiServiceImpl implements TrackerFrontEndApi {

    private final TransactionStateStore store;

    public TrackerApiServiceImpl(TransactionStateStore store) {
        this.store = store;
    }

    @Override
    public PaymentTransaction166 getPaymentTransactionInfo(String uetr) {
        return store.getOrUpdate(uetr);
    }
}
