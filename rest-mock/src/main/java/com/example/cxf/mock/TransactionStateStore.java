package com.example.cxf.mock;

import com.example.cxf.model.PaymentTransaction166;
import java.util.List;

public interface TransactionStateStore {
    PaymentTransaction166 getOrUpdate(String uetr);

    List<String> getActiveUetrs();

}
