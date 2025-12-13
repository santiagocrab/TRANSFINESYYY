package com.transfinesy.service;

import com.transfinesy.model.Attendance;
import com.transfinesy.model.AttendanceStatus;
import com.transfinesy.model.Event;
import com.transfinesy.model.Fine;
import com.transfinesy.repo.FineRepository;
import com.transfinesy.repo.FineRepositoryImpl;
import com.transfinesy.util.CheckpointAttendanceCalculator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class FineService {
    private FineRepository repository;
    private LedgerService ledgerService;

    private static final double DEFAULT_FINE_ABSENT = 100.0;
    private static final double DEFAULT_FINE_PER_MINUTE_LATE = 2.0;
    private static final double DEFAULT_MIN_FINE_LATE = 20.0;

    public FineService() {
        this.repository = new FineRepositoryImpl();
        this.ledgerService = new LedgerService();
    }

    public FineService(LedgerService ledgerService) {
        this.repository = new FineRepositoryImpl();
        this.ledgerService = ledgerService;
    }

    public double calculateFineAmount(AttendanceStatus status, int minutesLate) {
        return calculateFineAmount(status, minutesLate, null);
    }

    /**
     * Calculate fine amount based on checkpoint presence ratio
     * This is the new method that uses 4-checkpoint logic
     */
    public double calculateFineAmountFromCheckpoints(String studID, String eventID, 
                                                      List<Attendance> attendances, Event event) {
        CheckpointAttendanceCalculator.AttendanceResult result = 
            CheckpointAttendanceCalculator.calculateAttendanceResult(studID, eventID, attendances, event);
        return result.getFineAmount();
    }
    
    /**
     * Legacy method - kept for backward compatibility
     * For new checkpoint-based system, use calculateFineAmountFromCheckpoints
     */
    public double calculateFineAmount(AttendanceStatus status, int minutesLate, Event event) {
        switch (status) {
            case ABSENT:
                double fineAbsent;
                if (event != null && event.getFineAmountAbsent() != null) {
                    fineAbsent = event.getFineAmountAbsent();
                } else {
                    fineAbsent = DEFAULT_FINE_ABSENT;
                }
                return fineAbsent;
            case HALF_ABSENT_AM:
            case HALF_ABSENT_PM:
                double fineAbsentHalf;
                if (event != null && event.getFineAmountAbsent() != null) {
                    fineAbsentHalf = event.getFineAmountAbsent();
                } else {
                    fineAbsentHalf = DEFAULT_FINE_ABSENT;
                }
                return fineAbsentHalf / 2.0;
            case LATE:
                double finePerMinute;
                if (event != null && event.getFineAmountLate() != null) {
                    finePerMinute = event.getFineAmountLate();
                } else {
                    finePerMinute = DEFAULT_FINE_PER_MINUTE_LATE;
                }
                double lateFine = minutesLate * finePerMinute;
                double minFine;
                if (event != null && event.getFineAmountLate() != null) {
                    minFine = DEFAULT_MIN_FINE_LATE;
                } else {
                    minFine = DEFAULT_MIN_FINE_LATE;
                }
                return Math.max(lateFine, minFine);
            default:
                return 0.0;
        }
    }

    public Fine createFineFromAttendance(Attendance attendance, String eventID) {
        return createFineFromAttendance(attendance, eventID, null);
    }

    public Fine createFineFromAttendance(Attendance attendance, String eventID, Event event) {
        if (attendance == null) {
            throw new IllegalArgumentException("Attendance cannot be null");
        }
        double fineAmount = calculateFineAmount(attendance.getStatus(), attendance.getMinutesLate(), event);
        if (fineAmount <= 0) {
            return null;
        }
        String fineID = "FINE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String transactionID = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Fine fine = new Fine(
            fineID,
            transactionID,
            attendance.getStudID(),
            eventID,
            fineAmount,
            LocalDate.now()
        );
        return fine;
    }

    public void saveFine(Fine fine) {
        if (fine == null) {
            throw new IllegalArgumentException("Fine cannot be null");
        }
        if (fine.getFineAmount() <= 0) {
            throw new IllegalArgumentException("Fine amount must be greater than 0. Amount: " + fine.getFineAmount());
        }
        try {
            repository.save(fine);
            System.out.println("✓ Fine saved to database: " + fine.getFineID() + " for student " + fine.getStudID() + " amount: ₱" + fine.getFineAmount());
            ledgerService.addTransactionToLedger(fine.getStudID(), fine);
        } catch (Exception e) {
            System.err.println("✗ CRITICAL ERROR saving fine to database: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save fine to database", e);
        }
    }

    public void generateFinesFromAttendances(List<Attendance> attendances, String eventID) {
        generateFinesFromAttendances(attendances, eventID, null);
    }

    public void generateFinesFromAttendances(List<Attendance> attendances, String eventID, Event event) {
        System.out.println("\n=== STARTING FINE GENERATION ===");
        System.out.println("Event ID: " + eventID);
        System.out.println("Event: " + (event != null ? event.getEventName() : "NULL"));
        System.out.println("Event Fine Absent: " + (event != null && event.getFineAmountAbsent() != null ? event.getFineAmountAbsent() : "DEFAULT (100.0)"));
        System.out.println("Event Fine Late: " + (event != null && event.getFineAmountLate() != null ? event.getFineAmountLate() : "DEFAULT (2.0/min)"));
        System.out.println("Total attendance records: " + attendances.size());
        
        java.util.Set<String> processedStudents = new java.util.HashSet<>();
        List<Fine> existingFines = getFinesByEvent(eventID);
        System.out.println("Existing fines in database: " + existingFines.size());
        
        java.util.Set<String> studentsWithFines = existingFines.stream()
            .map(Fine::getStudID)
            .collect(java.util.stream.Collectors.toSet());
        
        java.util.Set<String> allStudentIDs = attendances.stream()
            .map(Attendance::getStudID)
            .collect(java.util.stream.Collectors.toSet());
        
        System.out.println("Unique student IDs: " + allStudentIDs.size());
        System.out.println("Students with existing fines: " + studentsWithFines.size());
        
        int finesCreated = 0;
        int finesSkipped = 0;
        int finesFailed = 0;
        double totalFinesAmount = 0.0;
        
        for (String studID : allStudentIDs) {
            // Skip if already processed in this run
            if (processedStudents.contains(studID)) {
                finesSkipped++;
                continue;
            }
            
            // If student already has a fine for this event, skip (don't create duplicate)
            if (studentsWithFines.contains(studID)) {
                finesSkipped++;
                continue;
            }
            
            // Use new checkpoint-based calculation
            CheckpointAttendanceCalculator.AttendanceResult result = 
                CheckpointAttendanceCalculator.calculateAttendanceResult(studID, eventID, attendances, event);
            
            int totalCheckpoints = CheckpointAttendanceCalculator.getTotalCheckpoints(event);
            
            System.out.println("Student " + studID + " - Checkpoint Analysis:");
            System.out.println("  Present Count: " + result.getPresentCount() + "/" + totalCheckpoints);
            System.out.println("  Presence Ratio: " + String.format("%.2f", result.getPresenceRatio()));
            System.out.println("  Total Late Minutes: " + result.getTotalLateMinutes());
            System.out.println("  Calculated Fine Amount: ₱" + result.getFineAmount());
            
            // Only create fine if amount > 0
            if (result.getFineAmount() > 0) {
                try {
                    String fineID = "FINE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                    String transactionID = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                    Fine fine = new Fine(
                        fineID,
                        transactionID,
                        studID,
                        eventID,
                        result.getFineAmount(),
                        LocalDate.now()
                    );
                    
                    System.out.println("  Attempting to save fine: " + fineID + " for student " + studID);
                    saveFine(fine);
                    processedStudents.add(studID);
                    finesCreated++;
                    totalFinesAmount += result.getFineAmount();
                    System.out.println("  ✓ SUCCESS: Saved fine for student " + studID + ": ₱" + result.getFineAmount() + 
                                     " (Present: " + result.getPresentCount() + "/" + totalCheckpoints + ")");
                } catch (Exception e) {
                    finesFailed++;
                    System.err.println("  ✗ FAILED: Error saving fine for student " + studID + ": " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("  Student " + studID + " has no fine (Present: " + result.getPresentCount() + "/" + 
                                 totalCheckpoints + ")");
            }
        }
        
        System.out.println("\n=== FINE GENERATION SUMMARY ===");
        System.out.println("Total fines created: " + finesCreated);
        System.out.println("Total fines skipped (already exist): " + finesSkipped);
        System.out.println("Total fines failed: " + finesFailed);
        System.out.println("Total amount: ₱" + totalFinesAmount);
        
        // Verify by querying database
        List<Fine> verifyFines = getFinesByEvent(eventID);
        double verifyTotal = verifyFines.stream().mapToDouble(Fine::getFineAmount).sum();
        System.out.println("Database verification: " + verifyFines.size() + " fines, total: ₱" + verifyTotal);
        System.out.println("==============================\n");
    }
    
    private AttendanceStatus determineFinalAttendanceStatus(List<Attendance> attendances, String studID, String eventID) {
        List<Attendance> studentAttendances = attendances.stream()
            .filter(a -> a.getStudID().equals(studID) && a.getEventID().equals(eventID))
            .collect(java.util.stream.Collectors.toList());
        
        if (studentAttendances.isEmpty()) {
            return AttendanceStatus.ABSENT;
        }
        
        boolean hasAMTimeIn = studentAttendances.stream()
            .anyMatch(a -> "AM".equalsIgnoreCase(a.getSession()) && "TIME_IN".equals(a.getRecordType()));
        boolean hasAMTimeOut = studentAttendances.stream()
            .anyMatch(a -> "AM".equalsIgnoreCase(a.getSession()) && "TIME_OUT".equals(a.getRecordType()));
        boolean hasPMTimeIn = studentAttendances.stream()
            .anyMatch(a -> "PM".equalsIgnoreCase(a.getSession()) && "TIME_IN".equals(a.getRecordType()));
        boolean hasPMTimeOut = studentAttendances.stream()
            .anyMatch(a -> "PM".equalsIgnoreCase(a.getSession()) && "TIME_OUT".equals(a.getRecordType()));
        
        AttendanceStatus amStatus = null;
        AttendanceStatus pmStatus = null;
        
        if (!hasAMTimeIn && !hasAMTimeOut) {
            amStatus = AttendanceStatus.ABSENT;
        } else if (!hasAMTimeIn && hasAMTimeOut) {
            amStatus = AttendanceStatus.HALF_ABSENT_AM;
        } else {
            Attendance amTimeIn = studentAttendances.stream()
                .filter(a -> "AM".equalsIgnoreCase(a.getSession()) && "TIME_IN".equals(a.getRecordType()))
                .findFirst()
                .orElse(null);
            if (amTimeIn != null) {
                amStatus = amTimeIn.getStatus();
            }
        }
        
        if (!hasPMTimeIn && !hasPMTimeOut) {
            pmStatus = AttendanceStatus.ABSENT;
        } else if (!hasPMTimeIn && hasPMTimeOut) {
            pmStatus = AttendanceStatus.HALF_ABSENT_PM;
        } else {
            Attendance pmTimeIn = studentAttendances.stream()
                .filter(a -> "PM".equalsIgnoreCase(a.getSession()) && "TIME_IN".equals(a.getRecordType()))
                .findFirst()
                .orElse(null);
            if (pmTimeIn != null) {
                pmStatus = pmTimeIn.getStatus();
            }
        }
        
        if (amStatus == AttendanceStatus.ABSENT && pmStatus == AttendanceStatus.ABSENT) {
            return AttendanceStatus.ABSENT;
        } else if (amStatus == AttendanceStatus.HALF_ABSENT_AM || pmStatus == AttendanceStatus.HALF_ABSENT_PM) {
            if (amStatus == AttendanceStatus.HALF_ABSENT_AM && pmStatus == AttendanceStatus.HALF_ABSENT_PM) {
                return AttendanceStatus.ABSENT;
            } else if (amStatus == AttendanceStatus.HALF_ABSENT_AM) {
                return AttendanceStatus.HALF_ABSENT_AM;
            } else {
                return AttendanceStatus.HALF_ABSENT_PM;
            }
        } else if (amStatus == AttendanceStatus.LATE || pmStatus == AttendanceStatus.LATE) {
            return AttendanceStatus.LATE;
        } else {
            return AttendanceStatus.PRESENT;
        }
    }
    
    private Attendance findRepresentativeAttendance(List<Attendance> attendances, String studID, String eventID, AttendanceStatus targetStatus) {
        if (targetStatus == AttendanceStatus.HALF_ABSENT_AM) {
            return attendances.stream()
                .filter(a -> a.getStudID().equals(studID) && a.getEventID().equals(eventID))
                .filter(a -> a.getStatus() == AttendanceStatus.HALF_ABSENT_AM || 
                           ("AM".equalsIgnoreCase(a.getSession()) && "TIME_OUT".equals(a.getRecordType())))
                .findFirst()
                .orElse(null);
        } else if (targetStatus == AttendanceStatus.HALF_ABSENT_PM) {
            return attendances.stream()
                .filter(a -> a.getStudID().equals(studID) && a.getEventID().equals(eventID))
                .filter(a -> a.getStatus() == AttendanceStatus.HALF_ABSENT_PM || 
                           ("PM".equalsIgnoreCase(a.getSession()) && "TIME_OUT".equals(a.getRecordType())))
                .findFirst()
                .orElse(null);
        } else if (targetStatus == AttendanceStatus.ABSENT) {
            // For ABSENT status, first try to find any attendance record with ABSENT status
            Attendance absentRecord = attendances.stream()
                .filter(a -> a.getStudID().equals(studID) && a.getEventID().equals(eventID))
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                .findFirst()
                .orElse(null);
            
            if (absentRecord != null) {
                return absentRecord;
            }
            
            // If no ABSENT record found, check for TIME_IN records with ABSENT status
            return attendances.stream()
                .filter(a -> a.getStudID().equals(studID) && a.getEventID().equals(eventID))
                .filter(a -> "TIME_IN".equals(a.getRecordType()) && a.getStatus() == AttendanceStatus.ABSENT)
                .findFirst()
                .orElse(null);
        } else {
            Attendance timeInRecord = attendances.stream()
                .filter(a -> a.getStudID().equals(studID) && a.getEventID().equals(eventID))
                .filter(a -> "TIME_IN".equals(a.getRecordType()))
                .findFirst()
                .orElse(null);
            
            if (timeInRecord != null && timeInRecord.getStatus() == targetStatus) {
                return timeInRecord;
            }
            
            return attendances.stream()
                .filter(a -> a.getStudID().equals(studID) && a.getEventID().equals(eventID))
                .filter(a -> a.getStatus() == targetStatus)
                .findFirst()
                .orElse(null);
        }
    }

    public List<Fine> getFinesByStudent(String studID) {
        return repository.findByStudent(studID);
    }

    public List<Fine> getFinesByEvent(String eventID) {
        return repository.findByEvent(eventID);
    }

    public Fine getFineById(String fineID) {
        return repository.findById(fineID);
    }

    public List<Fine> getAllFines() {
        return repository.findAll();
    }

    public void deleteFine(String fineID) {
        repository.delete(fineID);
    }
}
