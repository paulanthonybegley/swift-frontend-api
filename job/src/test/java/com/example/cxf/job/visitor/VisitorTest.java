package com.example.cxf.job.visitor;

import com.example.cxf.model.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class VisitorTest {

    @Test
    public void testAuditingVisitor() {
        PaymentTransaction166 tx = createSampleTransaction();
        AuditingVisitor visitor = new AuditingVisitor();
        
        // Redirect stdout to capture output if needed, but for now we just run it
        visitor.visit(tx);
    }

    @Test
    public void testAsciiDocVisitor() {
        PaymentTransaction166 tx = createSampleTransaction();
        AsciiDocVisitor visitor = new AsciiDocVisitor();
        visitor.visit(tx);
        
        String output = visitor.getAsciiDoc();
        System.out.println("AsciiDoc Output:\n" + output);
        
        assertTrue(output.contains("= Payment Transaction: 00f4be35"));
        assertTrue(output.contains("== Status Information"));
        assertTrue(output.contains("| FROMBICXX | TOBICXX"));
    }

    @Test
    public void testPlantUmlVisitor() {
        PaymentTransaction166 tx = createSampleTransaction();
        PlantUmlVisitor visitor = new PlantUmlVisitor();
        visitor.visit(tx);
        
        String output = visitor.getPlantUml();
        System.out.println("PlantUML Output:\n" + output);
        
        assertTrue(output.contains("@startuml"));
        assertTrue(output.contains("\"FROMBICXX\" -> \"TOBICXX\" : Settlement"));
        assertTrue(output.contains("@enduml"));
    }

    private PaymentTransaction166 createSampleTransaction() {
        PaymentTransaction166 tx = new PaymentTransaction166();
        tx.setUETR("00f4be35-76f2-45c8-b4b3-565bbac5e86b");
        tx.setTransactionStatus("ACCC");
        tx.setTransactionStatusDescription("AcceptedSettlementCompleted");
        
        List<TransactionRouting1> routingList = new ArrayList<>();
        TransactionRouting1 r1 = new TransactionRouting1();
        r1.setFrom("FROMBICXX");
        r1.setTo("TOBICXX");
        routingList.add(r1);
        
        tx.setTransactionRouting(routingList);
        return tx;
    }
}
