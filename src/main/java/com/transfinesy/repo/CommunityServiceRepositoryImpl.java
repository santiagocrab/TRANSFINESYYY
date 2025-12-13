package com.transfinesy.repo;

import com.transfinesy.config.DBConfig;
import com.transfinesy.model.CommunityService;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CommunityServiceRepositoryImpl implements CommunityServiceRepository {

    @Override
    public List<CommunityService> findAll() {
        List<CommunityService> services = new ArrayList<>();
        String sql = "SELECT service_id, stud_id, hours_rendered, credit_amount, date, description FROM community_service ORDER BY date DESC";

        try (Connection conn = DBConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                CommunityService service = new CommunityService(
                    rs.getString("service_id"),
                    rs.getString("stud_id"),
                    rs.getInt("hours_rendered"),
                    rs.getDouble("credit_amount"),
                    rs.getDate("date").toLocalDate()
                );

                String description = null;
                try {
                    description = rs.getString("description");
                } catch (Exception e) {

                }
                service.setDescription(description);
                services.add(service);
            }
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to fetch community services. Table might not exist.");
            System.err.println("SQL Error: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            e.printStackTrace();

        }

        return services;
    }

    @Override
    public CommunityService findById(String serviceID) {
        String sql = "SELECT service_id, stud_id, hours_rendered, credit_amount, date, description FROM community_service WHERE service_id = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, serviceID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                CommunityService service = new CommunityService(
                    rs.getString("service_id"),
                    rs.getString("stud_id"),
                    rs.getInt("hours_rendered"),
                    rs.getDouble("credit_amount"),
                    rs.getDate("date").toLocalDate()
                );
                String description = null;
                try {
                    description = rs.getString("description");
                } catch (Exception e) {

                }
                service.setDescription(description);
                return service;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<CommunityService> findByStudent(String studID) {
        List<CommunityService> services = new ArrayList<>();
        String sql = "SELECT service_id, stud_id, hours_rendered, credit_amount, date, description FROM community_service WHERE stud_id = ? ORDER BY date DESC";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studID);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                CommunityService service = new CommunityService(
                    rs.getString("service_id"),
                    rs.getString("stud_id"),
                    rs.getInt("hours_rendered"),
                    rs.getDouble("credit_amount"),
                    rs.getDate("date").toLocalDate()
                );
                String description = null;
                try {
                    description = rs.getString("description");
                } catch (Exception e) {

                }
                service.setDescription(description);
                services.add(service);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return services;
    }

    @Override
    public void save(CommunityService cs) {

        String sql = "INSERT INTO community_service (service_id, stud_id, hours_rendered, credit_amount, date, description) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cs.getServiceID());
            pstmt.setString(2, cs.getStudID());
            pstmt.setInt(3, cs.getHoursRendered());
            pstmt.setDouble(4, cs.getCreditAmount());
            pstmt.setDate(5, Date.valueOf(cs.getDate()));
            pstmt.setString(6, cs.getDescription());
            pstmt.executeUpdate();
        } catch (SQLException e) {

            if (e.getMessage() != null && e.getMessage().contains("Unknown column 'description'")) {
                String sqlOld = "INSERT INTO community_service (service_id, stud_id, hours_rendered, credit_amount, date) VALUES (?, ?, ?, ?, ?)";
                try (Connection conn = DBConfig.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sqlOld)) {
                    pstmt.setString(1, cs.getServiceID());
                    pstmt.setString(2, cs.getStudID());
                    pstmt.setInt(3, cs.getHoursRendered());
                    pstmt.setDouble(4, cs.getCreditAmount());
                    pstmt.setDate(5, Date.valueOf(cs.getDate()));
                    pstmt.executeUpdate();
                } catch (SQLException e2) {
                    e2.printStackTrace();
                    throw new RuntimeException("Failed to save community service", e2);
                }
            } else {
                e.printStackTrace();
                throw new RuntimeException("Failed to save community service", e);
            }
        }
    }

    @Override
    public void update(CommunityService cs) {
        String sql = "UPDATE community_service SET stud_id = ?, hours_rendered = ?, credit_amount = ?, date = ?, description = ? WHERE service_id = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cs.getStudID());
            pstmt.setInt(2, cs.getHoursRendered());
            pstmt.setDouble(3, cs.getCreditAmount());
            pstmt.setDate(4, Date.valueOf(cs.getDate()));
            pstmt.setString(5, cs.getDescription());
            pstmt.setString(6, cs.getServiceID());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update community service", e);
        }
    }

    @Override
    public void delete(String serviceID) {
        String sql = "DELETE FROM community_service WHERE service_id = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, serviceID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to delete community service", e);
        }
    }

    @Override
    public int getTotalHours() {
        String sql = "SELECT COALESCE(SUM(hours_rendered), 0) as total FROM community_service";

        try (Connection conn = DBConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public double getTotalCredits() {
        String sql = "SELECT COALESCE(SUM(credit_amount), 0) as total FROM community_service";

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
    public int getHoursByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COALESCE(SUM(hours_rendered), 0) as total FROM community_service WHERE date >= ? AND date <= ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public double getCreditsByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COALESCE(SUM(credit_amount), 0) as total FROM community_service WHERE date >= ? AND date <= ?";

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
}

