package com.transfinesy.web;

import com.transfinesy.model.Fine;
import com.transfinesy.model.Student;
import com.transfinesy.model.Event;
import com.transfinesy.service.ClearanceService;
import com.transfinesy.service.EventService;
import com.transfinesy.service.FineService;
import com.transfinesy.service.LedgerService;
import com.transfinesy.service.StudentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/clearance")
public class ClearanceController {

    private final StudentService studentService;
    private final LedgerService ledgerService;
    private final ClearanceService clearanceService;
    private final FineService fineService;
    private final EventService eventService;

    public ClearanceController(StudentService studentService, LedgerService ledgerService, ClearanceService clearanceService, FineService fineService, EventService eventService) {
        this.studentService = studentService;
        this.ledgerService = ledgerService;
        this.clearanceService = clearanceService;
        this.fineService = fineService;
        this.eventService = eventService;
    }

    @GetMapping
    public String viewClearance(
            @RequestParam(required = false, defaultValue = "All") String filter,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "All Fields") String searchType,
            Model model) {

        List<Student> allStudents = studentService.getAllStudents();

        if (search != null && !search.trim().isEmpty()) {
            switch (searchType) {
                case "Student ID":
                    allStudents = studentService.searchByStudentID(search);
                    break;
                case "Name":
                    allStudents = studentService.searchStudents(search);
                    break;
                case "RFID Tag":
                    allStudents = studentService.searchByRFID(search);
                    break;
                case "Course":
                    allStudents = studentService.searchByCourse(search);
                    break;
                case "Year Level":
                    allStudents = studentService.searchByYearLevel(search);
                    break;
                case "Section":
                    allStudents = studentService.searchBySection(search);
                    break;
                default:

                    String searchTerm = search.trim().toLowerCase();
                    allStudents = allStudents.stream()
                        .filter(s -> {
                            String studID = s.getStudID() != null ? s.getStudID().toLowerCase() : "";
                            String firstName = s.getFirstName() != null ? s.getFirstName().toLowerCase() : "";
                            String lastName = s.getLastName() != null ? s.getLastName().toLowerCase() : "";
                            String fullName = (firstName + " " + lastName).trim();
                            String rfidTag = s.getRfidTag() != null ? s.getRfidTag().toLowerCase() : "";
                            String course = s.getCourse() != null ? s.getCourse().toLowerCase() : "";
                            String yearLevel = s.getYearLevel() != null ? s.getYearLevel().toLowerCase() : "";
                            String section = s.getSection() != null ? s.getSection().toLowerCase() : "";

                            return studID.contains(searchTerm) ||
                                   fullName.contains(searchTerm) ||
                                   firstName.contains(searchTerm) ||
                                   lastName.contains(searchTerm) ||
                                   rfidTag.contains(searchTerm) ||
                                   course.contains(searchTerm) ||
                                   yearLevel.contains(searchTerm) ||
                                   section.contains(searchTerm);
                        })
                        .collect(java.util.stream.Collectors.toList());
                    break;
            }
        }

        Map<String, Double> balances = new HashMap<>();
        Map<String, String> clearanceStatuses = new HashMap<>();
        Map<String, List<Fine>> studentFinesMap = new HashMap<>();
        Map<String, Event> eventMap = new HashMap<>();
        Map<String, Double> totalFinesMap = new HashMap<>();
        List<Student> students = new java.util.ArrayList<>();
        java.util.Set<String> eventIDs = new java.util.HashSet<>();

        // First pass: collect all event IDs from fines
        for (Student student : allStudents) {
            try {
                List<Fine> fines = fineService.getFinesByStudent(student.getStudID());
                for (Fine fine : fines) {
                    if (fine.getEventID() != null) {
                        eventIDs.add(fine.getEventID());
                    }
                }
            } catch (Exception e) {
                // Continue processing
            }
        }

        // Get event information only for events that have fines
        for (String eventID : eventIDs) {
            Event event = eventService.getEventById(eventID);
            if (event != null) {
                eventMap.put(eventID, event);
            }
        }

        for (Student student : allStudents) {
            try {
                double balance = ledgerService.getBalanceForStudent(student.getStudID());
                String status = clearanceService.getClearanceStatus(student);
                balances.put(student.getStudID(), balance);
                clearanceStatuses.put(student.getStudID(), status);
                
                // Get all fines for this student
                List<Fine> fines = fineService.getFinesByStudent(student.getStudID());
                studentFinesMap.put(student.getStudID(), fines);
                
                // Calculate total fines
                double totalFines = fines.stream()
                    .mapToDouble(Fine::getFineAmount)
                    .sum();
                totalFinesMap.put(student.getStudID(), totalFines);

                if (filter == null || filter.equals("All")) {
                    students.add(student);
                } else if (filter.equals("CLEARED") && status.equals("CLEARED")) {
                    students.add(student);
                } else if (filter.equals("WITH BALANCE") && status.equals("WITH BALANCE")) {
                    students.add(student);
                }
            } catch (Exception e) {

                balances.put(student.getStudID(), 0.0);
                clearanceStatuses.put(student.getStudID(), "WITH BALANCE");
                studentFinesMap.put(student.getStudID(), new java.util.ArrayList<>());
                totalFinesMap.put(student.getStudID(), 0.0);
                if (filter == null || filter.equals("All") || filter.equals("WITH BALANCE")) {
                    students.add(student);
                }
            }
        }

        model.addAttribute("pageTitle", "Clearance");
        model.addAttribute("activePage", "clearance");
        model.addAttribute("students", students);
        model.addAttribute("filter", filter);
        model.addAttribute("balances", balances);
        model.addAttribute("clearanceStatuses", clearanceStatuses);
        model.addAttribute("studentFinesMap", studentFinesMap);
        model.addAttribute("eventMap", eventMap);
        model.addAttribute("totalFinesMap", totalFinesMap);
        model.addAttribute("search", search);
        model.addAttribute("searchType", searchType);

        return "clearance/view";
    }
}

