package com.transfinesy.service;

import com.transfinesy.model.Attendance;
import com.transfinesy.model.AttendanceStatus;
import com.transfinesy.model.Event;
import com.transfinesy.model.Student;
import com.transfinesy.repo.AttendanceRepository;
import com.transfinesy.repo.AttendanceRepositoryImpl;
import com.transfinesy.repo.StudentRepository;
import com.transfinesy.repo.StudentRepositoryImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class RFIDService {
    private StudentRepository studentRepository;
    private AttendanceRepository attendanceRepository;

    public RFIDService() {
        this.studentRepository = new StudentRepositoryImpl();
        this.attendanceRepository = new AttendanceRepositoryImpl();
    }

    public String detectRFID() {

        return null;
    }

    public Student getStudentByRFID(String rfidTag) {
        if (rfidTag == null || rfidTag.trim().isEmpty()) {
            return null;
        }
        return studentRepository.findByRFID(rfidTag);
    }

    public Attendance autoCheckIn(Event event, String rfidTag) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }
        if (rfidTag == null || rfidTag.trim().isEmpty()) {
            throw new IllegalArgumentException("RFID tag cannot be null or empty");
        }

        Student student = getStudentByRFID(rfidTag);
        if (student == null) {
            throw new IllegalArgumentException("Student not found for RFID: " + rfidTag);
        }

        LocalDateTime checkInTime = LocalDateTime.now();
        LocalTime relevantTimeIn = event.getRelevantTimeIn(checkInTime);

        AttendanceStatus status = AttendanceStatus.PRESENT;
        int minutesLate = 0;

        if (relevantTimeIn != null) {
            LocalTime checkInTimeOnly = checkInTime.toLocalTime();
            if (checkInTimeOnly.isAfter(relevantTimeIn)) {
                status = AttendanceStatus.LATE;
                minutesLate = (int) ChronoUnit.MINUTES.between(relevantTimeIn, checkInTimeOnly);
            }
        }

        Attendance existing = findAttendanceByStudentAndEvent(student.getStudID(), event.getEventID());

        if (existing != null) {

            existing.setCheckInTime(checkInTime);
            existing.setStatus(status);
            existing.setMinutesLate(minutesLate);
            existing.setScanSource("RFID");
            attendanceRepository.update(existing);
            return existing;
        } else {

            String attendanceID = "ATT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            Attendance attendance = new Attendance(
                attendanceID,
                student.getStudID(),
                event.getEventID(),
                status,
                minutesLate,
                checkInTime,
                null,
                "RFID"
            );
            attendanceRepository.save(attendance);
            return attendance;
        }
    }

    private Attendance findAttendanceByStudentAndEvent(String studID, String eventID) {
        var attendances = attendanceRepository.findByEvent(eventID);
        return attendances.stream()
            .filter(a -> a.getStudID().equals(studID))
            .findFirst()
            .orElse(null);
    }
}
