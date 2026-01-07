package com.example.cxf.job;

import com.example.cxf.model.PaymentTransaction166;
import com.example.cxf.service.TrackerService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UetrJobTest {

    @Test
    public void testProcessTenUetrs() {
        TrackerService service = mock(TrackerService.class);
        when(service.getTransaction(anyString())).thenReturn(new PaymentTransaction166());

        UetrJob job = new UetrJob(service);
        String[] uetrs = new String[10];
        for (int i = 0; i < 10; i++) {
            uetrs[i] = "uetr-" + i;
        }
        job.seedUetrs(uetrs);
        
        long start = System.currentTimeMillis();
        job.run();
        long end = System.currentTimeMillis();

        // 10 items, 9 delays of 500ms = 4500ms minimum
        long duration = end - start;
        System.out.println("Duration: " + duration + "ms");
        
        verify(service, times(10)).getTransaction(anyString());
        assertEquals(0, job.getQueueSize(), "Queue should be empty after run");
        assert(duration >= 4500);
    }

    @Test
    public void testRemovalOnFailure() {
        TrackerService service = mock(TrackerService.class);
        when(service.getTransaction(anyString())).thenThrow(new RuntimeException("API Error"));

        UetrJob job = new UetrJob(service);
        job.seedUetrs(new String[]{"error-uetr"});
        
        job.run();
        
        verify(service, times(1)).getTransaction("error-uetr");
        assertEquals(0, job.getQueueSize(), "Queue should be empty even on failure");
    }
}
