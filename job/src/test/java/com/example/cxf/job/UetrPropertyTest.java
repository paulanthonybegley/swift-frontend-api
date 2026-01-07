package com.example.cxf.job;

import com.example.cxf.model.PaymentTransaction166;
import com.example.cxf.service.TrackerService;
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
        TrackerService service = mock(TrackerService.class);
        // Simulate some successes and some errors
        when(service.getTransaction(anyString())).thenAnswer(invocation -> {
            String arg = invocation.getArgument(0);
            if (arg.contains("fail")) {
                throw new RuntimeException("Induced failure");
            }
            return new PaymentTransaction166();
        });

        UetrJob job = new UetrJob(service);
        job.seedUetrs(uetrs.toArray(new String[0]));

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
