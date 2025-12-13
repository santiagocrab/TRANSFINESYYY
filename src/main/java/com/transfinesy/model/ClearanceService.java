package com.transfinesy.model;

public class ClearanceService {

    public boolean isEligibleForClearance(Student student, Ledger ledger) {
        if (student == null || ledger == null) {
            return false;
        }
        return ledger.getBalance() <= 0;
    }

    public String getClearanceStatus(Student student, Ledger ledger) {
        if (student == null || ledger == null) {
            return "UNKNOWN";
        }

        double balance = ledger.getBalance();
        if (balance <= 0) {
            return "CLEARED";
        } else if (balance > 0) {
            return "WITH BALANCE";
        } else {
            return "PENDING SERVICE";
        }
    }
}

