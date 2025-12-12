package com.transfinesy.service;

import org.springframework.stereotype.Service;

import com.transfinesy.model.Payment;
import com.transfinesy.repo.PaymentRepository;
import com.transfinesy.repo.PaymentRepositoryImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {
    private PaymentRepository repository;
    private LedgerService ledgerService;

    public PaymentService() {
        this.repository = new PaymentRepositoryImpl();
        this.ledgerService = new LedgerService();
    }

    public PaymentService(LedgerService ledgerService) {
        this.repository = new PaymentRepositoryImpl();
        this.ledgerService = ledgerService;
    }

    public void recordPayment(String studID, double amount, String orNumber, LocalDate date) {
        if (studID == null || studID.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID is required");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
        if (orNumber == null || orNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("OR Number is required");
        }

        if (!orNumber.matches("^\\d+$")) {
            throw new IllegalArgumentException("OR Number must contain digits only");
        }

        double remainingBalance = ledgerService.getBalanceForStudent(studID);
        if (amount > remainingBalance) {
            throw new IllegalArgumentException(
                "Payment amount exceeds the remaining balance. The remaining balance is only ₱" + 
                String.format("%.2f", remainingBalance) + "."
            );
        }

        String paymentID = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String transactionID = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Payment payment = new Payment(paymentID, transactionID, studID, amount, orNumber, date);
        repository.save(payment);
        ledgerService.addTransactionToLedger(studID, payment);
    }

    public void updatePayment(String paymentID, String studID, double amount, String orNumber, LocalDate date) {
        if (paymentID == null || paymentID.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment ID is required");
        }
        if (studID == null || studID.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID is required");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
        if (orNumber == null || orNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("OR Number is required");
        }

        if (!orNumber.matches("^\\d+$")) {
            throw new IllegalArgumentException("OR Number must contain digits only");
        }

        Payment existingPayment = repository.findById(paymentID);
        if (existingPayment == null) {
            throw new IllegalArgumentException("Payment not found with ID: " + paymentID);
        }

        double remainingBalance = ledgerService.getBalanceForStudent(studID);
        double oldPaymentAmount = existingPayment.getAmount();
        double adjustedBalance = remainingBalance + oldPaymentAmount;
        
        if (amount > adjustedBalance) {
            throw new IllegalArgumentException(
                "Payment amount exceeds the remaining balance. The remaining balance is only ₱" + 
                String.format("%.2f", adjustedBalance) + "."
            );
        }

        existingPayment.setStudID(studID);
        existingPayment.setAmount(amount);
        existingPayment.setOrNumber(orNumber);
        existingPayment.setDate(date);

        repository.update(existingPayment);

    }

    public void deletePayment(String paymentID) {
        if (paymentID == null || paymentID.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment ID is required");
        }

        Payment payment = repository.findById(paymentID);
        if (payment == null) {
            throw new IllegalArgumentException("Payment not found with ID: " + paymentID);
        }

        repository.delete(paymentID);

    }

    public List<Payment> getPaymentsByStudent(String studID) {
        return repository.findByStudent(studID);
    }

    public Payment getPaymentById(String paymentID) {
        return repository.findById(paymentID);
    }

    public List<Payment> getAllPayments() {
        return repository.findAll();
    }

}

