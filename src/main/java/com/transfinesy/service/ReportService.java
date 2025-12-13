package com.transfinesy.service;

import com.transfinesy.model.CommunityService;
import com.transfinesy.model.Fine;
import com.transfinesy.model.Payment;
import com.transfinesy.model.Student;
import com.transfinesy.repo.CommunityServiceRepository;
import com.transfinesy.repo.CommunityServiceRepositoryImpl;
import com.transfinesy.repo.FineRepository;
import com.transfinesy.repo.FineRepositoryImpl;
import com.transfinesy.repo.PaymentRepository;
import com.transfinesy.repo.PaymentRepositoryImpl;
import com.transfinesy.repo.StudentRepository;
import com.transfinesy.repo.StudentRepositoryImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {
    private FineRepository fineRepository;
    private PaymentRepository paymentRepository;
    private StudentRepository studentRepository;
    private CommunityServiceRepository serviceRepository;

    public ReportService() {
        this.fineRepository = new FineRepositoryImpl();
        this.paymentRepository = new PaymentRepositoryImpl();
        this.studentRepository = new StudentRepositoryImpl();
        this.serviceRepository = new CommunityServiceRepositoryImpl();
    }

    public double getMonthlyCollections(YearMonth yearMonth) {
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        return paymentRepository.getSumByDateRange(start, end);
    }

    public double getMonthlyCollectionsFiltered(YearMonth yearMonth, String yearLevel, String section) {
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        List<Payment> allPayments = paymentRepository.findAll();

        List<Student> allStudents = studentRepository.findAll();
        Map<String, String> studentYearMap = new HashMap<>();
        Map<String, String> studentSectionMap = new HashMap<>();
        for (Student student : allStudents) {
            studentYearMap.put(student.getStudID(), student.getYearLevel());
            studentSectionMap.put(student.getStudID(), student.getSection());
        }

        return allPayments.stream()
                .filter(p -> !p.getDate().isBefore(start) && !p.getDate().isAfter(end))
                .filter(p -> {
                    String studentYear = studentYearMap.get(p.getStudID());
                    String studentSection = studentSectionMap.get(p.getStudID());

                    if (yearLevel != null && !yearLevel.isEmpty() && !yearLevel.equals("all")) {
                        if (studentYear == null || !studentYear.equals(yearLevel)) {
                            return false;
                        }
                    }

                    if (section != null && !section.isEmpty() && !section.equals("all")) {
                        if (studentSection == null || !studentSection.equals(section)) {
                            return false;
                        }
                    }

                    return true;
                })
                .mapToDouble(Payment::getAmount)
                .sum();
    }

    public double getSemesterCollections(YearMonth currentMonth) {
        YearMonth semesterStart;
        int month = currentMonth.getMonthValue();
        if (month >= 1 && month <= 5) {
            semesterStart = YearMonth.of(currentMonth.getYear(), 1);
        } else {
            semesterStart = YearMonth.of(currentMonth.getYear(), 6);
        }

        double total = 0.0;
        YearMonth current = semesterStart;
        while (!current.isAfter(currentMonth)) {
            total += getMonthlyCollections(current);
            current = current.plusMonths(1);
        }
        return total;
    }

    public double getSemesterCollectionsFiltered(YearMonth currentMonth, String yearLevel, String section) {
        YearMonth semesterStart;
        int month = currentMonth.getMonthValue();
        if (month >= 1 && month <= 5) {
            semesterStart = YearMonth.of(currentMonth.getYear(), 1);
        } else {
            semesterStart = YearMonth.of(currentMonth.getYear(), 6);
        }

        double total = 0.0;
        YearMonth current = semesterStart;
        while (!current.isAfter(currentMonth)) {
            total += getMonthlyCollectionsFiltered(current, yearLevel, section);
            current = current.plusMonths(1);
        }
        return total;
    }

    public double getTotalFinesIssued() {
        return fineRepository.getTotalSum();
    }

    public double getTotalFinesIssuedFiltered(String course, String yearLevel, String section) {
        List<Fine> allFines = fineRepository.findAll();
        List<Student> allStudents = studentRepository.findAll();
        Map<String, String> studentCourseMap = new HashMap<>();
        Map<String, String> studentYearMap = new HashMap<>();
        Map<String, String> studentSectionMap = new HashMap<>();

        for (Student student : allStudents) {
            studentCourseMap.put(student.getStudID(), student.getCourse());
            studentYearMap.put(student.getStudID(), student.getYearLevel());
            studentSectionMap.put(student.getStudID(), student.getSection());
        }

        return allFines.stream()
                .filter(f -> {
                    String studentCourse = studentCourseMap.get(f.getStudID());
                    String studentYear = studentYearMap.get(f.getStudID());
                    String studentSection = studentSectionMap.get(f.getStudID());

                    if (course != null && !course.isEmpty() && !course.equals("all")) {
                        if (studentCourse == null || !studentCourse.equals(course)) {
                            return false;
                        }
                    }

                    if (yearLevel != null && !yearLevel.isEmpty() && !yearLevel.equals("all")) {
                        if (studentYear == null || !studentYear.equals(yearLevel)) {
                            return false;
                        }
                    }

                    if (section != null && !section.isEmpty() && !section.equals("all")) {
                        if (studentSection == null || !studentSection.equals(section)) {
                            return false;
                        }
                    }

                    return true;
                })
                .mapToDouble(Fine::getFineAmount)
                .sum();
    }

    public double getTotalFinesByEvent(String eventID) {
        return fineRepository.getSumByEvent(eventID);
    }

    public double getTotalFinesByEventFiltered(String eventID, String yearLevel, String section) {
        List<Fine> eventFines = fineRepository.findByEvent(eventID);
        List<Student> allStudents = studentRepository.findAll();
        Map<String, String> studentYearMap = new HashMap<>();
        Map<String, String> studentSectionMap = new HashMap<>();

        for (Student student : allStudents) {
            studentYearMap.put(student.getStudID(), student.getYearLevel());
            studentSectionMap.put(student.getStudID(), student.getSection());
        }

        return eventFines.stream()
                .filter(f -> {
                    String studentYear = studentYearMap.get(f.getStudID());
                    String studentSection = studentSectionMap.get(f.getStudID());

                    if (yearLevel != null && !yearLevel.isEmpty() && !yearLevel.equals("all")) {
                        if (studentYear == null || !studentYear.equals(yearLevel)) {
                            return false;
                        }
                    }

                    if (section != null && !section.isEmpty() && !section.equals("all")) {
                        if (studentSection == null || !studentSection.equals(section)) {
                            return false;
                        }
                    }

                    return true;
                })
                .mapToDouble(Fine::getFineAmount)
                .sum();
    }

    public double getTotalPayments() {
        return paymentRepository.getTotalSum();
    }

    public double getTotalPaymentsFiltered(String course, String yearLevel, String section) {
        List<Payment> allPayments = paymentRepository.findAll();
        List<Student> allStudents = studentRepository.findAll();
        Map<String, String> studentCourseMap = new HashMap<>();
        Map<String, String> studentYearMap = new HashMap<>();
        Map<String, String> studentSectionMap = new HashMap<>();

        for (Student student : allStudents) {
            studentCourseMap.put(student.getStudID(), student.getCourse());
            studentYearMap.put(student.getStudID(), student.getYearLevel());
            studentSectionMap.put(student.getStudID(), student.getSection());
        }

        return allPayments.stream()
                .filter(p -> {
                    String studentCourse = studentCourseMap.get(p.getStudID());
                    String studentYear = studentYearMap.get(p.getStudID());
                    String studentSection = studentSectionMap.get(p.getStudID());

                    if (course != null && !course.isEmpty() && !course.equals("all")) {
                        if (studentCourse == null || !studentCourse.equals(course)) {
                            return false;
                        }
                    }

                    if (yearLevel != null && !yearLevel.isEmpty() && !yearLevel.equals("all")) {
                        if (studentYear == null || !studentYear.equals(yearLevel)) {
                            return false;
                        }
                    }

                    if (section != null && !section.isEmpty() && !section.equals("all")) {
                        if (studentSection == null || !studentSection.equals(section)) {
                            return false;
                        }
                    }

                    return true;
                })
                .mapToDouble(Payment::getAmount)
                .sum();
    }

    public double getTotalServiceCreditsFiltered(String course, String yearLevel, String section) {
        List<CommunityService> allServices = serviceRepository.findAll();
        List<Student> allStudents = studentRepository.findAll();
        Map<String, String> studentCourseMap = new HashMap<>();
        Map<String, String> studentYearMap = new HashMap<>();
        Map<String, String> studentSectionMap = new HashMap<>();

        for (Student student : allStudents) {
            studentCourseMap.put(student.getStudID(), student.getCourse());
            studentYearMap.put(student.getStudID(), student.getYearLevel());
            studentSectionMap.put(student.getStudID(), student.getSection());
        }

        return allServices.stream()
                .filter(s -> {
                    String studentCourse = studentCourseMap.get(s.getStudID());
                    String studentYear = studentYearMap.get(s.getStudID());
                    String studentSection = studentSectionMap.get(s.getStudID());

                    if (course != null && !course.isEmpty() && !course.equals("all")) {
                        if (studentCourse == null || !studentCourse.equals(course)) {
                            return false;
                        }
                    }

                    if (yearLevel != null && !yearLevel.isEmpty() && !yearLevel.equals("all")) {
                        if (studentYear == null || !studentYear.equals(yearLevel)) {
                            return false;
                        }
                    }

                    if (section != null && !section.isEmpty() && !section.equals("all")) {
                        if (studentSection == null || !studentSection.equals(section)) {
                            return false;
                        }
                    }

                    return true;
                })
                .mapToDouble(CommunityService::getCreditAmount)
                .sum();
    }

    public int getTotalServiceHoursFiltered(String course, String yearLevel, String section) {
        List<CommunityService> allServices = serviceRepository.findAll();
        List<Student> allStudents = studentRepository.findAll();
        Map<String, String> studentCourseMap = new HashMap<>();
        Map<String, String> studentYearMap = new HashMap<>();
        Map<String, String> studentSectionMap = new HashMap<>();

        for (Student student : allStudents) {
            studentCourseMap.put(student.getStudID(), student.getCourse());
            studentYearMap.put(student.getStudID(), student.getYearLevel());
            studentSectionMap.put(student.getStudID(), student.getSection());
        }

        return allServices.stream()
                .filter(s -> {
                    String studentCourse = studentCourseMap.get(s.getStudID());
                    String studentYear = studentYearMap.get(s.getStudID());
                    String studentSection = studentSectionMap.get(s.getStudID());

                    if (course != null && !course.isEmpty() && !course.equals("all")) {
                        if (studentCourse == null || !studentCourse.equals(course)) {
                            return false;
                        }
                    }

                    if (yearLevel != null && !yearLevel.isEmpty() && !yearLevel.equals("all")) {
                        if (studentYear == null || !studentYear.equals(yearLevel)) {
                            return false;
                        }
                    }

                    if (section != null && !section.isEmpty() && !section.equals("all")) {
                        if (studentSection == null || !studentSection.equals(section)) {
                            return false;
                        }
                    }

                    return true;
                })
                .mapToInt(CommunityService::getHoursRendered)
                .sum();
    }

    public long getServiceStudentCount() {
        List<CommunityService> allServices = serviceRepository.findAll();
        return allServices.stream()
                .map(CommunityService::getStudID)
                .distinct()
                .count();
    }

    public long getServiceStudentCountFiltered(String course, String yearLevel, String section) {
        List<CommunityService> allServices = serviceRepository.findAll();
        List<Student> allStudents = studentRepository.findAll();
        Map<String, String> studentCourseMap = new HashMap<>();
        Map<String, String> studentYearMap = new HashMap<>();
        Map<String, String> studentSectionMap = new HashMap<>();

        for (Student student : allStudents) {
            studentCourseMap.put(student.getStudID(), student.getCourse());
            studentYearMap.put(student.getStudID(), student.getYearLevel());
            studentSectionMap.put(student.getStudID(), student.getSection());
        }

        return allServices.stream()
                .filter(s -> {
                    String studentCourse = studentCourseMap.get(s.getStudID());
                    String studentYear = studentYearMap.get(s.getStudID());
                    String studentSection = studentSectionMap.get(s.getStudID());

                    if (course != null && !course.isEmpty() && !course.equals("all")) {
                        if (studentCourse == null || !studentCourse.equals(course)) {
                            return false;
                        }
                    }

                    if (yearLevel != null && !yearLevel.isEmpty() && !yearLevel.equals("all")) {
                        if (studentYear == null || !studentYear.equals(yearLevel)) {
                            return false;
                        }
                    }

                    if (section != null && !section.isEmpty() && !section.equals("all")) {
                        if (studentSection == null || !studentSection.equals(section)) {
                            return false;
                        }
                    }

                    return true;
                })
                .map(CommunityService::getStudID)
                .distinct()
                .count();
    }

    public double calculateOutstandingBalance(double totalFines, double totalPayments, double totalServiceCredits) {
        double balance = totalFines - totalPayments - totalServiceCredits;
        return Math.max(0.0, balance);
    }

    public Map<String, Double> getTotalsByCourseFiltered(String yearLevel, String section) {
        Map<String, Double> totals = new HashMap<>();

        List<Fine> allFines = fineRepository.findAll();
        Map<String, String> studentCourseMap = new HashMap<>();
        Map<String, String> studentYearMap = new HashMap<>();
        Map<String, String> studentSectionMap = new HashMap<>();

        List<Student> allStudents = studentRepository.findAll();
        for (Student student : allStudents) {
            studentCourseMap.put(student.getStudID(), student.getCourse());
            studentYearMap.put(student.getStudID(), student.getYearLevel());
            studentSectionMap.put(student.getStudID(), student.getSection());
        }

        for (Fine fine : allFines) {
            String studentYear = studentYearMap.get(fine.getStudID());
            String studentSection = studentSectionMap.get(fine.getStudID());
            String course = studentCourseMap.get(fine.getStudID());

            if (yearLevel != null && !yearLevel.isEmpty() && !yearLevel.equals("all")) {
                if (studentYear == null || !studentYear.equals(yearLevel)) {
                    continue;
                }
            }

            if (section != null && !section.isEmpty() && !section.equals("all")) {
                if (studentSection == null || !studentSection.equals(section)) {
                    continue;
                }
            }

            if (course != null && !course.isEmpty() && fine.getFineAmount() > 0) {
                totals.put(course, totals.getOrDefault(course, 0.0) + fine.getFineAmount());
            }
        }

        return totals;
    }

    public Map<String, Double> getPaymentsByCourseFiltered(String yearLevel, String section) {
        Map<String, Double> totals = new HashMap<>();

        List<Payment> allPayments = paymentRepository.findAll();
        Map<String, String> studentCourseMap = new HashMap<>();
        Map<String, String> studentYearMap = new HashMap<>();
        Map<String, String> studentSectionMap = new HashMap<>();

        List<Student> allStudents = studentRepository.findAll();
        for (Student student : allStudents) {
            studentCourseMap.put(student.getStudID(), student.getCourse());
            studentYearMap.put(student.getStudID(), student.getYearLevel());
            studentSectionMap.put(student.getStudID(), student.getSection());
        }

        for (Payment payment : allPayments) {
            String studentYear = studentYearMap.get(payment.getStudID());
            String studentSection = studentSectionMap.get(payment.getStudID());
            String course = studentCourseMap.get(payment.getStudID());

            if (yearLevel != null && !yearLevel.isEmpty() && !yearLevel.equals("all")) {
                if (studentYear == null || !studentYear.equals(yearLevel)) {
                    continue;
                }
            }

            if (section != null && !section.isEmpty() && !section.equals("all")) {
                if (studentSection == null || !studentSection.equals(section)) {
                    continue;
                }
            }

            if (course != null && !course.isEmpty()) {
                totals.put(course, totals.getOrDefault(course, 0.0) + payment.getAmount());
            }
        }

        return totals;
    }

    public double getTotalServiceCredits() {
        return serviceRepository.getTotalCredits();
    }

    public int getTotalServiceHours() {
        return serviceRepository.getTotalHours();
    }

    public int getServiceHoursForDay(LocalDate date) {
        List<CommunityService> allServices = serviceRepository.findAll();
        return allServices.stream()
                .filter(s -> s.getDate().equals(date))
                .mapToInt(CommunityService::getHoursRendered)
                .sum();
    }

    public int getServiceHoursForMonth(YearMonth yearMonth) {
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        return serviceRepository.getHoursByDateRange(start, end);
    }

    public int getServiceHoursForSemester(YearMonth currentMonth) {
        YearMonth semesterStart;
        int month = currentMonth.getMonthValue();
        if (month >= 1 && month <= 5) {
            semesterStart = YearMonth.of(currentMonth.getYear(), 1);
        } else {
            semesterStart = YearMonth.of(currentMonth.getYear(), 6);
        }

        int total = 0;
        YearMonth current = semesterStart;
        while (!current.isAfter(currentMonth)) {
            total += getServiceHoursForMonth(current);
            current = current.plusMonths(1);
        }
        return total;
    }

    public int getServiceHoursForYear(int year) {
        List<CommunityService> allServices = serviceRepository.findAll();
        return allServices.stream()
                .filter(s -> s.getDate().getYear() == year)
                .mapToInt(CommunityService::getHoursRendered)
                .sum();
    }

    public double getServiceCreditsForMonth(YearMonth yearMonth) {
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        return serviceRepository.getCreditsByDateRange(start, end);
    }

    public List<Map<String, Object>> getTopServiceContributors(int limit) {
        List<CommunityService> allServices = serviceRepository.findAll();
        List<Student> allStudents = studentRepository.findAll();
        Map<String, Student> studentMap = allStudents.stream()
                .collect(Collectors.toMap(Student::getStudID, s -> s));

        Map<String, Integer> hoursByStudent = new HashMap<>();
        Map<String, Double> creditsByStudent = new HashMap<>();

        for (CommunityService service : allServices) {
            hoursByStudent.put(service.getStudID(),
                    hoursByStudent.getOrDefault(service.getStudID(), 0) + service.getHoursRendered());
            creditsByStudent.put(service.getStudID(),
                    creditsByStudent.getOrDefault(service.getStudID(), 0.0) + service.getCreditAmount());
        }

        return creditsByStudent.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    Map<String, Object> result = new HashMap<>();
                    Student student = studentMap.get(entry.getKey());
                    result.put("studID", entry.getKey());
                    result.put("studentName", student != null ? student.getFullName() : entry.getKey());
                    result.put("totalHours", hoursByStudent.getOrDefault(entry.getKey(), 0));
                    result.put("totalCredits", entry.getValue());
                    return result;
                })
                .collect(Collectors.toList());
    }

    public Map<String, Integer> getServiceHoursByCourse() {
        List<CommunityService> allServices = serviceRepository.findAll();
        List<Student> allStudents = studentRepository.findAll();
        Map<String, String> studentCourseMap = allStudents.stream()
                .collect(Collectors.toMap(Student::getStudID, Student::getCourse));

        Map<String, Integer> hoursByCourse = new HashMap<>();
        for (CommunityService service : allServices) {
            String course = studentCourseMap.get(service.getStudID());
            if (course != null && !course.isEmpty()) {
                hoursByCourse.put(course, hoursByCourse.getOrDefault(course, 0) + service.getHoursRendered());
            }
        }
        return hoursByCourse;
    }

    public Map<String, Integer> getServiceHoursByYearLevel() {
        List<CommunityService> allServices = serviceRepository.findAll();
        List<Student> allStudents = studentRepository.findAll();
        Map<String, String> studentYearMap = allStudents.stream()
                .collect(Collectors.toMap(Student::getStudID, Student::getYearLevel));

        Map<String, Integer> hoursByYear = new HashMap<>();
        for (CommunityService service : allServices) {
            String yearLevel = studentYearMap.get(service.getStudID());
            if (yearLevel != null && !yearLevel.isEmpty()) {
                hoursByYear.put(yearLevel, hoursByYear.getOrDefault(yearLevel, 0) + service.getHoursRendered());
            }
        }
        return hoursByYear;
    }

    public Map<String, Integer> getServiceHoursBySection() {
        List<CommunityService> allServices = serviceRepository.findAll();
        List<Student> allStudents = studentRepository.findAll();
        Map<String, String> studentSectionMap = allStudents.stream()
                .collect(Collectors.toMap(Student::getStudID, Student::getSection));

        Map<String, Integer> hoursBySection = new HashMap<>();
        for (CommunityService service : allServices) {
            String section = studentSectionMap.get(service.getStudID());
            if (section != null && !section.isEmpty()) {
                hoursBySection.put(section, hoursBySection.getOrDefault(section, 0) + service.getHoursRendered());
            }
        }
        return hoursBySection;
    }

    public double getTotalFinesByCourse(String course) {
        List<Student> students = studentRepository.findByCourse(course);
        if (students.isEmpty()) return 0.0;

        List<Fine> allFines = fineRepository.findAll();
        Set<String> studentIds = students.stream()
            .map(Student::getStudID)
            .collect(Collectors.toSet());

        return allFines.stream()
            .filter(f -> studentIds.contains(f.getStudID()))
            .mapToDouble(Fine::getFineAmount)
            .sum();
    }

    public Map<String, Double> getTotalsByCourse() {
        Map<String, Double> totals = new HashMap<>();

        List<Fine> allFines = fineRepository.findAll();
        Map<String, String> studentCourseMap = new HashMap<>();

        List<Student> allStudents = studentRepository.findAll();
        for (Student student : allStudents) {
            studentCourseMap.put(student.getStudID(), student.getCourse());
        }

        for (Fine fine : allFines) {
            String course = studentCourseMap.get(fine.getStudID());
            if (course != null && !course.isEmpty()) {
                totals.put(course, totals.getOrDefault(course, 0.0) + fine.getFineAmount());
            }
        }

        return totals;
    }

    public Map<String, Double> getPaymentsByCourse() {
        Map<String, Double> totals = new HashMap<>();

        List<Payment> allPayments = paymentRepository.findAll();
        Map<String, String> studentCourseMap = new HashMap<>();

        List<Student> allStudents = studentRepository.findAll();
        for (Student student : allStudents) {
            studentCourseMap.put(student.getStudID(), student.getCourse());
        }

        for (Payment payment : allPayments) {
            String course = studentCourseMap.get(payment.getStudID());
            if (course != null && !course.isEmpty()) {
                totals.put(course, totals.getOrDefault(course, 0.0) + payment.getAmount());
            }
        }

        return totals;
    }
}

