package com.transfinesy.service;

import com.transfinesy.model.Attendance;
import com.transfinesy.model.AttendanceSession;
import com.transfinesy.model.AttendanceStatus;
import com.transfinesy.model.Event;
import com.transfinesy.model.Fine;
import com.transfinesy.model.Student;
import com.transfinesy.repo.AttendanceRepository;
import com.transfinesy.repo.AttendanceRepositoryImpl;
import com.transfinesy.repo.EventRepository;
import com.transfinesy.repo.EventRepositoryImpl;
import com.transfinesy.repo.StudentRepository;
import com.transfinesy.repo.StudentRepositoryImpl;
import com.transfinesy.service.RFIDService;
import com.transfinesy.util.Queue;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AttendanceService {
    private AttendanceRepository repository;
    private EventRepository eventRepository;
    private StudentRepository studentRepository;
    private FineService fineService;
    private LedgerService ledgerService;
    private RFIDService rfidService;

    private Queue<AttendanceScanRequest> scanQueue;

    public AttendanceService() {
        this.repository = new AttendanceRepositoryImpl();
        this.eventRepository = new EventRepositoryImpl();
        this.studentRepository = new StudentRepositoryImpl();
        this.rfidService = new RFIDService();
        this.scanQueue = new Queue<>();
    }

    private static class AttendanceScanRequest {
        String rfidTag;
        String eventId;
        String sessionType;
        boolean isTimeIn;

        AttendanceScanRequest(String rfidTag, String eventId, String sessionType, boolean isTimeIn) {
            this.rfidTag = rfidTag;
            this.eventId = eventId;
            this.sessionType = sessionType;
            this.isTimeIn = isTimeIn;
        }
    }

    public AttendanceService(FineService fineService) {
        this();
        this.fineService = fineService;
        this.ledgerService = new LedgerService();
    }

    public AttendanceService(FineService fineService, RFIDService rfidService) {
        this();
        this.fineService = fineService;
        this.ledgerService = new LedgerService();
        this.rfidService = rfidService;
    }
    
    public AttendanceService(FineService fineService, LedgerService ledgerService) {
        this();
        this.fineService = fineService;
        this.ledgerService = ledgerService;
    }

    public List<Student> searchStudents(String query) {
        return studentRepository.search(query);
    }

    public Attendance checkInStudent(String studID, String eventID) {
        throw new IllegalStateException("Attendance session not defined. Please use checkInStudentWithWindow with AM or PM session.");
    }

    public void checkOutStudent(String studID, String eventID) {
        Attendance attendance = findAttendanceByStudentAndEvent(studID, eventID);
        if (attendance != null) {
            attendance.setCheckOutTime(LocalDateTime.now());
            repository.update(attendance);
        }
    }

    public Attendance findAttendanceByStudentAndEvent(String studID, String eventID) {
        List<Attendance> attendances = repository.findByEvent(eventID);
        return attendances.stream()
            .filter(a -> a.getStudID().equals(studID))
            .findFirst()
            .orElse(null);
    }

    private Attendance findAttendanceByStudentEventSessionAndType(String studID, String eventID, String session, String recordType) {
        List<Attendance> attendances = repository.findByEvent(eventID);
        return attendances.stream()
            .filter(a -> a.getStudID().equals(studID))
            .filter(a -> a.getSession() != null && (session == null || session.equalsIgnoreCase(a.getSession())))
            .filter(a -> (recordType == null && a.getRecordType() == null) || (recordType != null && recordType.equals(a.getRecordType())))
            .findFirst()
            .orElse(null);
    }

    public Attendance scanRFID(String rfidTag, Event event) {
        throw new IllegalStateException("Attendance session not defined. Please use scanRFIDWithWindow with AM or PM session.");
    }

    public void finalizeEventAttendance(String eventID) {
        Event event = eventRepository.findById(eventID);
        if (event == null) {
            throw new IllegalArgumentException("Event not found: " + eventID);
        }

        if (event.isFinalized()) {
            throw new IllegalStateException("Event is already finalized. No further attendance can be recorded.");
        }

        List<Student> allStudents = studentRepository.findAll();
        List<Attendance> existingAttendance = repository.findByEvent(eventID);
        java.util.Set<String> studentsWithAttendance = existingAttendance.stream()
            .map(Attendance::getStudID)
            .collect(Collectors.toSet());

        for (Student student : allStudents) {
            if (!studentsWithAttendance.contains(student.getStudID())) {
                String attendanceID = "ATT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                Attendance absentAttendance = new Attendance(
                    attendanceID,
                    student.getStudID(),
                    eventID,
                    AttendanceStatus.ABSENT,
                    0,
                    null,
                    null,
                    "MANUAL"
                );
                repository.save(absentAttendance);
            }
        }

        List<Attendance> allAttendance = repository.findByEvent(eventID);
        
        if (fineService != null) {
            fineService.generateFinesFromAttendances(allAttendance, eventID, event);
        }
        
        java.util.Set<String> allStudentIDs = allAttendance.stream()
            .map(Attendance::getStudID)
            .collect(Collectors.toSet());
        
        if (ledgerService != null) {
            for (String studID : allStudentIDs) {
                ledgerService.getLedgerForStudent(studID);
            }
        }

        // Mark event as finalized
        event.setFinalized(true);
        eventRepository.update(event);
    }

    public void finalizeAttendanceAndGenerateFines(String eventID) {
        finalizeEventAttendance(eventID);
    }

    public void recordAttendance(Attendance attendance) {
        if (attendance == null) {
            throw new IllegalArgumentException("Attendance cannot be null");
        }
        repository.save(attendance);
    }

    public void updateAttendance(Attendance attendance) {
        if (attendance == null) {
            throw new IllegalArgumentException("Attendance cannot be null");
        }
        repository.update(attendance);
    }

    public List<Attendance> getAttendanceByEvent(String eventID) {
        return repository.findByEvent(eventID);
    }

    public List<Attendance> getAttendanceByStudent(String studID) {
        return repository.findByStudent(studID);
    }

    public Attendance getAttendanceById(String attendanceID) {
        return repository.findById(attendanceID);
    }

    public void deleteAttendance(String attendanceID) {
        repository.delete(attendanceID);
    }

    public List<Attendance> getAllAttendance() {
        return repository.findAll();
    }

    public Map<String, Long> getAttendanceCountsByStatus(String eventId) {
        return repository.countUniqueStudentsByStatus(eventId);
    }

    public Map<String, Long> getAttendanceCountsByStatusFiltered(String eventId, String course, String yearLevel, String section) {
        return repository.countUniqueStudentsByStatusFiltered(eventId, course, yearLevel, section);
    }

    public void saveAttendanceBatch(List<Attendance> attendances) {
        for (Attendance attendance : attendances) {
            recordAttendance(attendance);
        }
    }

    public Attendance checkInStudentWithWindow(String studID, String eventID, String sessionType, boolean isTimeIn) {
        Event event = eventRepository.findById(eventID);
        if (event == null) {
            throw new IllegalArgumentException("Event not found: " + eventID);
        }

        if (event.isFinalized()) {
            throw new IllegalStateException("Event is finalized. No further attendance can be recorded.");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDate eventDate = event.getEventDate();
        LocalDate today = now.toLocalDate();
        long daysDifference = java.time.temporal.ChronoUnit.DAYS.between(eventDate, today);

        if (daysDifference > 1) {
            throw new IllegalArgumentException("Attendance cannot be recorded. The event date (" + eventDate + ") was more than 1 day ago.");
        }

        LocalDateTime checkInTime = now;
        LocalTime scannedTime = checkInTime.toLocalTime();

        LocalTime startTime = null;
        LocalTime stopTime = null;

        if ("AM".equalsIgnoreCase(sessionType)) {
            if (isTimeIn) {
                startTime = event.getTimeInStartAM();
                stopTime = event.getTimeInStopAM();
            } else {
                startTime = event.getTimeOutStartAM();
                stopTime = event.getTimeOutStopAM();
            }
        } else if ("PM".equalsIgnoreCase(sessionType)) {
            if (isTimeIn) {
                startTime = event.getTimeInStartPM();
                stopTime = event.getTimeInStopPM();
            } else {
                startTime = event.getTimeOutStartPM();
                stopTime = event.getTimeOutStopPM();
            }
        }

        AttendanceStatus status = AttendanceStatus.PRESENT;
        int minutesLate = 0;

        if (startTime != null && stopTime != null) {

            if (scannedTime.isBefore(startTime)) {

                status = AttendanceStatus.LATE;
                minutesLate = (int) ChronoUnit.MINUTES.between(scannedTime, startTime);
            } else if (scannedTime.isAfter(stopTime)) {

                status = AttendanceStatus.LATE;
                minutesLate = (int) ChronoUnit.MINUTES.between(stopTime, scannedTime);
            } else {

                if (scannedTime.equals(startTime) || scannedTime.isAfter(startTime)) {

                    status = AttendanceStatus.PRESENT;
                    minutesLate = 0;
                } else {

                    status = AttendanceStatus.PRESENT;
                    minutesLate = 0;
                }
            }
        } else {

            LocalTime relevantTimeIn = event.getRelevantTimeIn(checkInTime);
            if (relevantTimeIn != null && scannedTime.isAfter(relevantTimeIn)) {
                status = AttendanceStatus.LATE;
                minutesLate = (int) ChronoUnit.MINUTES.between(relevantTimeIn, scannedTime);
            }
        }

        String recordType = isTimeIn ? "TIME_IN" : "TIME_OUT";
        
        if (isTimeIn) {
            Attendance existingTimeIn = findAttendanceByStudentEventSessionAndType(studID, eventID, sessionType, "TIME_IN");
            if (existingTimeIn != null) {
                String firstCheckIn = existingTimeIn.getCheckInTime() != null ? 
                    existingTimeIn.getCheckInTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A";
                throw new IllegalArgumentException(
                    "Attendance already recorded for this student in " + sessionType + " session. " +
                    "First check-in time: " + firstCheckIn + ". " +
                    "Duplicate check-ins are not allowed."
                );
            }
        } else {
            Attendance existingTimeIn = findAttendanceByStudentEventSessionAndType(studID, eventID, sessionType, "TIME_IN");
            Attendance existingTimeOut = findAttendanceByStudentEventSessionAndType(studID, eventID, sessionType, "TIME_OUT");
            
            if (existingTimeOut != null) {
                String firstTimeOut = existingTimeOut.getCheckOutTime() != null ? 
                    existingTimeOut.getCheckOutTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A";
                throw new IllegalArgumentException(
                    "Time-out already recorded for this student in " + sessionType + " session. " +
                    "First time-out: " + firstTimeOut + ". " +
                    "Duplicate time-outs are not allowed."
                );
            }
            
            if (existingTimeIn == null) {
                AttendanceStatus halfAbsentStatus = "AM".equalsIgnoreCase(sessionType) ? 
                    AttendanceStatus.HALF_ABSENT_AM : AttendanceStatus.HALF_ABSENT_PM;
                
                String attendanceID = "ATT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                Attendance halfAbsentAttendance = new Attendance(
                    attendanceID,
                    studID,
                    eventID,
                    halfAbsentStatus,
                    0,
                    null,
                    checkInTime,
                    "RFID"
                );
                halfAbsentAttendance.setSession(sessionType != null ? sessionType.toUpperCase() : null);
                halfAbsentAttendance.setRecordType("TIME_OUT");
                repository.save(halfAbsentAttendance);
                
                if (fineService != null) {
                    Fine fine = fineService.createFineFromAttendance(halfAbsentAttendance, eventID, event);
                    if (fine != null) {
                        fineService.saveFine(fine);
                    }
                }
                
                return halfAbsentAttendance;
            } else {
                String attendanceID = "ATT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                Attendance timeOutAttendance = new Attendance(
                    attendanceID,
                    studID,
                    eventID,
                    existingTimeIn.getStatus(),
                    existingTimeIn.getMinutesLate(),
                    null,
                    checkInTime,
                    "RFID"
                );
                timeOutAttendance.setSession(sessionType != null ? sessionType.toUpperCase() : null);
                timeOutAttendance.setRecordType("TIME_OUT");
                repository.save(timeOutAttendance);
                
                return timeOutAttendance;
            }
        }

        String attendanceID = "ATT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Attendance attendance = new Attendance(
            attendanceID,
            studID,
            eventID,
            status,
            minutesLate,
            isTimeIn ? checkInTime : null,
            isTimeIn ? null : checkInTime,
            "RFID"
        );
        attendance.setSession(sessionType != null ? sessionType.toUpperCase() : null);
        attendance.setRecordType(recordType);
        repository.save(attendance);

        if (status == AttendanceStatus.LATE && minutesLate > 0) {
            FineService fineService = this.fineService != null ? this.fineService : new FineService();
            Fine fine = fineService.createFineFromAttendance(attendance, eventID, event);
            if (fine != null) {
                fineService.saveFine(fine);
            }
        }

        checkAndUpdateHalfAbsentStatus(studID, eventID, sessionType);

        return attendance;
    }

    public Attendance scanRFIDWithWindow(String rfidTag, Event event, String sessionType, boolean isTimeIn) {
        if (event.isFinalized()) {
            throw new IllegalStateException("Event is finalized. No further attendance can be recorded.");
        }

        if (rfidService == null) {
            rfidService = new RFIDService();
        }

        AttendanceScanRequest request = new AttendanceScanRequest(rfidTag, event.getEventID(), sessionType, isTimeIn);
        scanQueue.enqueue(request);

        try {

            Student student = rfidService.getStudentByRFID(rfidTag);
            if (student == null) {
                throw new IllegalArgumentException("Student not found for RFID: " + rfidTag);
            }

            Attendance attendance = checkInStudentWithWindow(student.getStudID(), event.getEventID(), sessionType, isTimeIn);

            if (!scanQueue.isEmpty()) {
                scanQueue.dequeue();
            }

            return attendance;
        } catch (Exception e) {

            if (!scanQueue.isEmpty()) {
                try {
                    scanQueue.dequeue();
                } catch (Exception ex) {

                }
            }
            throw e;
        }
    }

    public int getScanQueueSize() {
        return scanQueue.size();
    }

    public void markSessionAbsentees(String eventID, String sessionType, boolean isTimeIn) {
        Event event = eventRepository.findById(eventID);
        if (event == null) {
            throw new IllegalArgumentException("Event not found: " + eventID);
        }
        LocalTime stopTime = null;
        if ("AM".equalsIgnoreCase(sessionType)) {
            stopTime = isTimeIn ? event.getTimeInStopAM() : event.getTimeOutStopAM();
        } else if ("PM".equalsIgnoreCase(sessionType)) {
            stopTime = isTimeIn ? event.getTimeInStopPM() : event.getTimeOutStopPM();
        }
        if (stopTime != null) {
            LocalTime now = LocalTime.now();
            if (now.isBefore(stopTime)) {
                throw new IllegalArgumentException("Cannot mark absentees yet. The attendance window is still open until " + stopTime + ".");
            }
        }
        List<Student> allStudents = studentRepository.findAll();
        List<Attendance> existingAttendance = repository.findByEvent(eventID);
        java.util.Set<String> checkedInStudents = new java.util.HashSet<>();
        java.util.Set<String> checkedOutStudents = new java.util.HashSet<>();
        
        for (Attendance att : existingAttendance) {
            if (att.getSession() != null && att.getSession().equalsIgnoreCase(sessionType)) {
                if ("TIME_IN".equals(att.getRecordType())) {
                    checkedInStudents.add(att.getStudID());
                } else if ("TIME_OUT".equals(att.getRecordType())) {
                    checkedOutStudents.add(att.getStudID());
                }
            }
        }
        
        for (Student student : allStudents) {
            String studID = student.getStudID();
            boolean hasTimeIn = checkedInStudents.contains(studID);
            boolean hasTimeOut = checkedOutStudents.contains(studID);
            
            if (!hasTimeIn && !hasTimeOut) {
                String attendanceID = "ATT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                LocalDateTime checkInTime = LocalDateTime.of(LocalDate.now(), LocalTime.now());
                Attendance absentRecord = new Attendance(
                    attendanceID,
                    studID,
                    eventID,
                    AttendanceStatus.ABSENT,
                    0,
                    isTimeIn ? checkInTime : null,
                    isTimeIn ? null : checkInTime,
                    "MANUAL"
                );
                absentRecord.setSession(sessionType);
                absentRecord.setRecordType(isTimeIn ? "TIME_IN" : "TIME_OUT");
                repository.save(absentRecord);
            } else if (!hasTimeIn && hasTimeOut) {
                Attendance timeOutRecord = findAttendanceByStudentEventSessionAndType(studID, eventID, sessionType, "TIME_OUT");
                if (timeOutRecord != null) {
                    AttendanceStatus halfAbsentStatus = "AM".equalsIgnoreCase(sessionType) ? 
                        AttendanceStatus.HALF_ABSENT_AM : AttendanceStatus.HALF_ABSENT_PM;
                    if (timeOutRecord.getStatus() != halfAbsentStatus) {
                        timeOutRecord.setStatus(halfAbsentStatus);
                        repository.update(timeOutRecord);
                        
                        if (fineService != null) {
                            Fine fine = fineService.createFineFromAttendance(timeOutRecord, eventID, event);
                            if (fine != null) {
                                fineService.saveFine(fine);
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean isWithinAttendanceWindow(Event event, String sessionType, boolean isTimeIn, LocalTime scannedTime) {
        LocalTime startTime = null;
        LocalTime stopTime = null;

        if ("AM".equalsIgnoreCase(sessionType)) {
            if (isTimeIn) {
                startTime = event.getTimeInStartAM();
                stopTime = event.getTimeInStopAM();
            } else {
                startTime = event.getTimeOutStartAM();
                stopTime = event.getTimeOutStopAM();
            }
        } else if ("PM".equalsIgnoreCase(sessionType)) {
            if (isTimeIn) {
                startTime = event.getTimeInStartPM();
                stopTime = event.getTimeInStopPM();
            } else {
                startTime = event.getTimeOutStartPM();
                stopTime = event.getTimeOutStopPM();
            }
        }

        if (startTime == null || stopTime == null) {
            return false;
        }

        return !scannedTime.isBefore(startTime) && !scannedTime.isAfter(stopTime);
    }

    private void checkAndUpdateHalfAbsentStatus(String studID, String eventID, String sessionType) {
        Attendance timeIn = findAttendanceByStudentEventSessionAndType(studID, eventID, sessionType, "TIME_IN");
        Attendance timeOut = findAttendanceByStudentEventSessionAndType(studID, eventID, sessionType, "TIME_OUT");
        
        if (timeOut != null && timeIn == null) {
            AttendanceStatus halfAbsentStatus = "AM".equalsIgnoreCase(sessionType) ? 
                AttendanceStatus.HALF_ABSENT_AM : AttendanceStatus.HALF_ABSENT_PM;
            
            if (timeOut.getStatus() != halfAbsentStatus) {
                timeOut.setStatus(halfAbsentStatus);
                repository.update(timeOut);
                
                if (fineService != null) {
                    Event event = eventRepository.findById(eventID);
                    Fine fine = fineService.createFineFromAttendance(timeOut, eventID, event);
                    if (fine != null) {
                        fineService.saveFine(fine);
                    }
                }
            }
        }
    }
}
