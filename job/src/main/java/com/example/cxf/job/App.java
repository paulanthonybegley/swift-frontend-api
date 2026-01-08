package com.example.cxf.job;

import com.example.cxf.service.LoggingUetrProcessor;
import com.example.cxf.service.TrackerService;
import com.example.cxf.service.UetrProcessor;

public class App {
    public static void main(String[] args) {
        String url = System.getProperty("tracker.url", "http://localhost:9000/");
        String user = System.getProperty("tracker.user", "admin");
        String pass = System.getProperty("tracker.pass", "password");

        System.out.println("Starting Job Processor...");
        System.out.println("Target URL: " + url);
        System.out.println("User      : " + user);

        try {
            // 1. Initialize the Base Service
            UetrProcessor baseService = new TrackerService(url, user, pass);

            // 2. Wrap with Logging Decorator
            UetrProcessor loggingService = new LoggingUetrProcessor(baseService);

            // 3. Initialize and Run the Job
            UetrJob job = new UetrJob(loggingService);
            
            // 4. Seeding: If loadUetrs returns nothing (empty store), 
            // seed with demonstration UETRs so the job has work to do.
            job.offer("00f4be35-76f2-45c8-b4b3-565bbac5e86b");
            job.offer("eec67498-8422-4a00-9844-325d7e305e75");

            job.run();

            System.out.println("Job Processor completed successfully.");
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR: Job Processor failed.");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
