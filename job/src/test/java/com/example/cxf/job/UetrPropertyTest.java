package com.example.cxf.job;

import com.example.cxf.model.PaymentTransaction166;
import com.example.cxf.service.UetrProcessor;
import net.jqwik.api.*;
import net.jqwik.api.constraints.Size;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UetrPropertyTest {

    @Property(tries = 20)
    void verifyQueueClearsAndRateLimit(@ForAll @Size(max = 20) List<String> uetrs) {
        UetrProcessor service = mock(UetrProcessor.class);
        when(service.loadUetrs()).thenReturn(uetrs.toArray(new String[0]));
        // Simulate some successes and some errors
        when(service.getTransaction(anyString())).thenAnswer(invocation -> {
            String arg = invocation.getArgument(0);
            if (arg.contains("fail")) {
                throw new RuntimeException("Induced failure");
            }
            PaymentTransaction166 tx = new PaymentTransaction166();
            tx.setUETR(arg);
            tx.setTransactionStatus("PDNG");
            tx.setTransactionStatusDescription("Processing...");
            tx.addTransactionRoutingItem(new com.example.cxf.model.TransactionRouting1().from("SENDER").to("RECEIVER"));
            return tx;
        });

        UetrJob job = new UetrJob(service);

        long start = System.currentTimeMillis();
        job.run();
        long end = System.currentTimeMillis();

        verify(service, times(uetrs.size())).getTransaction(anyString());
        assertEquals(0, job.getQueueSize(), "Queue must be empty after processing " + uetrs.size() + " items");

        if (uetrs.size() > 1) {
            long expectedMinDuration = (uetrs.size() - 1) * 500L;
            long actualDuration = end - start;
            assert actualDuration >= expectedMinDuration : "Rate limit not respected: " + actualDuration + "ms < " + expectedMinDuration + "ms";
        }
    }
}
