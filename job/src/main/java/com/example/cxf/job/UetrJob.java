package com.example.cxf.job;

import com.example.cxf.model.PaymentTransaction166;
import com.example.cxf.service.TrackerService;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UetrJob {
    private final TrackerService service;
    private final Queue<String> queue = new ConcurrentLinkedQueue<>();
    private static final long DELAY_MS = 500; // 2 calls per second

    public UetrJob(TrackerService service) {
        this.service = service;
    }

    public void seedUetrs(String[] uetrs) {
        for (String uetr : uetrs) {
            queue.offer(uetr);
        }
    }

    public void run() {
        while (!queue.isEmpty()) {
            String uetr = queue.poll();
            if (uetr != null) {
                try {
                    System.out.println("Processing UETR: " + uetr);
                    PaymentTransaction166 tx = service.getTransaction(uetr);
                    if (tx != null) {
                        System.out.println("Successfully retrieved transaction: " + tx.getUETR());
                    } else {
                        System.out.println("Transaction not found for UETR: " + uetr);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing UETR " + uetr + ": " + e.getMessage());
                }

                // Rate limiting
                if (!queue.isEmpty()) {
                    try {
                        Thread.sleep(DELAY_MS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }

    public int getQueueSize() {
        return queue.size();
    }
}
