package com.example.cxf.job.visitor;

import com.example.cxf.model.*;
import java.io.StringWriter;
import java.io.PrintWriter;

public class AsciiDocVisitor implements PaymentVisitor {
    private final StringWriter sw = new StringWriter();
    private final PrintWriter pw = new PrintWriter(sw);

    @Override
    public void visit(PaymentTransaction166 tx) {
        pw.println("= Payment Transaction: " + tx.getUETR());
        pw.println(":toc:");
        pw.println();
        pw.println("== Status Information");
        pw.println("* Status: " + tx.getTransactionStatus());
        pw.println("* Description: " + tx.getTransactionStatusDescription());
        pw.println();
        pw.println("== Routing Details");
        pw.println("[cols=\"1,1\", options=\"header\"]");
        pw.println("|===");
        pw.println("| From | To");
        if (tx.getTransactionRouting() != null) {
            for (TransactionRouting1 routing : tx.getTransactionRouting()) {
                visit(routing);
            }
        }
        pw.println("|===");
    }

    @Override
    public void visit(TransactionRouting1 routing) {
        pw.println("| " + routing.getFrom() + " | " + (routing.getTo() != null ? routing.getTo() : "N/A"));
    }

    public String getAsciiDoc() {
        return sw.toString();
    }
}
