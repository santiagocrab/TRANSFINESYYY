package com.transfinesy.repo;

import com.transfinesy.model.Payment;
import java.time.LocalDate;
import java.util.List;

public interface PaymentRepository {
    List<Payment> findAll();
    Payment findById(String paymentID);
    List<Payment> findByStudent(String studID);
    void save(Payment p);
    void update(Payment p);
    void delete(String paymentID);

    double getSumByDateRange(LocalDate startDate, LocalDate endDate);

    double getTotalSum();
}

