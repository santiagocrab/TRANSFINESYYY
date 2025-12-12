package com.transfinesy.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Ledger {
    private String studID;
    private List<Transaction> transactions;
    private double openingBalance;
    private double closingBalance;
    private LocalDateTime lastUpdated;
    private double totalFines;
    private double totalPayments;
    private double totalServiceCredits;

    public Ledger() {
        this.transactions = new ArrayList<>();
        this.openingBalance = 0.0;
        this.closingBalance = 0.0;
        this.lastUpdated = LocalDateTime.now();
    }

    public Ledger(String studID) {
        this();
        this.studID = studID;
    }

    public void addTransaction(Transaction t) {
        if (t != null) {
            transactions.add(t);
            updateClosingBalance();
            updateTotals();
            lastUpdated = LocalDateTime.now();
        }
    }

    public double computeBalance() {
        double totalFines = 0.0;
        double totalPayments = 0.0;
        double totalCredits = 0.0;

        for (Transaction t : transactions) {
            if (t instanceof Fine) {
                totalFines += Math.abs(t.getAmount());
            } else if (t instanceof Payment) {
                if (t.getTransactionID() != null && t.getTransactionID().startsWith("SVC-TXN-")) {
                    totalCredits += Math.abs(t.getAmount());
                } else {
                    totalPayments += Math.abs(t.getAmount());
                }
            }
        }

        double balance = openingBalance + totalFines - totalPayments - totalCredits;
        closingBalance = Math.max(0.0, balance);

        this.totalFines = totalFines;
        this.totalPayments = totalPayments;
        this.totalServiceCredits = totalCredits;

        return closingBalance;
    }

    public double getBalance() {
        return Math.max(0.0, closingBalance);
    }

    public double getTotalFines() {
        return totalFines;
    }

    public double getTotalPayments() {
        return totalPayments;
    }

    public double getTotalCredits() {
        return totalServiceCredits;
    }

    public void updateClosingBalance() {
        computeBalance();
    }

    private void updateTotals() {
        totalFines = 0.0;
        totalPayments = 0.0;
        totalServiceCredits = 0.0;

        for (Transaction t : transactions) {
            if (t instanceof Fine) {
                totalFines += Math.abs(t.getAmount());
            } else if (t instanceof Payment) {
                totalPayments += t.getAmount();
            }
        }
    }

    public List<Transaction> filterTransactionsByDate(LocalDate start, LocalDate end) {
        return transactions.stream()
                .filter(t -> !t.getDate().isBefore(start) && !t.getDate().isAfter(end))
                .collect(Collectors.toList());
    }

    public List<Transaction> getTransactionHistory() {
        return new ArrayList<>(transactions);
    }

    public String getStudID() {
        return studID;
    }

    public void setStudID(String studID) {
        this.studID = studID;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions != null ? transactions : new ArrayList<>();
        updateClosingBalance();
        updateTotals();
    }

    public double getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(double openingBalance) {
        this.openingBalance = openingBalance;
        updateClosingBalance();
    }

    public double getClosingBalance() {
        return closingBalance;
    }

    public void setClosingBalance(double closingBalance) {
        this.closingBalance = closingBalance;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public double getTotalServiceCredits() {
        return totalServiceCredits;
    }

    public void setTotalServiceCredits(double totalServiceCredits) {
        this.totalServiceCredits = totalServiceCredits;
    }
}
