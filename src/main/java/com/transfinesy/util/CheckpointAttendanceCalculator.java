package com.transfinesy.util;

import com.transfinesy.model.Attendance;
import com.transfinesy.model.AttendanceSession;
import com.transfinesy.model.AttendanceStatus;
import com.transfinesy.model.Event;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Calculates attendance presence ratio based on dynamic checkpoints:
 * - MORNING_ONLY: 2 checkpoints (AM Time-In, AM Time-Out)
 * - AFTERNOON_ONLY: 2 checkpoints (PM Time-In, PM Time-Out)
 * - BOTH: 4 checkpoints (AM Time-In, AM Time-Out, PM Time-In, PM Time-Out)
 */
public class CheckpointAttendanceCalculator {
    
    /**
     * Represents checkpoints for attendance (dynamic based on session type)
     */
    public enum Checkpoint {
        AM_TIME_IN("AM", "TIME_IN"),
        AM_TIME_OUT("AM", "TIME_OUT"),
        PM_TIME_IN("PM", "TIME_IN"),
        PM_TIME_OUT("PM", "TIME_OUT");
        
        private final String session;
        private final String recordType;
        
        Checkpoint(String session, String recordType) {
            this.session = session;
            this.recordType = recordType;
        }
        
        public String getSession() {
            return session;
        }
        
        public String getRecordType() {
            return recordType;
        }
    }
    
    /**
     * Get total number of checkpoints for an event based on session type
     */
    public static int getTotalCheckpoints(Event event) {
        if (event == null || event.getSessionType() == null) {
            return 4; // Default to BOTH if not specified
        }
        
        switch (event.getSessionType()) {
            case MORNING_ONLY:
                return 2; // AM Time-In, AM Time-Out
            case AFTERNOON_ONLY:
                return 2; // PM Time-In, PM Time-Out
            case BOTH:
                return 4; // AM Time-In, AM Time-Out, PM Time-In, PM Time-Out
            default:
                return 4;
        }
    }
    
    /**
     * Get list of checkpoints for an event based on session type
     */
    public static List<Checkpoint> getCheckpointsForEvent(Event event) {
        List<Checkpoint> checkpoints = new ArrayList<>();
        
        if (event == null || event.getSessionType() == null) {
            // Default to BOTH if not specified
            checkpoints.add(Checkpoint.AM_TIME_IN);
            checkpoints.add(Checkpoint.AM_TIME_OUT);
            checkpoints.add(Checkpoint.PM_TIME_IN);
            checkpoints.add(Checkpoint.PM_TIME_OUT);
            return checkpoints;
        }
        
        switch (event.getSessionType()) {
            case MORNING_ONLY:
                checkpoints.add(Checkpoint.AM_TIME_IN);
                checkpoints.add(Checkpoint.AM_TIME_OUT);
                break;
            case AFTERNOON_ONLY:
                checkpoints.add(Checkpoint.PM_TIME_IN);
                checkpoints.add(Checkpoint.PM_TIME_OUT);
                break;
            case BOTH:
                checkpoints.add(Checkpoint.AM_TIME_IN);
                checkpoints.add(Checkpoint.AM_TIME_OUT);
                checkpoints.add(Checkpoint.PM_TIME_IN);
                checkpoints.add(Checkpoint.PM_TIME_OUT);
                break;
        }
        
        return checkpoints;
    }
    
    /**
     * Checkpoint status for a student
     */
    public static class CheckpointStatus {
        private final Checkpoint checkpoint;
        private final AttendanceStatus status;
        private final int minutesLate;
        private final LocalDateTime scanTime;
        
        public CheckpointStatus(Checkpoint checkpoint, AttendanceStatus status, int minutesLate, LocalDateTime scanTime) {
            this.checkpoint = checkpoint;
            this.status = status;
            this.minutesLate = minutesLate;
            this.scanTime = scanTime;
        }
        
        public Checkpoint getCheckpoint() {
            return checkpoint;
        }
        
        public AttendanceStatus getStatus() {
            return status;
        }
        
        public int getMinutesLate() {
            return minutesLate;
        }
        
        public LocalDateTime getScanTime() {
            return scanTime;
        }
        
        public boolean isPresent() {
            return status == AttendanceStatus.PRESENT || status == AttendanceStatus.LATE;
        }
    }
    
    /**
     * Calculate presence ratio and fine amount for a student
     */
    public static class AttendanceResult {
        private final int presentCount;
        private final double presenceRatio;
        private final double fineAmount;
        private final int totalLateMinutes;
        private final Map<Checkpoint, CheckpointStatus> checkpointStatuses;
        
        public AttendanceResult(int presentCount, double presenceRatio, double fineAmount, 
                               int totalLateMinutes, Map<Checkpoint, CheckpointStatus> checkpointStatuses) {
            this.presentCount = presentCount;
            this.presenceRatio = presenceRatio;
            this.fineAmount = fineAmount;
            this.totalLateMinutes = totalLateMinutes;
            this.checkpointStatuses = checkpointStatuses;
        }
        
        public int getPresentCount() {
            return presentCount;
        }
        
        public double getPresenceRatio() {
            return presenceRatio;
        }
        
        public double getFineAmount() {
            return fineAmount;
        }
        
        public int getTotalLateMinutes() {
            return totalLateMinutes;
        }
        
        public Map<Checkpoint, CheckpointStatus> getCheckpointStatuses() {
            return checkpointStatuses;
        }
    }
    
    /**
     * Calculate attendance result for a student based on their attendance records
     */
    public static AttendanceResult calculateAttendanceResult(String studID, String eventID, 
                                                             List<Attendance> attendances, Event event) {
        Map<Checkpoint, CheckpointStatus> checkpointStatuses = new HashMap<>();
        
        // Get checkpoints for this event based on session type
        List<Checkpoint> eventCheckpoints = getCheckpointsForEvent(event);
        int totalCheckpoints = getTotalCheckpoints(event);
        
        // Initialize only relevant checkpoints as ABSENT
        for (Checkpoint checkpoint : eventCheckpoints) {
            checkpointStatuses.put(checkpoint, new CheckpointStatus(
                checkpoint, AttendanceStatus.ABSENT, 0, null
            ));
        }
        
        // Process actual attendance records
        for (Attendance att : attendances) {
            if (!att.getStudID().equals(studID) || !att.getEventID().equals(eventID)) {
                continue;
            }
            
            String session = att.getSession();
            String recordType = att.getRecordType();
            
            if (session == null || recordType == null) {
                continue;
            }
            
            Checkpoint checkpoint = findCheckpoint(session, recordType);
            if (checkpoint != null) {
                LocalDateTime scanTime = "TIME_IN".equals(recordType) ? att.getCheckInTime() : att.getCheckOutTime();
                checkpointStatuses.put(checkpoint, new CheckpointStatus(
                    checkpoint, att.getStatus(), att.getMinutesLate(), scanTime
                ));
            }
        }
        
        // Calculate presence count and total late minutes
        int presentCount = 0;
        int totalLateMinutes = 0;
        
        for (CheckpointStatus status : checkpointStatuses.values()) {
            if (status.isPresent()) {
                presentCount++;
            }
            totalLateMinutes += status.getMinutesLate();
        }
        
        double presenceRatio = totalCheckpoints > 0 ? (double) presentCount / totalCheckpoints : 0.0;
        double fineAmount = calculateFineAmount(presentCount, totalCheckpoints, event);
        
        return new AttendanceResult(presentCount, presenceRatio, fineAmount, totalLateMinutes, checkpointStatuses);
    }
    
    /**
     * Find checkpoint enum from session and recordType
     */
    private static Checkpoint findCheckpoint(String session, String recordType) {
        if (session == null || recordType == null) {
            return null;
        }
        
        for (Checkpoint checkpoint : Checkpoint.values()) {
            if (checkpoint.getSession().equalsIgnoreCase(session) && 
                checkpoint.getRecordType().equals(recordType)) {
                return checkpoint;
            }
        }
        
        return null;
    }
    
    /**
     * Calculate fine amount based on presence count and total checkpoints
     * Uses proportional formula: Fine = FULL_ABSENT_FINE × (1 - presenceRatio)
     * 
     * Examples:
     * - 2-checkpoint event: 2/2 → ₱0, 1/2 → ₱50, 0/2 → ₱100
     * - 4-checkpoint event: 4/4 → ₱0, 3/4 → ₱25, 2/4 → ₱50, 1/4 → ₱75, 0/4 → ₱100
     */
    public static double calculateFineAmount(int presentCount, int totalCheckpoints, Event event) {
        double fullAbsentFine = event != null && event.getFineAmountAbsent() != null 
            ? event.getFineAmountAbsent() 
            : 100.0;
        
        if (totalCheckpoints <= 0) {
            return fullAbsentFine;
        }
        
        // Calculate presence ratio
        double presenceRatio = (double) presentCount / totalCheckpoints;
        
        // Fine = FULL_ABSENT_FINE × (1 - presenceRatio)
        double fineAmount = fullAbsentFine * (1.0 - presenceRatio);
        
        // Round to 2 decimal places
        return Math.round(fineAmount * 100.0) / 100.0;
    }
    
    /**
     * Get checkpoint stop time from event
     */
    public static LocalTime getCheckpointStopTime(Checkpoint checkpoint, Event event) {
        if (event == null) {
            return null;
        }
        
        switch (checkpoint) {
            case AM_TIME_IN:
                return event.getTimeInStopAM();
            case AM_TIME_OUT:
                return event.getTimeOutStopAM();
            case PM_TIME_IN:
                return event.getTimeInStopPM();
            case PM_TIME_OUT:
                return event.getTimeOutStopPM();
            default:
                return null;
        }
    }
    
    /**
     * Calculate minutes late for a checkpoint
     */
    public static int calculateMinutesLate(LocalDateTime scanTime, Checkpoint checkpoint, Event event) {
        if (scanTime == null || event == null) {
            return 0;
        }
        
        LocalTime stopTime = getCheckpointStopTime(checkpoint, event);
        if (stopTime == null) {
            return 0;
        }
        
        LocalTime scanLocalTime = scanTime.toLocalTime();
        if (scanLocalTime.isAfter(stopTime)) {
            return (int) java.time.temporal.ChronoUnit.MINUTES.between(stopTime, scanLocalTime);
        }
        
        return 0;
    }
    
    /**
     * Calculate checkpoint completion for a specific session (AM or PM)
     * Returns: presentCount / totalCheckpoints (2 for AM or PM)
     */
    public static class SessionCheckpointResult {
        private final int presentCount;
        private final int totalCheckpoints;
        private final int lateMinutes;
        
        public SessionCheckpointResult(int presentCount, int totalCheckpoints, int lateMinutes) {
            this.presentCount = presentCount;
            this.totalCheckpoints = totalCheckpoints;
            this.lateMinutes = lateMinutes;
        }
        
        public int getPresentCount() {
            return presentCount;
        }
        
        public int getTotalCheckpoints() {
            return totalCheckpoints;
        }
        
        public int getLateMinutes() {
            return lateMinutes;
        }
        
        public String getFraction() {
            return presentCount + "/" + totalCheckpoints;
        }
    }
    
    /**
     * Calculate checkpoint completion for AM session
     */
    public static SessionCheckpointResult calculateAMSessionCheckpoints(String studID, String eventID, 
                                                                        List<Attendance> attendances) {
        int presentCount = 0;
        int lateMinutes = 0;
        
        for (Attendance att : attendances) {
            if (!att.getStudID().equals(studID) || !att.getEventID().equals(eventID)) {
                continue;
            }
            
            String session = att.getSession();
            if (session != null && "AM".equalsIgnoreCase(session)) {
                if (att.getStatus() == AttendanceStatus.PRESENT || att.getStatus() == AttendanceStatus.LATE) {
                    presentCount++;
                }
                lateMinutes += att.getMinutesLate();
            }
        }
        
        return new SessionCheckpointResult(presentCount, 2, lateMinutes);
    }
    
    /**
     * Calculate checkpoint completion for PM session
     */
    public static SessionCheckpointResult calculatePMSessionCheckpoints(String studID, String eventID, 
                                                                        List<Attendance> attendances) {
        int presentCount = 0;
        int lateMinutes = 0;
        
        for (Attendance att : attendances) {
            if (!att.getStudID().equals(studID) || !att.getEventID().equals(eventID)) {
                continue;
            }
            
            String session = att.getSession();
            if (session != null && "PM".equalsIgnoreCase(session)) {
                if (att.getStatus() == AttendanceStatus.PRESENT || att.getStatus() == AttendanceStatus.LATE) {
                    presentCount++;
                }
                lateMinutes += att.getMinutesLate();
            }
        }
        
        return new SessionCheckpointResult(presentCount, 2, lateMinutes);
    }
}

