package com.example.cxf.job.visitor;

import com.example.cxf.model.*;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

/**
 * This test is specifically designed to demonstrate the full concatenated 
 * output of the visitors (Audit, AsciiDoc, and PlantUML) in the console.
 */
public class VisitorFullOutputTest {

    @Test
    public void demonstrateVisitorOutputs() {
        PaymentTransaction166 tx = createHighFidelityTransaction();

        System.out.println("============================================================");
        System.out.println("DEMONSTRATING VISITOR OUTPUTS (HIGH FIDELITY)");
        System.out.println("============================================================");

        // 1. Audit Output
        System.out.println("\n--- 1. AUDITING VISITOR OUTPUT ---");
        AuditingVisitor audit = new AuditingVisitor();
        audit.visit(tx);

        // 2. AsciiDoc Output
        System.out.println("\n--- 2. ASCIIDOC VISITOR OUTPUT ---");
        AsciiDocVisitor adoc = new AsciiDocVisitor();
        adoc.visit(tx);
        System.out.println(adoc.getAsciiDoc());

        // 3. PlantUML Output
        System.out.println("\n--- 3. PLANTUML VISITOR OUTPUT ---");
        PlantUmlVisitor puml = new PlantUmlVisitor();
        puml.visit(tx);
        System.out.println(puml.getPlantUml());

        System.out.println("============================================================");
    }

    private PaymentTransaction166 createHighFidelityTransaction() {
        PaymentTransaction166 tx = new PaymentTransaction166();
        tx.setUETR("00f4be35-76f2-45c8-b4b3-565bbac5e86b");
        tx.setTransactionStatus("ACCC");
        tx.setTransactionStatusDescription("Payment credited to beneficiary bank");
        tx.setTransactionStatusReason("Credited");
        tx.setTransactionInitiationDateTime("2025-05-23T10:00:40Z");
        tx.setTransactionLastUpdateDateTime("2025-05-23T10:05:40Z");
        tx.setTransactionCompletionDateTime("2025-05-23T10:05:40Z");
        
        List<TransactionRouting1> routing = new ArrayList<>();
        routing.add(new TransactionRouting1().from("BANKBEBICXX").to("BANKUSBICXX"));
        routing.add(new TransactionRouting1().from("BANKUSBICXX").to("BANKFRBICXX"));
        tx.setTransactionRouting(routing);

        tx.setTransactionInstructedAmount(new PaymentTransaction166TransactionInstructedAmount()
                .currency("USD").amount("100000"));
        tx.setTransactionConfirmedAmount(new PaymentTransaction166TransactionConfirmedAmount()
                .currency("USD").amount("99550"));
        
        return tx;
    }
}
