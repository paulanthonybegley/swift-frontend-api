package com.example.cxf.job.visitor;

import com.example.cxf.model.*;

public class AuditingVisitor implements PaymentVisitor {
    
    @Override
    public void visit(PaymentTransaction166 tx) {
        System.out.println("[AUDIT] Transaction ID: " + tx.getUETR());
        System.out.println("[AUDIT] Status: " + tx.getTransactionStatus());
        if (tx.getTransactionRouting() != null) {
            for (TransactionRouting1 routing : tx.getTransactionRouting()) {
                visit(routing);
            }
        }
    }

    @Override
    public void visit(TransactionRouting1 routing) {
        String from = redact(routing.getFrom());
        String to = redact(routing.getTo());
        System.out.println("[AUDIT] Routing: " + from + " -> " + to);
    }

    private String redact(String value) {
        if (value == null || value.length() < 4) return "****";
        return value.substring(0, 4) + "****";
    }
}
