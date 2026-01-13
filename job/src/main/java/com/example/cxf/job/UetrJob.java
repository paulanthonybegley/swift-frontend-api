package com.example.cxf.job;

import com.example.cxf.model.PaymentTransaction166;
import com.example.cxf.service.UetrProcessor;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UetrJob {
    private final UetrProcessor service;
    private final Queue<String> queue = new ConcurrentLinkedQueue<>();
    private static final long DELAY_MS = 500; // 2 calls per second
    private static final long SCHEDULE_INTERVAL_MS = 60000; // 1 minute
    private boolean continuous = false;

    public UetrJob(UetrProcessor service) {
        this.service = service;
    }

    public void setContinuous(boolean continuous) {
        this.continuous = continuous;
    }

    public void offer(String uetr) {
        if (uetr != null && !uetr.isBlank()) {
            this.queue.offer(uetr);
        }
    }

    public void run() {
        do {
            String[] uetrs = service.loadUetrs();
            if (uetrs != null) {
                for (String uetr : uetrs) {
                    queue.offer(uetr);
                }
            }

            while (!queue.isEmpty()) {
                String uetr = queue.poll();
                if (uetr != null) {
                    try {
                        System.out.println("Processing UETR: " + uetr);
                        PaymentTransaction166 tx = service.getTransaction(uetr);
                        if (tx != null) {
                            System.out.println("Successfully retrieved transaction: " + tx.getUETR() + " (Status: "
                                    + tx.getTransactionStatus() + ")");

                            // Apply Visitors
                            new com.example.cxf.job.visitor.AuditingVisitor().visit(tx);

                            com.example.cxf.job.visitor.AsciiDocVisitor adoc = new com.example.cxf.job.visitor.AsciiDocVisitor();
                            adoc.visit(tx);
                            // System.out.println("--- Generated AsciiDoc ---\n" + adoc.getAsciiDoc() +
                            // "\n--------------------------");

                            com.example.cxf.job.visitor.PlantUmlVisitor puml = new com.example.cxf.job.visitor.PlantUmlVisitor();
                            puml.visit(tx);
                            // System.out.println("--- Generated PlantUML ---\n" + puml.getPlantUml() +
                            // "\n--------------------------");

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
                            return;
                        }
                    }
                }
            }

            if (continuous) {
                System.out.println("Scheduling wait: Finished pass, waiting 60 seconds before next discovery...");
                try {
                    Thread.sleep(SCHEDULE_INTERVAL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } while (continuous);
    }

    public int getQueueSize() {
        return queue.size();
    }
}
