package com.example.cxf.mock;

import com.example.cxf.model.PaymentTransaction166;
import jakarta.ws.rs.NotFoundException;
import org.junit.Test;
import static org.junit.Assert.*;

public class TrackerApiServiceImplTest {

    private final TrackerApiServiceImpl service = new TrackerApiServiceImpl();

    @Test
    public void testGetTransaction_Success() {
        PaymentTransaction166 tx = service.getPaymentTransactionInfo("00f4be35-76f2-45c8-b4b3-565bbac5e86b");
        assertNotNull(tx);
        assertEquals("ACCC", tx.getTransactionStatus());
    }

    @Test(expected = NotFoundException.class)
    public void testGetTransaction_NotFound() {
        service.getPaymentTransactionInfo("invalid-uetr");
    }
}
