package com.transfinesy.repo;

import com.transfinesy.config.DBConfig;
import com.transfinesy.model.Fine;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FineRepositoryImpl implements FineRepository {

    @Override
    public List<Fine> findAll() {
        List<Fine> fines = new ArrayList<>();
        String sql = "SELECT fine_id, transaction_id, stud_id, event_id, amount, date FROM fines ORDER BY date DESC";

        try (Connection conn = DBConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Fine fine = new Fine(
                    rs.getString("fine_id"),
                    rs.getString("transaction_id"),
                    rs.getString("stud_id"),
                    rs.getString("event_id"),
                    rs.getDouble("amount"),
                    rs.getDate("date").toLocalDate()
                );
                fines.add(fine);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return fines;
    }

    @Override
    public Fine findById(String fineID) {
        String sql = "SELECT fine_id, transaction_id, stud_id, event_id, amount, date FROM fines WHERE fine_id = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, fineID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Fine(
                    rs.getString("fine_id"),
                    rs.getString("transaction_id"),
                    rs.getString("stud_id"),
                    rs.getString("event_id"),
                    rs.getDouble("amount"),
                    rs.getDate("date").toLocalDate()
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<Fine> findByStudent(String studID) {
        List<Fine> fines = new ArrayList<>();
        String sql = "SELECT fine_id, transaction_id, stud_id, event_id, amount, date FROM fines WHERE stud_id = ? ORDER BY date DESC";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studID);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Fine fine = new Fine(
                    rs.getString("fine_id"),
                    rs.getString("transaction_id"),
                    rs.getString("stud_id"),
                    rs.getString("event_id"),
                    rs.getDouble("amount"),
                    rs.getDate("date").toLocalDate()
                );
                fines.add(fine);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return fines;
    }

    @Override
    public List<Fine> findByEvent(String eventID) {
        List<Fine> fines = new ArrayList<>();
        String sql = "SELECT fine_id, transaction_id, stud_id, event_id, amount, date FROM fines WHERE event_id = ? ORDER BY date DESC";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, eventID);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Fine fine = new Fine(
                    rs.getString("fine_id"),
                    rs.getString("transaction_id"),
                    rs.getString("stud_id"),
                    rs.getString("event_id"),
                    rs.getDouble("amount"),
                    rs.getDate("date").toLocalDate()
                );
                fines.add(fine);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return fines;
    }

    @Override
    public void save(Fine f) {
        String sql = "INSERT INTO fines (fine_id, transaction_id, stud_id, event_id, amount, date) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, f.getFineID());
            pstmt.setString(2, f.getTransactionID());
            pstmt.setString(3, f.getStudID());
            pstmt.setString(4, f.getEventID());
            pstmt.setDouble(5, f.getFineAmount());
            pstmt.setDate(6, Date.valueOf(f.getDate()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save fine", e);
        }
    }

    @Override
    public void update(Fine f) {
        String sql = "UPDATE fines SET stud_id = ?, event_id = ?, amount = ?, date = ? WHERE fine_id = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, f.getStudID());
            pstmt.setString(2, f.getEventID());
            pstmt.setDouble(3, f.getFineAmount());
            pstmt.setDate(4, Date.valueOf(f.getDate()));
            pstmt.setString(5, f.getFineID());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update fine", e);
        }
    }

    @Override
    public void delete(String fineID) {
        String sql = "DELETE FROM fines WHERE fine_id = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, fineID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to delete fine", e);
        }
    }

    @Override
    public double getTotalSum() {
        String sql = "SELECT COALESCE(SUM(amount), 0) as total FROM fines";

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

    @Override
    public double getSumByEvent(String eventID) {
        String sql = "SELECT COALESCE(SUM(amount), 0) as total FROM fines WHERE event_id = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, eventID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0.0;
    }
}

