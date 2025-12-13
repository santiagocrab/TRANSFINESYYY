package com.transfinesy.web;

import com.transfinesy.model.Event;
import com.transfinesy.service.AttendanceService;
import com.transfinesy.service.EventService;
import com.transfinesy.service.LedgerService;
import com.transfinesy.service.ReportService;
import com.transfinesy.service.StudentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final ReportService reportService;
    private final AttendanceService attendanceService;
    private final StudentService studentService;
    private final EventService eventService;
    private final LedgerService ledgerService;

    public DashboardController(ReportService reportService, AttendanceService attendanceService, StudentService studentService, EventService eventService, LedgerService ledgerService) {
        this.reportService = reportService;
        this.attendanceService = attendanceService;
        this.studentService = studentService;
        this.eventService = eventService;
        this.ledgerService = ledgerService;
    }

    @GetMapping
    public String dashboard(
            @RequestParam(required = false) String financialCourse,
            @RequestParam(required = false) String financialYearLevel,
            @RequestParam(required = false) String financialSection,
            @RequestParam(required = false) String attendanceEvent,
            @RequestParam(required = false) String attendanceCourse,
            @RequestParam(required = false) String attendanceYearLevel,
            @RequestParam(required = false) String attendanceSection,
            @RequestParam(required = false) String serviceCourse,
            @RequestParam(required = false) String serviceYearLevel,
            @RequestParam(required = false) String serviceSection,
            Model model) {

        List<Event> allEvents = eventService.getAllEvents();
        List<String> courses = studentService.getDistinctCourses();
        List<String> yearLevels = studentService.getDistinctYearLevels();
        List<String> sections = studentService.getDistinctSections();

        boolean hasFinancialFilters = (financialCourse != null && !financialCourse.isEmpty() && !financialCourse.equals("all")) ||
                                     (financialYearLevel != null && !financialYearLevel.isEmpty() && !financialYearLevel.equals("all")) ||
                                     (financialSection != null && !financialSection.isEmpty() && !financialSection.equals("all"));

        double totalFines;
        double totalPayments;
        double totalServiceCredits;

        if (hasFinancialFilters) {
            totalFines = reportService.getTotalFinesIssuedFiltered(
                financialCourse != null && !financialCourse.equals("all") ? financialCourse : null,
                financialYearLevel != null && !financialYearLevel.equals("all") ? financialYearLevel : null,
                financialSection != null && !financialSection.equals("all") ? financialSection : null
            );
            totalPayments = reportService.getTotalPaymentsFiltered(
                financialCourse != null && !financialCourse.equals("all") ? financialCourse : null,
                financialYearLevel != null && !financialYearLevel.equals("all") ? financialYearLevel : null,
                financialSection != null && !financialSection.equals("all") ? financialSection : null
            );
            totalServiceCredits = reportService.getTotalServiceCreditsFiltered(
                financialCourse != null && !financialCourse.equals("all") ? financialCourse : null,
                financialYearLevel != null && !financialYearLevel.equals("all") ? financialYearLevel : null,
                financialSection != null && !financialSection.equals("all") ? financialSection : null
            );
        } else {
            totalFines = reportService.getTotalFinesIssued();
            totalPayments = reportService.getTotalPayments();
            totalServiceCredits = reportService.getTotalServiceCredits();
        }

        double outstandingBalance = reportService.calculateOutstandingBalance(totalFines, totalPayments, totalServiceCredits);

        String attendanceEventFilter = (attendanceEvent != null && !attendanceEvent.isEmpty() && !attendanceEvent.equals("all")) ? attendanceEvent : null;
        boolean hasAttendanceFilters = (attendanceCourse != null && !attendanceCourse.isEmpty() && !attendanceCourse.equals("all")) ||
                                      (attendanceYearLevel != null && !attendanceYearLevel.isEmpty() && !attendanceYearLevel.equals("all")) ||
                                      (attendanceSection != null && !attendanceSection.isEmpty() && !attendanceSection.equals("all"));

        // Get filtered counts (if filters are applied)
        Map<String, Long> attendanceCounts;
        if (attendanceEventFilter != null || hasAttendanceFilters) {
            attendanceCounts = attendanceService.getAttendanceCountsByStatusFiltered(
                attendanceEventFilter,
                attendanceCourse != null && !attendanceCourse.equals("all") ? attendanceCourse : null,
                attendanceYearLevel != null && !attendanceYearLevel.equals("all") ? attendanceYearLevel : null,
                attendanceSection != null && !attendanceSection.equals("all") ? attendanceSection : null
            );
        } else {
            attendanceCounts = attendanceService.getAttendanceCountsByStatus(null);
        }

        // ALWAYS get overall totals across ALL events (no filters) - count TOTAL records, not unique students
        System.out.println("=== CALLING getTotalAttendanceRecordsByStatus() ===");
        Map<String, Long> overallAttendanceCounts = attendanceService.getTotalAttendanceRecordsByStatus();
        System.out.println("Raw counts from service: " + overallAttendanceCounts);
        
        long overallPresentCount = overallAttendanceCounts.getOrDefault("PRESENT", 0L);
        long overallLateCount = overallAttendanceCounts.getOrDefault("LATE", 0L);
        long overallAbsentCount = overallAttendanceCounts.getOrDefault("ABSENT", 0L);
        long overallHalfAbsentAMCount = overallAttendanceCounts.getOrDefault("HALF_ABSENT_AM", 0L);
        long overallHalfAbsentPMCount = overallAttendanceCounts.getOrDefault("HALF_ABSENT_PM", 0L);
        
        // Count half-absent as absent for overall totals
        long totalOverallAbsent = overallAbsentCount + overallHalfAbsentAMCount + overallHalfAbsentPMCount;
        long totalOverallLate = overallLateCount;
        long totalOverallPresent = overallPresentCount;
        
        // Debug logging
        System.out.println("=== DASHBOARD ATTENDANCE COUNTS (OVERALL - ALL EVENTS) ===");
        System.out.println("Raw counts - PRESENT: " + overallPresentCount + ", LATE: " + overallLateCount + ", ABSENT: " + overallAbsentCount);
        System.out.println("Raw counts - HALF_ABSENT_AM: " + overallHalfAbsentAMCount + ", HALF_ABSENT_PM: " + overallHalfAbsentPMCount);
        System.out.println("OVERALL PRESENT (all events): " + totalOverallPresent);
        System.out.println("OVERALL LATE (all events): " + totalOverallLate);
        System.out.println("OVERALL ABSENT (all events): " + totalOverallAbsent);
        System.out.println("Adding to model: totalOverallPresent=" + totalOverallPresent + ", totalOverallLate=" + totalOverallLate + ", totalOverallAbsent=" + totalOverallAbsent);
        System.out.println("===================================");

        String selectedAttendanceEventName = "All Events";
        if (attendanceEventFilter != null) {
            Event selectedEvent = eventService.getEventById(attendanceEventFilter);
            if (selectedEvent != null) {
                selectedAttendanceEventName = selectedEvent.getEventName();
            }
        }

        boolean hasServiceFilters = (serviceCourse != null && !serviceCourse.isEmpty() && !serviceCourse.equals("all")) ||
                                   (serviceYearLevel != null && !serviceYearLevel.isEmpty() && !serviceYearLevel.equals("all")) ||
                                   (serviceSection != null && !serviceSection.isEmpty() && !serviceSection.equals("all"));

        int totalServiceHours;
        double serviceCredits;
        long serviceStudentCount;

        if (hasServiceFilters) {
            totalServiceHours = reportService.getTotalServiceHoursFiltered(
                serviceCourse != null && !serviceCourse.equals("all") ? serviceCourse : null,
                serviceYearLevel != null && !serviceYearLevel.equals("all") ? serviceYearLevel : null,
                serviceSection != null && !serviceSection.equals("all") ? serviceSection : null
            );
            serviceCredits = reportService.getTotalServiceCreditsFiltered(
                serviceCourse != null && !serviceCourse.equals("all") ? serviceCourse : null,
                serviceYearLevel != null && !serviceYearLevel.equals("all") ? serviceYearLevel : null,
                serviceSection != null && !serviceSection.equals("all") ? serviceSection : null
            );
            serviceStudentCount = reportService.getServiceStudentCountFiltered(
                serviceCourse != null && !serviceCourse.equals("all") ? serviceCourse : null,
                serviceYearLevel != null && !serviceYearLevel.equals("all") ? serviceYearLevel : null,
                serviceSection != null && !serviceSection.equals("all") ? serviceSection : null
            );
        } else {
            totalServiceHours = reportService.getTotalServiceHours();
            serviceCredits = reportService.getTotalServiceCredits();
            serviceStudentCount = reportService.getServiceStudentCount();
        }

        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("activePage", "dashboard");

        model.addAttribute("totalFines", totalFines);
        model.addAttribute("totalPayments", totalPayments);
        model.addAttribute("totalServiceCredits", totalServiceCredits);
        model.addAttribute("outstandingBalance", outstandingBalance);

        // Convert to Long objects to ensure Thymeleaf can process them
        model.addAttribute("totalOverallPresent", Long.valueOf(totalOverallPresent));
        model.addAttribute("totalOverallLate", Long.valueOf(totalOverallLate));
        model.addAttribute("totalOverallAbsent", Long.valueOf(totalOverallAbsent));
        
        System.out.println("DEBUG: Model attributes set - totalOverallPresent=" + totalOverallPresent + 
                         ", totalOverallLate=" + totalOverallLate + ", totalOverallAbsent=" + totalOverallAbsent);
        model.addAttribute("selectedAttendanceEventName", selectedAttendanceEventName);
        model.addAttribute("hasAttendanceEventFilter", attendanceEventFilter != null);

        model.addAttribute("totalServiceHours", totalServiceHours);
        model.addAttribute("serviceCredits", serviceCredits);
        model.addAttribute("serviceStudentCount", serviceStudentCount);

        model.addAttribute("allEvents", allEvents);
        model.addAttribute("courses", courses);
        model.addAttribute("yearLevels", yearLevels);
        model.addAttribute("sections", sections);

        model.addAttribute("financialCourse", financialCourse != null ? financialCourse : "all");
        model.addAttribute("financialYearLevel", financialYearLevel != null ? financialYearLevel : "all");
        model.addAttribute("financialSection", financialSection != null ? financialSection : "all");

        model.addAttribute("attendanceEvent", attendanceEvent != null ? attendanceEvent : "all");
        model.addAttribute("attendanceCourse", attendanceCourse != null ? attendanceCourse : "all");
        model.addAttribute("attendanceYearLevel", attendanceYearLevel != null ? attendanceYearLevel : "all");
        model.addAttribute("attendanceSection", attendanceSection != null ? attendanceSection : "all");

        model.addAttribute("serviceCourse", serviceCourse != null ? serviceCourse : "all");
        model.addAttribute("serviceYearLevel", serviceYearLevel != null ? serviceYearLevel : "all");
        model.addAttribute("serviceSection", serviceSection != null ? serviceSection : "all");

        return "dashboard/index";
    }
}
