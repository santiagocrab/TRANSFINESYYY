package com.transfinesy.web;

import com.transfinesy.model.AttendanceSession;
import com.transfinesy.model.Event;
import com.transfinesy.service.EventService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public String listEvents(Model model) {
        List<Event> events = eventService.getAllEvents();
        model.addAttribute("pageTitle", "Events & Attendance");
        model.addAttribute("activePage", "events");
        model.addAttribute("events", events);
        return "events/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        Event event = new Event();
        event.setEventID("EVT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        event.setEventDate(LocalDate.now());
        event.setSemester(1);
        event.setSchoolYear("2025-2026");

        model.addAttribute("pageTitle", "Add Event");
        model.addAttribute("activePage", "events");
        model.addAttribute("event", event);
        model.addAttribute("minDate", LocalDate.now());
        return "events/form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        Event event = eventService.getEventById(id);
        if (event == null) {
            return "redirect:/events";
        }

        // Prevent editing finalized events
        if (event.isFinalized()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot edit a finalized event. Attendance has been locked and fines have been computed.");
            return "redirect:/events";
        }

        if (event.getEventDate() != null && event.getEventDate().isBefore(LocalDate.now())) {
            event.setEventDate(LocalDate.now());
        }

        model.addAttribute("pageTitle", "Edit Event");
        model.addAttribute("activePage", "events");
        model.addAttribute("event", event);
        model.addAttribute("minDate", LocalDate.now());
        return "events/form";
    }

    @PostMapping("/save")
    public String saveEvent(@ModelAttribute Event event,
                             @RequestParam(required = false) String sessionType,
                             RedirectAttributes redirectAttributes) {
        try {

            if (sessionType != null && !sessionType.trim().isEmpty()) {
                try {
                    event.setSessionType(AttendanceSession.valueOf(sessionType));
                } catch (IllegalArgumentException e) {

                    event.setSessionType(null);
                }
            }

            if (event.getEventID() != null && !event.getEventID().trim().isEmpty()) {
                Event existing = eventService.getEventById(event.getEventID());
                if (existing != null) {
                    // Prevent updating finalized events
                    if (existing.isFinalized()) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Cannot update a finalized event. Attendance has been locked and fines have been computed.");
                        return "redirect:/events";
                    }

                    if (existing.getAmTimeIn() != null && event.getAmTimeIn() == null) {
                        event.setAmTimeIn(existing.getAmTimeIn());
                    }
                    if (existing.getAmTimeOut() != null && event.getAmTimeOut() == null) {
                        event.setAmTimeOut(existing.getAmTimeOut());
                    }
                    if (existing.getPmTimeIn() != null && event.getPmTimeIn() == null) {
                        event.setPmTimeIn(existing.getPmTimeIn());
                    }
                    if (existing.getPmTimeOut() != null && event.getPmTimeOut() == null) {
                        event.setPmTimeOut(existing.getPmTimeOut());
                    }
                    // Preserve finalized status
                    event.setFinalized(existing.getFinalized());
                    eventService.updateEvent(event);
                    redirectAttributes.addFlashAttribute("successMessage", "Event updated successfully.");
                } else {
                    eventService.addEvent(event);
                    redirectAttributes.addFlashAttribute("successMessage", "Event added successfully.");
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Event ID is required.");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid input: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot process request: " + e.getMessage());
        }
        return "redirect:/events";
    }

    @PostMapping("/delete/{id}")
    public String deleteEvent(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            Event event = eventService.getEventById(id);
            if (event != null && event.isFinalized()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete a finalized event. Attendance records and fines are locked.");
                return "redirect:/events";
            }
            eventService.deleteEvent(id);
            redirectAttributes.addFlashAttribute("successMessage", "Event deleted successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Record not found: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot process request: " + e.getMessage());
        }
        return "redirect:/events";
    }
}

