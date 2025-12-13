package com.transfinesy.web;

import com.transfinesy.service.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/reports")
public class TransparencyReportController {

    private final ReportService reportService;

    public TransparencyReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public String viewReport(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer semester,
            Model model) {

        if (year == null) {
            year = YearMonth.now().getYear();
        }
        if (semester == null) {
            semester = YearMonth.now().getMonthValue() <= 5 ? 1 : 2;
        }

        YearMonth currentMonth = YearMonth.now();

        double monthly = reportService.getMonthlyCollections(currentMonth);

        YearMonth semesterStart = YearMonth.of(year, semester == 1 ? 1 : 6);
        double semesterTotal = 0.0;
        YearMonth current = semesterStart;
        int monthCount = 0;
        while (current.getYear() == year && monthCount < 6) {
            semesterTotal += reportService.getMonthlyCollections(current);
            current = current.plusMonths(1);
            monthCount++;
        }

        double yearly = 0.0;
        for (int month = 1; month <= 12; month++) {
            yearly += reportService.getMonthlyCollections(YearMonth.of(year, month));
        }

        double allTime = reportService.getTotalFinesIssued();

        Map<String, Double> finesByCourse = reportService.getTotalsByCourse();
        Map<String, Double> paymentsByCourse = reportService.getPaymentsByCourse();

        int totalServiceHours = reportService.getTotalServiceHours();
        double totalServiceCredits = reportService.getTotalServiceCredits();
        int dailyServiceHours = reportService.getServiceHoursForDay(java.time.LocalDate.now());
        int monthlyServiceHours = reportService.getServiceHoursForMonth(currentMonth);
        int semesterServiceHours = reportService.getServiceHoursForSemester(currentMonth);
        int yearlyServiceHours = reportService.getServiceHoursForYear(year);
        double monthlyServiceCredits = reportService.getServiceCreditsForMonth(currentMonth);
        List<Map<String, Object>> topContributors = reportService.getTopServiceContributors(10);
        Map<String, Integer> serviceHoursByCourse = reportService.getServiceHoursByCourse();
        Map<String, Integer> serviceHoursByYear = reportService.getServiceHoursByYearLevel();
        Map<String, Integer> serviceHoursBySection = reportService.getServiceHoursBySection();

        List<Integer> availableYears = new ArrayList<>();
        int currentYear = YearMonth.now().getYear();
        for (int y = currentYear - 2; y <= currentYear + 1; y++) {
            availableYears.add(y);
        }

        model.addAttribute("pageTitle", "Transparency Report");
        model.addAttribute("activePage", "reports");
        model.addAttribute("year", year);
        model.addAttribute("semester", semester);
        model.addAttribute("availableYears", availableYears);
        model.addAttribute("monthly", monthly);
        model.addAttribute("semesterTotal", semesterTotal);
        model.addAttribute("yearly", yearly);
        model.addAttribute("allTime", allTime);
        model.addAttribute("finesByCourse", finesByCourse);
        model.addAttribute("paymentsByCourse", paymentsByCourse);

        model.addAttribute("totalServiceHours", totalServiceHours);
        model.addAttribute("totalServiceCredits", totalServiceCredits);
        model.addAttribute("dailyServiceHours", dailyServiceHours);
        model.addAttribute("monthlyServiceHours", monthlyServiceHours);
        model.addAttribute("semesterServiceHours", semesterServiceHours);
        model.addAttribute("yearlyServiceHours", yearlyServiceHours);
        model.addAttribute("monthlyServiceCredits", monthlyServiceCredits);
        model.addAttribute("topContributors", topContributors);
        model.addAttribute("serviceHoursByCourse", serviceHoursByCourse);
        model.addAttribute("serviceHoursByYear", serviceHoursByYear);
        model.addAttribute("serviceHoursBySection", serviceHoursBySection);

        return "reports/view";
    }
}

