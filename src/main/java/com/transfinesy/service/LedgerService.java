package com.transfinesy.service;

import com.transfinesy.model.*;
import com.transfinesy.repo.FineRepository;
import com.transfinesy.repo.FineRepositoryImpl;
import com.transfinesy.repo.PaymentRepository;
import com.transfinesy.repo.PaymentRepositoryImpl;
import com.transfinesy.repo.CommunityServiceRepository;
import com.transfinesy.repo.CommunityServiceRepositoryImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@Service
public class LedgerService {
    private FineRepository fineRepository;
    private PaymentRepository paymentRepository;
    private CommunityServiceRepository serviceRepository;
    private Stack<Transaction> recentTransactions;

    public LedgerService() {
        this.fineRepository = new FineRepositoryImpl();
        this.paymentRepository = new PaymentRepositoryImpl();
        this.serviceRepository = new CommunityServiceRepositoryImpl();
        this.recentTransactions = new Stack<>();
    }

    public Ledger getLedgerForStudent(String studID) {
        Ledger ledger = new Ledger(studID);

        List<Fine> fines = fineRepository.findByStudent(studID);
        for (Fine fine : fines) {
            ledger.addTransaction(fine);
            recentTransactions.push(fine);
        }

        List<Payment> payments = paymentRepository.findByStudent(studID);
        for (Payment payment : payments) {
            ledger.addTransaction(payment);
            recentTransactions.push(payment);
        }

        List<CommunityService> services = serviceRepository.findByStudent(studID);
        double totalServiceCredits = services.stream()
                .mapToDouble(CommunityService::getCreditAmount)
                .sum();
        ledger.setTotalServiceCredits(totalServiceCredits);

        for (CommunityService service : services) {
            String txnID = "SVC-TXN-" + service.getServiceID();
            String description = service.getDescription() != null && !service.getDescription().trim().isEmpty()
                ? "Community Service: " + service.getDescription()
                : "Community Service: " + service.getHoursRendered() + " hours";
            Payment serviceCredit = new Payment(
                "SVC-PAY-" + service.getServiceID(),
                txnID,
                studID,
                service.getCreditAmount(),
                description,
                service.getDate()
            );
            ledger.addTransaction(serviceCredit);
        }

        ledger.computeBalance();
        return ledger;
    }

    public double getBalanceForStudent(String studID) {
        Ledger ledger = getLedgerForStudent(studID);
        return ledger.getBalance();
    }

    public void addTransactionToLedger(String studID, Transaction transaction) {
    }

    public void addServiceCreditToLedger(String studID, double creditAmount, LocalDate date) {
    }

    public List<Transaction> getTransactionHistory(String studID, LocalDate start, LocalDate end) {
        Ledger ledger = getLedgerForStudent(studID);
        return ledger.filterTransactionsByDate(start, end);
    }

    public List<Transaction> getAllTransactions(String studID) {
        Ledger ledger = getLedgerForStudent(studID);
        return ledger.getTransactionHistory();
    }

    public List<Transaction> getRecentTransactions(int limit) {
        List<Transaction> recent = new ArrayList<>();
        Stack<Transaction> tempStack = new Stack<>();

        int count = 0;
        while (!recentTransactions.isEmpty() && count < limit) {
            Transaction txn = recentTransactions.pop();
            recent.add(txn);
            tempStack.push(txn);
            count++;
        }

        while (!tempStack.isEmpty()) {
            recentTransactions.push(tempStack.pop());
        }

        return recent;
    }

    public int getRecentTransactionsSize() {
        return recentTransactions.size();
    }
}
