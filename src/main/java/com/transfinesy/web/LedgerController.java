package com.transfinesy.web;

import com.transfinesy.model.Fine;
import com.transfinesy.model.Ledger;
import com.transfinesy.model.Student;
import com.transfinesy.model.Event;
import com.transfinesy.service.ClearanceService;
import com.transfinesy.service.EventService;
import com.transfinesy.service.FineService;
import com.transfinesy.service.LedgerService;
import com.transfinesy.service.StudentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/ledger")
public class LedgerController {

    private final LedgerService ledgerService;
    private final ClearanceService clearanceService;
    private final StudentService studentService;
    private final FineService fineService;
    private final EventService eventService;

    public LedgerController(LedgerService ledgerService, ClearanceService clearanceService, StudentService studentService, FineService fineService, EventService eventService) {
        this.ledgerService = ledgerService;
        this.clearanceService = clearanceService;
        this.studentService = studentService;
        this.fineService = fineService;
        this.eventService = eventService;
    }

    @GetMapping
    public String viewLedger(
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "All Fields") String searchType,
            Model model) {

        List<Student> students = studentService.getAllStudents();

        if (search != null && !search.trim().isEmpty()) {
            switch (searchType) {
                case "Student ID":

                    students = studentService.searchByStudentID(search);
                    break;
                case "Name":

                    students = studentService.searchByNameOnly(search);
                    break;
                case "Course":

                    students = studentService.searchByCourse(search);
                    break;
                case "Year Level":

                    students = studentService.searchByYearLevel(search);
                    break;
                case "Section":

                    students = studentService.searchBySection(search);
                    break;
                case "RFID Tag":

                    students = studentService.searchByRFID(search);
                    break;
                case "All Fields":
                default:

                    students = studentService.searchStudents(search);
                    break;
            }
        }

        Ledger ledger = null;
        String clearanceStatus = "-";
        Student selectedStudent = null;
        List<Fine> studentFines = new java.util.ArrayList<>();
        Map<String, Event> eventMap = new HashMap<>();

        if (studentId != null && !studentId.trim().isEmpty()) {
            selectedStudent = studentService.getStudentById(studentId);
            if (selectedStudent == null) {
                model.addAttribute("errorMessage", "No matching student found.");
            } else {
                ledger = ledgerService.getLedgerForStudent(studentId);
                clearanceStatus = clearanceService.getClearanceStatusWithBalance(selectedStudent);
                
                // Get all fines for this student
                studentFines = fineService.getFinesByStudent(studentId);
                if (studentFines == null) {
                    studentFines = new java.util.ArrayList<>();
                }
                
                // Get event information for each fine in studentFines
                for (Fine fine : studentFines) {
                    if (fine != null && fine.getEventID() != null && !eventMap.containsKey(fine.getEventID())) {
                        Event event = eventService.getEventById(fine.getEventID());
                        if (event != null) {
                            eventMap.put(fine.getEventID(), event);
                        }
                    }
                }
                
                // Also populate eventMap for all fines in transaction history
                if (ledger != null && ledger.getTransactionHistory() != null) {
                    for (com.transfinesy.model.Transaction txn : ledger.getTransactionHistory()) {
                        if (txn instanceof Fine) {
                            Fine fine = (Fine) txn;
                            if (fine.getEventID() != null && !eventMap.containsKey(fine.getEventID())) {
                                Event event = eventService.getEventById(fine.getEventID());
                                if (event != null) {
                                    eventMap.put(fine.getEventID(), event);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (search != null && !search.trim().isEmpty() && students.isEmpty()) {
            model.addAttribute("errorMessage", "No matching student found.");
        }

        model.addAttribute("pageTitle", "Ledger & Transactions");
        model.addAttribute("activePage", "ledger");
        model.addAttribute("students", students);
        model.addAttribute("selectedStudent", selectedStudent);
        model.addAttribute("ledger", ledger);
        model.addAttribute("clearanceStatus", clearanceStatus);
        model.addAttribute("studentFines", studentFines);
        model.addAttribute("eventMap", eventMap);
        model.addAttribute("search", search);
        model.addAttribute("searchType", searchType);

        return "ledger/view";
    }
}

