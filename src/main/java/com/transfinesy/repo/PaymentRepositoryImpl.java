package com.transfinesy.repo;

import com.transfinesy.config.DBConfig;
import com.transfinesy.model.Payment;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PaymentRepositoryImpl implements PaymentRepository {

    @Override
    public List<Payment> findAll() {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT payment_id, transaction_id, stud_id, amount, or_number, date FROM payments ORDER BY date DESC";

        try (Connection conn = DBConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Payment payment = new Payment(
                    rs.getString("payment_id"),
                    rs.getString("transaction_id"),
                    rs.getString("stud_id"),
                    rs.getDouble("amount"),
                    rs.getString("or_number"),
                    rs.getDate("date").toLocalDate()
                );
                payments.add(payment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return payments;
    }

    @Override
    public Payment findById(String paymentID) {
        String sql = "SELECT payment_id, transaction_id, stud_id, amount, or_number, date FROM payments WHERE payment_id = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, paymentID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Payment(
                    rs.getString("payment_id"),
                    rs.getString("transaction_id"),
                    rs.getString("stud_id"),
                    rs.getDouble("amount"),
                    rs.getString("or_number"),
                    rs.getDate("date").toLocalDate()
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<Payment> findByStudent(String studID) {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT payment_id, transaction_id, stud_id, amount, or_number, date FROM payments WHERE stud_id = ? ORDER BY date DESC";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studID);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Payment payment = new Payment(
                    rs.getString("payment_id"),
                    rs.getString("transaction_id"),
                    rs.getString("stud_id"),
                    rs.getDouble("amount"),
                    rs.getString("or_number"),
                    rs.getDate("date").toLocalDate()
                );
                payments.add(payment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return payments;
    }

    @Override
    public void save(Payment p) {
        String sql = "INSERT INTO payments (payment_id, transaction_id, stud_id, amount, or_number, date) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, p.getPaymentID());
            pstmt.setString(2, p.getTransactionID());
            pstmt.setString(3, p.getStudID());
            pstmt.setDouble(4, p.getAmount());
            pstmt.setString(5, p.getOrNumber());
            pstmt.setDate(6, Date.valueOf(p.getDate()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save payment", e);
        }
    }

    @Override
    public void update(Payment p) {
        String sql = "UPDATE payments SET stud_id = ?, amount = ?, or_number = ?, date = ? WHERE payment_id = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, p.getStudID());
            pstmt.setDouble(2, p.getAmount());
            pstmt.setString(3, p.getOrNumber());
            pstmt.setDate(4, Date.valueOf(p.getDate()));
            pstmt.setString(5, p.getPaymentID());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update payment", e);
        }
    }

    @Override
    public void delete(String paymentID) {
        String sql = "DELETE FROM payments WHERE payment_id = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, paymentID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to delete payment", e);
        }
    }

    @Override
    public double getSumByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COALESCE(SUM(amount), 0) as total FROM payments WHERE date >= ? AND date <= ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    @Override
    public double getTotalSum() {
        String sql = "SELECT COALESCE(SUM(amount), 0) as total FROM payments";

        try (Connection conn = DBConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0.0;
    }
}

