package com.transfinesy.repo;

import com.transfinesy.config.DBConfig;
import com.transfinesy.model.Attendance;
import com.transfinesy.model.AttendanceStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttendanceRepositoryImpl implements AttendanceRepository {

    @Override
    public List<Attendance> findAll() {
        List<Attendance> attendances = new ArrayList<>();
        String sql = "SELECT attendance_id, stud_id, event_id, status, minutes_late, check_in_time, check_out_time, scan_source, session, record_type FROM attendance";

        try (Connection conn = DBConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Attendance attendance = new Attendance(
                    rs.getString("attendance_id"),
                    rs.getString("stud_id"),
                    rs.getString("event_id"),
                    AttendanceStatus.valueOf(rs.getString("status")),
                    rs.getInt("minutes_late")
                );
                Timestamp checkIn = rs.getTimestamp("check_in_time");
                Timestamp checkOut = rs.getTimestamp("check_out_time");
                String scanSource = rs.getString("scan_source");
                if (checkIn != null) attendance.setCheckInTime(checkIn.toLocalDateTime());
                if (checkOut != null) attendance.setCheckOutTime(checkOut.toLocalDateTime());
                attendance.setScanSource(scanSource != null ? scanSource : "MANUAL");
                try {
                    attendance.setSession(rs.getString("session"));
                    attendance.setRecordType(rs.getString("record_type"));
                } catch (SQLException e) {
                }
                attendances.add(attendance);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return attendances;
    }

    @Override
    public Attendance findById(String attendanceID) {
        String sql = "SELECT attendance_id, stud_id, event_id, status, minutes_late, check_in_time, check_out_time, scan_source, session, record_type FROM attendance WHERE attendance_id = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, attendanceID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Attendance attendance = new Attendance(
                    rs.getString("attendance_id"),
                    rs.getString("stud_id"),
                    rs.getString("event_id"),
                    AttendanceStatus.valueOf(rs.getString("status")),
                    rs.getInt("minutes_late")
                );
                Timestamp checkIn = rs.getTimestamp("check_in_time");
                Timestamp checkOut = rs.getTimestamp("check_out_time");
                String scanSource = rs.getString("scan_source");
                if (checkIn != null) attendance.setCheckInTime(checkIn.toLocalDateTime());
                if (checkOut != null) attendance.setCheckOutTime(checkOut.toLocalDateTime());
                attendance.setScanSource(scanSource != null ? scanSource : "MANUAL");
                try {
                    attendance.setSession(rs.getString("session"));
                    attendance.setRecordType(rs.getString("record_type"));
                } catch (SQLException e) {
                }
                return attendance;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<Attendance> findByEvent(String eventID) {
        List<Attendance> attendances = new ArrayList<>();

        String sql = "SELECT attendance_id, stud_id, event_id, status, minutes_late, check_in_time, check_out_time, scan_source, session, record_type FROM attendance WHERE event_id = ? ORDER BY check_in_time DESC";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, eventID);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Attendance attendance = new Attendance(
                    rs.getString("attendance_id"),
                    rs.getString("stud_id"),
                    rs.getString("event_id"),
                    AttendanceStatus.valueOf(rs.getString("status")),
                    rs.getInt("minutes_late")
                );
                Timestamp checkIn = rs.getTimestamp("check_in_time");
                Timestamp checkOut = rs.getTimestamp("check_out_time");
                String scanSource = null;
                try {
                    scanSource = rs.getString("scan_source");
                } catch (SQLException e) {

                    scanSource = "MANUAL";
                }
                if (checkIn != null) attendance.setCheckInTime(checkIn.toLocalDateTime());
                if (checkOut != null) attendance.setCheckOutTime(checkOut.toLocalDateTime());
                attendance.setScanSource(scanSource != null ? scanSource : "MANUAL");
                try {
                    attendance.setSession(rs.getString("session"));
                    attendance.setRecordType(rs.getString("record_type"));
                } catch (SQLException e) {
                }
                attendances.add(attendance);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error fetching attendance for event: " + eventID);
            System.err.println("SQL Error: " + e.getMessage());
        }

        return attendances;
    }

    @Override
    public List<Attendance> findByStudent(String studID) {
        List<Attendance> attendances = new ArrayList<>();
        String sql = "SELECT attendance_id, stud_id, event_id, status, minutes_late, check_in_time, check_out_time, scan_source, session, record_type FROM attendance WHERE stud_id = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studID);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Attendance attendance = new Attendance(
                    rs.getString("attendance_id"),
                    rs.getString("stud_id"),
                    rs.getString("event_id"),
                    AttendanceStatus.valueOf(rs.getString("status")),
                    rs.getInt("minutes_late")
                );
                Timestamp checkIn = rs.getTimestamp("check_in_time");
                Timestamp checkOut = rs.getTimestamp("check_out_time");
                String scanSource = rs.getString("scan_source");
                if (checkIn != null) attendance.setCheckInTime(checkIn.toLocalDateTime());
                if (checkOut != null) attendance.setCheckOutTime(checkOut.toLocalDateTime());
                attendance.setScanSource(scanSource != null ? scanSource : "MANUAL");
                try {
                    attendance.setSession(rs.getString("session"));
                    attendance.setRecordType(rs.getString("record_type"));
                } catch (SQLException e) {
                }
                attendances.add(attendance);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return attendances;
    }

    @Override
    public Attendance findByStudentAndEvent(String studID, String eventID) {
        String sql = "SELECT attendance_id, stud_id, event_id, status, minutes_late, check_in_time, check_out_time, scan_source, session, record_type FROM attendance WHERE stud_id = ? AND event_id = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studID);
            pstmt.setString(2, eventID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Attendance attendance = new Attendance(
                    rs.getString("attendance_id"),
                    rs.getString("stud_id"),
                    rs.getString("event_id"),
                    AttendanceStatus.valueOf(rs.getString("status")),
                    rs.getInt("minutes_late")
                );
                Timestamp checkIn = rs.getTimestamp("check_in_time");
                Timestamp checkOut = rs.getTimestamp("check_out_time");
                String scanSource = rs.getString("scan_source");
                if (checkIn != null) attendance.setCheckInTime(checkIn.toLocalDateTime());
                if (checkOut != null) attendance.setCheckOutTime(checkOut.toLocalDateTime());
                attendance.setScanSource(scanSource != null ? scanSource : "MANUAL");
                try {
                    attendance.setSession(rs.getString("session"));
                    attendance.setRecordType(rs.getString("record_type"));
                } catch (SQLException e) {
                }
                return attendance;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void save(Attendance a) {

        boolean hasSessionColumn = checkColumnExists("attendance", "session");
        boolean hasRecordTypeColumn = checkColumnExists("attendance", "record_type");

        String sql;
        if (hasSessionColumn && hasRecordTypeColumn) {
            sql = "INSERT INTO attendance (attendance_id, stud_id, event_id, status, minutes_late, check_in_time, check_out_time, scan_source, session, record_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        } else {
            sql = "INSERT INTO attendance (attendance_id, stud_id, event_id, status, minutes_late, check_in_time, check_out_time, scan_source) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        }

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, a.getAttendanceID());
            pstmt.setString(2, a.getStudID());
            pstmt.setString(3, a.getEventID());
            pstmt.setString(4, a.getStatus().name());
            pstmt.setInt(5, a.getMinutesLate());
            pstmt.setTimestamp(6, a.getCheckInTime() != null ? Timestamp.valueOf(a.getCheckInTime()) : null);
            pstmt.setTimestamp(7, a.getCheckOutTime() != null ? Timestamp.valueOf(a.getCheckOutTime()) : null);
            pstmt.setString(8, a.getScanSource() != null ? a.getScanSource() : "MANUAL");

            if (hasSessionColumn && hasRecordTypeColumn) {
                pstmt.setString(9, a.getSession());
                pstmt.setString(10, a.getRecordType());
            }

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save attendance", e);
        }
    }

    private boolean checkColumnExists(String tableName, String columnName) {
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?")) {
            pstmt.setString(1, tableName);
            pstmt.setString(2, columnName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {

            return false;
        }
        return false;
    }

    @Override
    public void update(Attendance a) {
        boolean hasSessionColumn = checkColumnExists("attendance", "session");
        boolean hasRecordTypeColumn = checkColumnExists("attendance", "record_type");
        
        String sql;
        if (hasSessionColumn && hasRecordTypeColumn) {
            sql = "UPDATE attendance SET stud_id = ?, event_id = ?, status = ?, minutes_late = ?, check_in_time = ?, check_out_time = ?, scan_source = ?, session = ?, record_type = ? WHERE attendance_id = ?";
        } else {
            sql = "UPDATE attendance SET stud_id = ?, event_id = ?, status = ?, minutes_late = ?, check_in_time = ?, check_out_time = ?, scan_source = ? WHERE attendance_id = ?";
        }

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, a.getStudID());
            pstmt.setString(2, a.getEventID());
            pstmt.setString(3, a.getStatus().name());
            pstmt.setInt(4, a.getMinutesLate());
            pstmt.setTimestamp(5, a.getCheckInTime() != null ? Timestamp.valueOf(a.getCheckInTime()) : null);
            pstmt.setTimestamp(6, a.getCheckOutTime() != null ? Timestamp.valueOf(a.getCheckOutTime()) : null);
            pstmt.setString(7, a.getScanSource() != null ? a.getScanSource() : "MANUAL");
            
            if (hasSessionColumn && hasRecordTypeColumn) {
                pstmt.setString(8, a.getSession());
                pstmt.setString(9, a.getRecordType());
                pstmt.setString(10, a.getAttendanceID());
            } else {
                pstmt.setString(8, a.getAttendanceID());
            }
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update attendance", e);
        }
    }

    @Override
    public void delete(String attendanceID) {
        String sql = "DELETE FROM attendance WHERE attendance_id = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, attendanceID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to delete attendance", e);
        }
    }

    @Override
    public Map<String, Long> countUniqueStudentsByStatus(String eventId) {
        Map<String, Long> counts = new HashMap<>();
        String sql;

        if (eventId != null && !eventId.isEmpty() && !eventId.equals("all")) {

            sql = "SELECT status, COUNT(DISTINCT stud_id) as count " +
                  "FROM attendance WHERE event_id = ? " +
                  "GROUP BY status";
        } else {

            sql = "SELECT status, COUNT(DISTINCT stud_id) as count " +
                  "FROM attendance " +
                  "GROUP BY status";
        }

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (eventId != null && !eventId.isEmpty() && !eventId.equals("all")) {
                pstmt.setString(1, eventId);
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String status = rs.getString("status");
                long count = rs.getLong("count");
                counts.put(status, count);
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }

        return counts;
    }

    @Override
    public Map<String, Long> countUniqueStudentsByStatusFiltered(String eventId, String course, String yearLevel, String section) {
        Map<String, Long> counts = new HashMap<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT a.status, COUNT(DISTINCT a.stud_id) as count ");
        sql.append("FROM attendance a ");
        sql.append("INNER JOIN students s ON a.stud_id = s.stud_id ");
        sql.append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (eventId != null && !eventId.isEmpty() && !eventId.equals("all")) {
            sql.append("AND a.event_id = ? ");
            params.add(eventId);
        }

        if (course != null && !course.isEmpty() && !course.equals("all")) {
            sql.append("AND s.course = ? ");
            params.add(course);
        }

        if (yearLevel != null && !yearLevel.isEmpty() && !yearLevel.equals("all")) {
            sql.append("AND s.year_level = ? ");
            params.add(yearLevel);
        }

        if (section != null && !section.isEmpty() && !section.equals("all")) {
            sql.append("AND s.section = ? ");
            params.add(section);
        }

        sql.append("GROUP BY a.status");

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String status = rs.getString("status");
                long count = rs.getLong("count");
                counts.put(status, count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return counts;
    }
    
    @Override
    public Map<String, Long> countTotalAttendanceRecordsByStatus() {
        Map<String, Long> counts = new HashMap<>();
        
        // First, get total count of all records to verify
        String totalSql = "SELECT COUNT(*) as total FROM attendance";
        long totalRecordsInDB = 0;
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(totalSql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                totalRecordsInDB = rs.getLong("total");
            }
        } catch (SQLException e) {
            System.err.println("ERROR getting total records: " + e.getMessage());
        }
        
        String sql = "SELECT status, COUNT(*) as count " +
                     "FROM attendance " +
                     "GROUP BY status";

        System.out.println("=== COUNTING TOTAL ATTENDANCE RECORDS BY STATUS ===");
        System.out.println("TOTAL RECORDS IN DATABASE: " + totalRecordsInDB);
        System.out.println("SQL: " + sql);

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();
            long totalRecords = 0;
            while (rs.next()) {
                String status = rs.getString("status");
                long count = rs.getLong("count");
                counts.put(status, count);
                totalRecords += count;
                System.out.println("  Status: " + status + " = " + count + " records");
            }
            System.out.println("  TOTAL RECORDS COUNTED BY STATUS: " + totalRecords);
            System.out.println("================================================");
        } catch (SQLException e) {
            System.err.println("ERROR counting attendance records: " + e.getMessage());
            e.printStackTrace();
        }

        return counts;
    }
}

