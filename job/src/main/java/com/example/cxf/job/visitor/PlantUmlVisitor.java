package com.example.cxf.job.visitor;

import com.example.cxf.model.*;
import java.io.StringWriter;
import java.io.PrintWriter;

public class PlantUmlVisitor implements PaymentVisitor {
    private final StringWriter sw = new StringWriter();
    private final PrintWriter pw = new PrintWriter(sw);

    @Override
    public void visit(PaymentTransaction166 tx) {
        pw.println("@startuml");
        pw.println("title Transaction Flow: " + tx.getUETR());
        pw.println("autonumber");
        if (tx.getTransactionRouting() != null) {
            for (TransactionRouting1 routing : tx.getTransactionRouting()) {
                visit(routing);
            }
        }
        pw.println("@enduml");
    }

    @Override
    public void visit(TransactionRouting1 routing) {
        String from = routing.getFrom();
        String to = routing.getTo();
        if (from != null && to != null) {
            pw.println("\"" + from + "\" -> \"" + to + "\" : Settlement");
        }
    }

    public String getPlantUml() {
        return sw.toString();
    }
}
