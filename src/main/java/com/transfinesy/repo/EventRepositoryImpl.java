package com.transfinesy.repo;

import com.transfinesy.config.DBConfig;
import com.transfinesy.model.AttendanceSession;
import com.transfinesy.model.Event;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class EventRepositoryImpl implements EventRepository {

    @Override
    public List<Event> findAll() {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT event_id, event_name, event_date, semester, school_year, " +
                     "am_time_in, am_time_out, pm_time_in, pm_time_out, " +
                     "session_type, fine_amount_absent, fine_amount_late, " +
                     "time_in_start_am, time_in_stop_am, time_out_start_am, time_out_stop_am, " +
                     "time_in_start_pm, time_in_stop_pm, time_out_start_pm, time_out_stop_pm, " +
                     "finalized " +
                     "FROM events ORDER BY event_date DESC";

        try (Connection conn = DBConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Event event = new Event(
                    rs.getString("event_id"),
                    rs.getString("event_name"),
                    rs.getDate("event_date").toLocalDate(),
                    rs.getInt("semester"),
                    rs.getString("school_year")
                );

                Time amTimeIn = rs.getTime("am_time_in");
                Time amTimeOut = rs.getTime("am_time_out");
                Time pmTimeIn = rs.getTime("pm_time_in");
                Time pmTimeOut = rs.getTime("pm_time_out");

                if (amTimeIn != null) event.setAmTimeIn(amTimeIn.toLocalTime());
                if (amTimeOut != null) event.setAmTimeOut(amTimeOut.toLocalTime());
                if (pmTimeIn != null) event.setPmTimeIn(pmTimeIn.toLocalTime());
                if (pmTimeOut != null) event.setPmTimeOut(pmTimeOut.toLocalTime());

                String sessionTypeStr = rs.getString("session_type");
                if (sessionTypeStr != null) {
                    try {
                        event.setSessionType(AttendanceSession.valueOf(sessionTypeStr));
                    } catch (IllegalArgumentException e) {

                    }
                }

                Time timeInStartAM = rs.getTime("time_in_start_am");
                Time timeInStopAM = rs.getTime("time_in_stop_am");
                Time timeOutStartAM = rs.getTime("time_out_start_am");
                Time timeOutStopAM = rs.getTime("time_out_stop_am");
                Time timeInStartPM = rs.getTime("time_in_start_pm");
                Time timeInStopPM = rs.getTime("time_in_stop_pm");
                Time timeOutStartPM = rs.getTime("time_out_start_pm");
                Time timeOutStopPM = rs.getTime("time_out_stop_pm");

                if (timeInStartAM != null) event.setTimeInStartAM(timeInStartAM.toLocalTime());
                if (timeInStopAM != null) event.setTimeInStopAM(timeInStopAM.toLocalTime());
                if (timeOutStartAM != null) event.setTimeOutStartAM(timeOutStartAM.toLocalTime());
                if (timeOutStopAM != null) event.setTimeOutStopAM(timeOutStopAM.toLocalTime());
                if (timeInStartPM != null) event.setTimeInStartPM(timeInStartPM.toLocalTime());
                if (timeInStopPM != null) event.setTimeInStopPM(timeInStopPM.toLocalTime());
                if (timeOutStartPM != null) event.setTimeOutStartPM(timeOutStartPM.toLocalTime());
                if (timeOutStopPM != null) event.setTimeOutStopPM(timeOutStopPM.toLocalTime());

                Double fineAbsent = rs.getObject("fine_amount_absent", Double.class);
                Double fineLate = rs.getObject("fine_amount_late", Double.class);
                if (fineAbsent != null) event.setFineAmountAbsent(fineAbsent);
                if (fineLate != null) event.setFineAmountLate(fineLate);

                Boolean finalized = rs.getObject("finalized", Boolean.class);
                if (finalized != null) event.setFinalized(finalized);

                events.add(event);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return events;
    }

    @Override
    public Event findById(String eventID) {
        String sql = "SELECT event_id, event_name, event_date, semester, school_year, " +
                     "am_time_in, am_time_out, pm_time_in, pm_time_out, " +
                     "session_type, fine_amount_absent, fine_amount_late, " +
                     "time_in_start_am, time_in_stop_am, time_out_start_am, time_out_stop_am, " +
                     "time_in_start_pm, time_in_stop_pm, time_out_start_pm, time_out_stop_pm, " +
                     "finalized " +
                     "FROM events WHERE event_id = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, eventID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Event event = new Event(
                    rs.getString("event_id"),
                    rs.getString("event_name"),
                    rs.getDate("event_date").toLocalDate(),
                    rs.getInt("semester"),
                    rs.getString("school_year")
                );

                Time amTimeIn = rs.getTime("am_time_in");
                Time amTimeOut = rs.getTime("am_time_out");
                Time pmTimeIn = rs.getTime("pm_time_in");
                Time pmTimeOut = rs.getTime("pm_time_out");

                if (amTimeIn != null) event.setAmTimeIn(amTimeIn.toLocalTime());
                if (amTimeOut != null) event.setAmTimeOut(amTimeOut.toLocalTime());
                if (pmTimeIn != null) event.setPmTimeIn(pmTimeIn.toLocalTime());
                if (pmTimeOut != null) event.setPmTimeOut(pmTimeOut.toLocalTime());

                String sessionTypeStr = rs.getString("session_type");
                if (sessionTypeStr != null) {
                    try {
                        event.setSessionType(AttendanceSession.valueOf(sessionTypeStr));
                    } catch (IllegalArgumentException e) {

                    }
                }

                Time timeInStartAM = rs.getTime("time_in_start_am");
                Time timeInStopAM = rs.getTime("time_in_stop_am");
                Time timeOutStartAM = rs.getTime("time_out_start_am");
                Time timeOutStopAM = rs.getTime("time_out_stop_am");
                Time timeInStartPM = rs.getTime("time_in_start_pm");
                Time timeInStopPM = rs.getTime("time_in_stop_pm");
                Time timeOutStartPM = rs.getTime("time_out_start_pm");
                Time timeOutStopPM = rs.getTime("time_out_stop_pm");

                if (timeInStartAM != null) event.setTimeInStartAM(timeInStartAM.toLocalTime());
                if (timeInStopAM != null) event.setTimeInStopAM(timeInStopAM.toLocalTime());
                if (timeOutStartAM != null) event.setTimeOutStartAM(timeOutStartAM.toLocalTime());
                if (timeOutStopAM != null) event.setTimeOutStopAM(timeOutStopAM.toLocalTime());
                if (timeInStartPM != null) event.setTimeInStartPM(timeInStartPM.toLocalTime());
                if (timeInStopPM != null) event.setTimeInStopPM(timeInStopPM.toLocalTime());
                if (timeOutStartPM != null) event.setTimeOutStartPM(timeOutStartPM.toLocalTime());
                if (timeOutStopPM != null) event.setTimeOutStopPM(timeOutStopPM.toLocalTime());

                Double fineAbsent = rs.getObject("fine_amount_absent", Double.class);
                Double fineLate = rs.getObject("fine_amount_late", Double.class);
                if (fineAbsent != null) event.setFineAmountAbsent(fineAbsent);
                if (fineLate != null) event.setFineAmountLate(fineLate);

                Boolean finalized = rs.getObject("finalized", Boolean.class);
                if (finalized != null) event.setFinalized(finalized);

                return event;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void save(Event e) {
        String sql = "INSERT INTO events (event_id, event_name, event_date, semester, school_year, " +
                     "am_time_in, am_time_out, pm_time_in, pm_time_out, " +
                     "session_type, fine_amount_absent, fine_amount_late, " +
                     "time_in_start_am, time_in_stop_am, time_out_start_am, time_out_stop_am, " +
                     "time_in_start_pm, time_in_stop_pm, time_out_start_pm, time_out_stop_pm, " +
                     "finalized) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, e.getEventID());
            pstmt.setString(2, e.getEventName());
            pstmt.setDate(3, Date.valueOf(e.getEventDate()));
            pstmt.setInt(4, e.getSemester());
            pstmt.setString(5, e.getSchoolYear());
            pstmt.setTime(6, e.getAmTimeIn() != null ? Time.valueOf(e.getAmTimeIn()) : null);
            pstmt.setTime(7, e.getAmTimeOut() != null ? Time.valueOf(e.getAmTimeOut()) : null);
            pstmt.setTime(8, e.getPmTimeIn() != null ? Time.valueOf(e.getPmTimeIn()) : null);
            pstmt.setTime(9, e.getPmTimeOut() != null ? Time.valueOf(e.getPmTimeOut()) : null);
            pstmt.setString(10, e.getSessionType() != null ? e.getSessionType().name() : null);
            pstmt.setObject(11, e.getFineAmountAbsent());
            pstmt.setObject(12, e.getFineAmountLate());
            pstmt.setTime(13, e.getTimeInStartAM() != null ? Time.valueOf(e.getTimeInStartAM()) : null);
            pstmt.setTime(14, e.getTimeInStopAM() != null ? Time.valueOf(e.getTimeInStopAM()) : null);
            pstmt.setTime(15, e.getTimeOutStartAM() != null ? Time.valueOf(e.getTimeOutStartAM()) : null);
            pstmt.setTime(16, e.getTimeOutStopAM() != null ? Time.valueOf(e.getTimeOutStopAM()) : null);
            pstmt.setTime(17, e.getTimeInStartPM() != null ? Time.valueOf(e.getTimeInStartPM()) : null);
            pstmt.setTime(18, e.getTimeInStopPM() != null ? Time.valueOf(e.getTimeInStopPM()) : null);
            pstmt.setTime(19, e.getTimeOutStartPM() != null ? Time.valueOf(e.getTimeOutStartPM()) : null);
            pstmt.setTime(20, e.getTimeOutStopPM() != null ? Time.valueOf(e.getTimeOutStopPM()) : null);
            pstmt.setBoolean(21, e.isFinalized());
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Failed to save event", ex);
        }
    }

    @Override
    public void update(Event e) {
        String sql = "UPDATE events SET event_name = ?, event_date = ?, semester = ?, school_year = ?, " +
                     "am_time_in = ?, am_time_out = ?, pm_time_in = ?, pm_time_out = ?, " +
                     "session_type = ?, fine_amount_absent = ?, fine_amount_late = ?, " +
                     "time_in_start_am = ?, time_in_stop_am = ?, time_out_start_am = ?, time_out_stop_am = ?, " +
                     "time_in_start_pm = ?, time_in_stop_pm = ?, time_out_start_pm = ?, time_out_stop_pm = ?, " +
                     "finalized = ? " +
                     "WHERE event_id = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, e.getEventName());
            pstmt.setDate(2, Date.valueOf(e.getEventDate()));
            pstmt.setInt(3, e.getSemester());
            pstmt.setString(4, e.getSchoolYear());
            pstmt.setTime(5, e.getAmTimeIn() != null ? Time.valueOf(e.getAmTimeIn()) : null);
            pstmt.setTime(6, e.getAmTimeOut() != null ? Time.valueOf(e.getAmTimeOut()) : null);
            pstmt.setTime(7, e.getPmTimeIn() != null ? Time.valueOf(e.getPmTimeIn()) : null);
            pstmt.setTime(8, e.getPmTimeOut() != null ? Time.valueOf(e.getPmTimeOut()) : null);
            pstmt.setString(9, e.getSessionType() != null ? e.getSessionType().name() : null);
            pstmt.setObject(10, e.getFineAmountAbsent());
            pstmt.setObject(11, e.getFineAmountLate());
            pstmt.setTime(12, e.getTimeInStartAM() != null ? Time.valueOf(e.getTimeInStartAM()) : null);
            pstmt.setTime(13, e.getTimeInStopAM() != null ? Time.valueOf(e.getTimeInStopAM()) : null);
            pstmt.setTime(14, e.getTimeOutStartAM() != null ? Time.valueOf(e.getTimeOutStartAM()) : null);
            pstmt.setTime(15, e.getTimeOutStopAM() != null ? Time.valueOf(e.getTimeOutStopAM()) : null);
            pstmt.setTime(16, e.getTimeInStartPM() != null ? Time.valueOf(e.getTimeInStartPM()) : null);
            pstmt.setTime(17, e.getTimeInStopPM() != null ? Time.valueOf(e.getTimeInStopPM()) : null);
            pstmt.setTime(18, e.getTimeOutStartPM() != null ? Time.valueOf(e.getTimeOutStartPM()) : null);
            pstmt.setTime(19, e.getTimeOutStopPM() != null ? Time.valueOf(e.getTimeOutStopPM()) : null);
            pstmt.setBoolean(20, e.isFinalized());
            pstmt.setString(21, e.getEventID());
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Failed to update event", ex);
        }
    }

    @Override
    public void delete(String eventID) {
        String sql = "DELETE FROM events WHERE event_id = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, eventID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to delete event", e);
        }
    }
}

