package com.transfinesy.web;

import com.transfinesy.model.Payment;
import com.transfinesy.model.Student;
import com.transfinesy.service.LedgerService;
import com.transfinesy.service.PaymentService;
import com.transfinesy.service.StudentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final StudentService studentService;
    private final LedgerService ledgerService;

    public PaymentController(PaymentService paymentService, StudentService studentService, LedgerService ledgerService) {
        this.paymentService = paymentService;
        this.studentService = studentService;
        this.ledgerService = ledgerService;
    }

    @GetMapping
    public String listPayments(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "All Fields") String searchType,
            Model model) {

        List<Payment> payments = paymentService.getAllPayments();

        if (search != null && !search.trim().isEmpty()) {
            List<Payment> filteredPayments = payments.stream()
                .filter(payment -> {
                    Student student = studentService.getStudentById(payment.getStudID());
                    String studentName = student != null ? student.getFullName() : "";

                    switch (searchType) {
                        case "All Fields":
                            return payment.getPaymentID().toLowerCase().contains(search.toLowerCase()) ||
                                   payment.getStudID().toLowerCase().contains(search.toLowerCase()) ||
                                   studentName.toLowerCase().contains(search.toLowerCase()) ||
                                   payment.getOrNumber().toLowerCase().contains(search.toLowerCase());
                        case "Student ID":
                            return payment.getStudID().toLowerCase().contains(search.toLowerCase());
                        case "Student Name":
                            return studentName.toLowerCase().contains(search.toLowerCase());
                        case "OR Number":
                            return payment.getOrNumber().toLowerCase().contains(search.toLowerCase());
                        case "Payment ID":
                            return payment.getPaymentID().toLowerCase().contains(search.toLowerCase());
                        default:
                            return true;
                    }
                })
                .toList();
            payments = filteredPayments;
        }

        List<Student> students = studentService.getAllStudents();

        Map<String, Student> studentMap = new HashMap<>();
        Map<String, Double> balancesMap = new HashMap<>();
        
        for (Student student : students) {
            studentMap.put(student.getStudID(), student);
            try {
                double balance = ledgerService.getBalanceForStudent(student.getStudID());
                balancesMap.put(student.getStudID(), balance);
            } catch (Exception e) {
                balancesMap.put(student.getStudID(), 0.0);
            }
        }

        model.addAttribute("pageTitle", "Payments");
        model.addAttribute("activePage", "payments");
        model.addAttribute("payments", payments);
        model.addAttribute("students", students);
        model.addAttribute("studentMap", studentMap);
        model.addAttribute("balancesMap", balancesMap);
        model.addAttribute("search", search);
        model.addAttribute("searchType", searchType);
        model.addAttribute("today", LocalDate.now());

        return "payments/list";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable String id, Model model) {
        Payment payment = paymentService.getPaymentById(id);
        if (payment == null) {
            return "redirect:/payments";
        }
        List<Student> students = studentService.getAllStudents();
        model.addAttribute("pageTitle", "Edit Payment");
        model.addAttribute("activePage", "payments");
        model.addAttribute("payment", payment);
        model.addAttribute("students", students);
        return "payments/form";
    }

    @PostMapping("/save")
    public String savePayment(
            @RequestParam String studentId,
            @RequestParam double amount,
            @RequestParam String orNumber,
            @RequestParam String date,
            RedirectAttributes redirectAttributes) {
        try {

            LocalDate paymentDate = LocalDate.parse(date);
            paymentService.recordPayment(studentId, amount, orNumber, paymentDate);
            redirectAttributes.addFlashAttribute("successMessage", "Payment recorded successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid input: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error recording payment: " + e.getMessage());
        }
        return "redirect:/payments";
    }

    @PostMapping("/update/{id}")
    public String updatePayment(
            @PathVariable String id,
            @RequestParam String studentId,
            @RequestParam double amount,
            @RequestParam String orNumber,
            @RequestParam String date,
            RedirectAttributes redirectAttributes) {
        try {
            LocalDate paymentDate = LocalDate.parse(date);
            paymentService.updatePayment(id, studentId, amount, orNumber, paymentDate);
            redirectAttributes.addFlashAttribute("successMessage", "Payment updated successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid input: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating payment: " + e.getMessage());
        }
        return "redirect:/payments";
    }

    @PostMapping("/delete/{id}")
    public String deletePayment(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            paymentService.deletePayment(id);
            redirectAttributes.addFlashAttribute("successMessage", "Payment deleted successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Record not found: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting payment: " + e.getMessage());
        }
        return "redirect:/payments";
    }
}

