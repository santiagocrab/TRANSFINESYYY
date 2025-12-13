package com.transfinesy.service;

import com.transfinesy.model.Ledger;
import com.transfinesy.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClearanceService {
    private LedgerService ledgerService;

    public ClearanceService() {
        this.ledgerService = new LedgerService();
    }

    @Autowired
    public ClearanceService(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    public boolean isEligibleForClearance(Student student) {
        if (student == null) {
            return false;
        }
        Ledger ledger = ledgerService.getLedgerForStudent(student.getStudID());
        return ledger.getBalance() <= 0;
    }

    public String getClearanceStatus(Student student) {
        if (student == null) {
            return "UNKNOWN";
        }
        Ledger ledger = ledgerService.getLedgerForStudent(student.getStudID());
        double balance = ledger.getBalance();

        if (balance <= 0) {
            return "CLEARED";
        } else {
            return "WITH BALANCE";
        }
    }

    public String getClearanceStatusWithBalance(Student student) {
        if (student == null) {
            return "UNKNOWN";
        }
        Ledger ledger = ledgerService.getLedgerForStudent(student.getStudID());
        double balance = ledger.getBalance();

        if (balance <= 0) {
            return "CLEARED";
        } else {
            return String.format("WITH BALANCE (₱%.2f)", balance);
        }
    }
}

