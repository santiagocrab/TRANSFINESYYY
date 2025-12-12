package com.transfinesy.web;

import com.transfinesy.model.Attendance;
import com.transfinesy.model.AttendanceStatus;
import com.transfinesy.model.Event;
import com.transfinesy.model.Student;
import com.transfinesy.service.AttendanceService;
import com.transfinesy.service.EventService;
import com.transfinesy.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final EventService eventService;
    private final StudentService studentService;

    public AttendanceController(AttendanceService attendanceService, EventService eventService,
                                 StudentService studentService) {
        this.attendanceService = attendanceService;
        this.eventService = eventService;
        this.studentService = studentService;
    }

    @GetMapping("/event/{eventId}")
    public String eventAttendance(@PathVariable String eventId, Model model) {
        Event event = eventService.getEventById(eventId);
        if (event == null) {
            return "redirect:/events";
        }

        List<Attendance> allAttendances = attendanceService.getAttendanceByEvent(eventId);
        List<Student> allStudents = studentService.getAllStudents();

        Map<String, Student> studentMap = new HashMap<>();
        for (Student student : allStudents) {
            studentMap.put(student.getStudID(), student);
        }

        List<Attendance> amAttendances = new java.util.ArrayList<>();
        List<Attendance> pmAttendances = new java.util.ArrayList<>();

        for (Attendance attendance : allAttendances) {
            String session = attendance.getSession();
            if (session != null && (session.equalsIgnoreCase("AM") || session.equalsIgnoreCase("PM"))) {
                if ("AM".equalsIgnoreCase(session)) {
                    amAttendances.add(attendance);
                } else if ("PM".equalsIgnoreCase(session)) {
                    pmAttendances.add(attendance);
                }
            }
        }

        // Get fines for this event
        com.transfinesy.service.FineService fineService = new com.transfinesy.service.FineService();
        List<com.transfinesy.model.Fine> eventFines = fineService.getFinesByEvent(eventId);
        Map<String, com.transfinesy.model.Fine> fineMap = new HashMap<>();
        for (com.transfinesy.model.Fine fine : eventFines) {
            fineMap.put(fine.getStudID(), fine);
        }

        // Group attendances by final status (per student) using FineService logic
        Map<String, AttendanceStatus> studentFinalStatus = new HashMap<>();
        Map<String, Attendance> studentRepresentativeAttendance = new HashMap<>();
        
        // Get unique student IDs
        java.util.Set<String> uniqueStudentIDs = new java.util.HashSet<>();
        for (Attendance att : allAttendances) {
            uniqueStudentIDs.add(att.getStudID());
        }
        
        for (String studID : uniqueStudentIDs) {
            List<Attendance> studentAttendances = allAttendances.stream()
                .filter(a -> a.getStudID().equals(studID))
                .collect(java.util.stream.Collectors.toList());
            
            // Determine final status using similar logic to FineService
            AttendanceStatus finalStatus = determineFinalStatusForStudent(studentAttendances, eventId);
            studentFinalStatus.put(studID, finalStatus);
            
            // Find representative attendance
            Attendance representative = findRepresentativeAttendance(studentAttendances, finalStatus);
            if (representative != null) {
                studentRepresentativeAttendance.put(studID, representative);
            }
        }

        // Separate students by status
        List<Map<String, Object>> absentStudents = new java.util.ArrayList<>();
        List<Map<String, Object>> lateStudents = new java.util.ArrayList<>();
        List<Map<String, Object>> presentStudents = new java.util.ArrayList<>();

        for (String studID : studentFinalStatus.keySet()) {
            AttendanceStatus status = studentFinalStatus.get(studID);
            Student student = studentMap.get(studID);
            com.transfinesy.model.Fine fine = fineMap.get(studID);
            double fineAmount = (fine != null) ? fine.getFineAmount() : 0.0;

            Map<String, Object> studentInfo = new HashMap<>();
            studentInfo.put("student", student);
            studentInfo.put("status", status);
            studentInfo.put("fineAmount", fineAmount);
            studentInfo.put("attendance", studentRepresentativeAttendance.get(studID));

            if (status == AttendanceStatus.ABSENT || status == AttendanceStatus.HALF_ABSENT_AM || status == AttendanceStatus.HALF_ABSENT_PM) {
                absentStudents.add(studentInfo);
            } else if (status == AttendanceStatus.LATE) {
                lateStudents.add(studentInfo);
            } else if (status == AttendanceStatus.PRESENT) {
                presentStudents.add(studentInfo);
            }
        }

        // Calculate totals
        double totalAbsentFine = absentStudents.stream()
            .mapToDouble(s -> (Double) s.get("fineAmount"))
            .sum();
        double totalLateFine = lateStudents.stream()
            .mapToDouble(s -> (Double) s.get("fineAmount"))
            .sum();
        double totalPresentFine = presentStudents.stream()
            .mapToDouble(s -> (Double) s.get("fineAmount"))
            .sum();

        model.addAttribute("pageTitle", "Event Attendance");
        model.addAttribute("activePage", "events");
        model.addAttribute("event", event);
        model.addAttribute("attendances", allAttendances);
        model.addAttribute("amAttendances", amAttendances);
        model.addAttribute("pmAttendances", pmAttendances);
        model.addAttribute("students", allStudents);
        model.addAttribute("studentMap", studentMap);
        model.addAttribute("absentStudents", absentStudents);
        model.addAttribute("lateStudents", lateStudents);
        model.addAttribute("presentStudents", presentStudents);
        model.addAttribute("totalAbsentFine", totalAbsentFine);
        model.addAttribute("totalLateFine", totalLateFine);
        model.addAttribute("totalPresentFine", totalPresentFine);

        return "attendance/event";
    }

    private AttendanceStatus determineFinalStatusForStudent(List<Attendance> attendances, String eventID) {
        if (attendances.isEmpty()) {
            return AttendanceStatus.ABSENT;
        }
        
        boolean hasAMTimeIn = attendances.stream()
            .anyMatch(a -> "AM".equalsIgnoreCase(a.getSession()) && "TIME_IN".equals(a.getRecordType()));
        boolean hasAMTimeOut = attendances.stream()
            .anyMatch(a -> "AM".equalsIgnoreCase(a.getSession()) && "TIME_OUT".equals(a.getRecordType()));
        boolean hasPMTimeIn = attendances.stream()
            .anyMatch(a -> "PM".equalsIgnoreCase(a.getSession()) && "TIME_IN".equals(a.getRecordType()));
        boolean hasPMTimeOut = attendances.stream()
            .anyMatch(a -> "PM".equalsIgnoreCase(a.getSession()) && "TIME_OUT".equals(a.getRecordType()));
        
        AttendanceStatus amStatus = null;
        AttendanceStatus pmStatus = null;
        
        if (!hasAMTimeIn && !hasAMTimeOut) {
            amStatus = AttendanceStatus.ABSENT;
        } else if (!hasAMTimeIn && hasAMTimeOut) {
            amStatus = AttendanceStatus.HALF_ABSENT_AM;
        } else {
            Attendance amTimeIn = attendances.stream()
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
            Attendance pmTimeIn = attendances.stream()
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
    
    private Attendance findRepresentativeAttendance(List<Attendance> attendances, AttendanceStatus targetStatus) {
        if (targetStatus == AttendanceStatus.HALF_ABSENT_AM) {
            return attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.HALF_ABSENT_AM || 
                           ("AM".equalsIgnoreCase(a.getSession()) && "TIME_OUT".equals(a.getRecordType())))
                .findFirst()
                .orElse(null);
        } else if (targetStatus == AttendanceStatus.HALF_ABSENT_PM) {
            return attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.HALF_ABSENT_PM || 
                           ("PM".equalsIgnoreCase(a.getSession()) && "TIME_OUT".equals(a.getRecordType())))
                .findFirst()
                .orElse(null);
        } else {
            Attendance timeInRecord = attendances.stream()
                .filter(a -> "TIME_IN".equals(a.getRecordType()))
                .findFirst()
                .orElse(null);
            
            if (timeInRecord != null && timeInRecord.getStatus() == targetStatus) {
                return timeInRecord;
            }
            
            return attendances.stream()
                .filter(a -> a.getStatus() == targetStatus)
                .findFirst()
                .orElse(null);
        }
    }

    @PostMapping("/scan-rfid")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> scanRFID(@RequestParam String rfidTag, @RequestParam String eventId) {
        Map<String, Object> response = new HashMap<>();

        try {

            if (rfidTag == null || rfidTag.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "RFID tag is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (eventId == null || eventId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Event ID is required");
                return ResponseEntity.badRequest().body(response);
            }

            eventId = eventId.trim();

            Event event = eventService.getEventById(eventId);
            if (event == null) {
                response.put("success", false);
                response.put("message", "Event not found with ID: " + eventId);
                return ResponseEntity.badRequest().body(response);
            }

            response.put("success", false);
            response.put("message", "This endpoint is deprecated. Please use /scan-rfid-window with session type (AM/PM).");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/finalize/{eventId}")
    public String finalizeEvent(@PathVariable String eventId, RedirectAttributes redirectAttributes) {
        try {
            attendanceService.finalizeEventAttendance(eventId);
            redirectAttributes.addFlashAttribute("successMessage", "Event finalized successfully. Fines have been generated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error finalizing event: " + e.getMessage());
        }
        return "redirect:/attendance/event/" + eventId;
    }

    @PostMapping("/scan-rfid-window")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> scanRFIDWithWindow(
            @RequestParam String rfidTag,
            @RequestParam String eventId,
            @RequestParam String sessionType,
            @RequestParam(defaultValue = "true") boolean isTimeIn) {
        Map<String, Object> response = new HashMap<>();

        try {

            if (rfidTag == null || rfidTag.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "RFID tag is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (eventId == null || eventId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Event ID is required");
                return ResponseEntity.badRequest().body(response);
            }

            eventId = eventId.trim();

            Event event = eventService.getEventById(eventId);
            if (event == null) {
                response.put("success", false);
                response.put("message", "Event not found with ID: " + eventId);
                return ResponseEntity.badRequest().body(response);
            }

            Attendance attendance = attendanceService.scanRFIDWithWindow(rfidTag, event, sessionType, isTimeIn);
            Student student = studentService.getStudentById(attendance.getStudID());

            response.put("success", true);
            response.put("message", "Attendance recorded successfully");
            response.put("studentName", student.getFullName());
            response.put("status", attendance.getStatus().toString());
            response.put("minutesLate", attendance.getMinutesLate());
            response.put("checkInTime", attendance.getCheckInTime());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/mark-absentees/{eventId}")
    public String markAbsentees(
            @PathVariable String eventId,
            @RequestParam String sessionType,
            @RequestParam(defaultValue = "true") boolean isTimeIn,
            RedirectAttributes redirectAttributes) {
        try {
            attendanceService.markSessionAbsentees(eventId, sessionType, isTimeIn);
            redirectAttributes.addFlashAttribute("successMessage",
                "Absentees marked for " + sessionType + " " + (isTimeIn ? "Time-In" : "Time-Out") + " session.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error marking absentees: " + e.getMessage());
        }
        return "redirect:/attendance/event/" + eventId;
    }
}

