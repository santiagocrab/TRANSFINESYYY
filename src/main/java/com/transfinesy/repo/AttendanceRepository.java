package com.transfinesy.repo;

import com.transfinesy.model.Attendance;
import java.util.List;
import java.util.Map;

public interface AttendanceRepository {
    List<Attendance> findAll();
    Attendance findById(String attendanceID);
    List<Attendance> findByEvent(String eventID);
    List<Attendance> findByStudent(String studID);
    Attendance findByStudentAndEvent(String studID, String eventID);
    void save(Attendance a);
    void update(Attendance a);
    void delete(String attendanceID);

    Map<String, Long> countUniqueStudentsByStatus(String eventId);

    Map<String, Long> countUniqueStudentsByStatusFiltered(String eventId, String course, String yearLevel, String section);
    
    Map<String, Long> countTotalAttendanceRecordsByStatus();
}

