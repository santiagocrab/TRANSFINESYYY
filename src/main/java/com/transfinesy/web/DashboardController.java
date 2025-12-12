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

        long presentCount = attendanceCounts.getOrDefault("PRESENT", 0L);
        long lateCount = attendanceCounts.getOrDefault("LATE", 0L);
        long absentCount = attendanceCounts.getOrDefault("ABSENT", 0L);
        long halfAbsentAMCount = attendanceCounts.getOrDefault("HALF_ABSENT_AM", 0L);
        long halfAbsentPMCount = attendanceCounts.getOrDefault("HALF_ABSENT_PM", 0L);
        long totalHalfAbsent = halfAbsentAMCount + halfAbsentPMCount;

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

        model.addAttribute("presentCount", presentCount);
        model.addAttribute("lateCount", lateCount);
        model.addAttribute("absentCount", absentCount);
        model.addAttribute("halfAbsentAMCount", halfAbsentAMCount);
        model.addAttribute("halfAbsentPMCount", halfAbsentPMCount);
        model.addAttribute("totalHalfAbsent", totalHalfAbsent);
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
