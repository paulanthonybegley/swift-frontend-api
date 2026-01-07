package com.example.cxf.job.visitor;

import com.example.cxf.model.*;

public interface PaymentVisitor {
    void visit(PaymentTransaction166 tx);
    void visit(TransactionRouting1 routing);
}
