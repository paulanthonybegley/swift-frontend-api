package com.example.cxf.mock;

import com.example.cxf.model.PaymentTransaction166;
import org.junit.Test;
import static org.junit.Assert.*;

public class TrackerApiServiceImplTest {

    private final TrackerApiServiceImpl service = new TrackerApiServiceImpl(new TransactionStateStoreImpl());

    @Test
    public void testGetTransaction_Lifecycle() {
        String uetr = "00f4be35-76f2-45c8-b4b3-565bbac5e86b";

        // 1st call: INIT
        PaymentTransaction166 tx1 = service.getPaymentTransactionInfo(uetr);
        assertNotNull(tx1);
        assertEquals("INIT", tx1.getTransactionStatus());

        // 2nd call: PDNG
        PaymentTransaction166 tx2 = service.getPaymentTransactionInfo(uetr);
        assertEquals("PDNG", tx2.getTransactionStatus());

        // 4th call: ACCC
        service.getPaymentTransactionInfo(uetr); // 3rd
        PaymentTransaction166 tx4 = service.getPaymentTransactionInfo(uetr);
        assertEquals("ACCC", tx4.getTransactionStatus());
    }
}
