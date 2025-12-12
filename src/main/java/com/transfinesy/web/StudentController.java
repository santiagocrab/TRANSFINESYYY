package com.transfinesy.web;

import com.transfinesy.model.Student;
import com.transfinesy.service.StudentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public String listStudents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "All Fields") String searchType,
            @RequestParam(required = false, defaultValue = "lastName") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortOrder,
            Model model) {

        List<Student> students;

        if (search != null && !search.trim().isEmpty()) {
            // Validate name search - must contain only letters and spaces
            if ("Name".equals(searchType)) {
                String trimmedSearch = search.trim();
                if (!trimmedSearch.matches("^[a-zA-Z\\s]+$")) {
                    model.addAttribute("pageTitle", "Students");
                    model.addAttribute("activePage", "students");
                    model.addAttribute("students", studentService.getAllStudents());
                    model.addAttribute("search", search);
                    model.addAttribute("searchType", searchType);
                    model.addAttribute("sortBy", sortBy);
                    model.addAttribute("sortOrder", sortOrder);
                    model.addAttribute("errorMessage", "Please enter alphabetic characters only for name search.");
                    return "students/list";
                }
            }
            
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
        } else {
            students = studentService.getAllStudents();
        }

        students = studentService.sortStudents(students, sortBy, sortOrder);

        model.addAttribute("pageTitle", "Students");
        model.addAttribute("activePage", "students");
        model.addAttribute("students", students);
        model.addAttribute("search", search);
        model.addAttribute("searchType", searchType);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortOrder", sortOrder);

        return "students/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("pageTitle", "Add Student");
        model.addAttribute("activePage", "students");
        model.addAttribute("student", new Student());
        return "students/form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable String id, Model model) {
        Student student = studentService.getStudentById(id);
        if (student == null) {
            return "redirect:/students";
        }
        model.addAttribute("pageTitle", "Edit Student");
        model.addAttribute("activePage", "students");
        model.addAttribute("student", student);
        return "students/form";
    }

    @PostMapping("/save")
    public String saveStudent(@ModelAttribute Student student, RedirectAttributes redirectAttributes) {
        try {
            if (student.getStudID() != null && !student.getStudID().trim().isEmpty()) {
                Student existing = studentService.getStudentById(student.getStudID());
                if (existing != null) {
                    studentService.updateStudent(student);
                    redirectAttributes.addFlashAttribute("successMessage", "Student updated successfully.");
                } else {
                    studentService.addStudent(student);
                    redirectAttributes.addFlashAttribute("successMessage", "Student added successfully.");
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Student ID is required.");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid input: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot process request: " + e.getMessage());
        }
        return "redirect:/students";
    }

    @PostMapping("/delete/{id}")
    public String deleteStudent(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            studentService.deleteStudent(id);
            redirectAttributes.addFlashAttribute("successMessage", "Student deleted successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Record not found: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot process request: " + e.getMessage());
        }
        return "redirect:/students";
    }

    @GetMapping("/search")
    @ResponseBody
    public List<Map<String, String>> searchStudents(@RequestParam String query) {
        List<Student> students;
        if (query == null || query.trim().isEmpty()) {
            students = studentService.getAllStudents();
        } else {

            String searchTerm = query.trim().toLowerCase();
            students = studentService.getAllStudents().stream()
                .filter(s -> {
                    String studID = s.getStudID() != null ? s.getStudID().toLowerCase() : "";
                    String firstName = s.getFirstName() != null ? s.getFirstName().toLowerCase() : "";
                    String lastName = s.getLastName() != null ? s.getLastName().toLowerCase() : "";
                    String fullName = (firstName + " " + lastName).trim();
                    String rfidTag = s.getRfidTag() != null ? s.getRfidTag().toLowerCase() : "";

                    return studID.contains(searchTerm) ||
                           fullName.contains(searchTerm) ||
                           firstName.contains(searchTerm) ||
                           lastName.contains(searchTerm) ||
                           rfidTag.contains(searchTerm);
                })
                .limit(20)
                .collect(java.util.stream.Collectors.toList());
        }

        return students.stream()
            .map(s -> {
                Map<String, String> studentMap = new HashMap<>();
                studentMap.put("studID", s.getStudID());
                studentMap.put("name", s.getFullName());
                studentMap.put("display", s.getStudID() + " - " + s.getFullName() + (s.getRfidTag() != null && !s.getRfidTag().isEmpty() ? " (RFID: " + s.getRfidTag() + ")" : ""));
                return studentMap;
            })
            .collect(java.util.stream.Collectors.toList());
    }
}

