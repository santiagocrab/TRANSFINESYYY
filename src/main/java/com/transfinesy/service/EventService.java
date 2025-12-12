package com.transfinesy.service;

import com.transfinesy.model.Event;
import com.transfinesy.repo.EventRepository;
import com.transfinesy.repo.EventRepositoryImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class EventService {
    private EventRepository repository;

    public EventService() {
        this.repository = new EventRepositoryImpl();
    }

    public List<Event> getAllEvents() {
        return repository.findAll();
    }

    public Event getEventById(String eventID) {
        return repository.findById(eventID);
    }

    public void addEvent(Event event) {
        validateEvent(event);
        repository.save(event);
    }

    public void updateEvent(Event event) {
        if (event == null || event.getEventID() == null) {
            throw new IllegalArgumentException("Event ID is required");
        }
        validateEvent(event);
        repository.update(event);
    }

    private void validateEvent(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }

        if (event.getEventID() == null || event.getEventID().trim().isEmpty()) {
            throw new IllegalArgumentException("Event ID is required");
        }

        if (event.getEventDate() != null) {
            LocalDate today = LocalDate.now();
            LocalDate eventDate = event.getEventDate();

            if (eventDate.isBefore(today)) {
                throw new IllegalArgumentException("Event date cannot be in the past. Please select today or a future date.");
            }

            int eventYear = eventDate.getYear();
            if (eventYear < 2000) {
                throw new IllegalArgumentException("Event date cannot be before year 2000");
            }
        }

        if (event.getSemester() != null && event.getSemester() != 1 && event.getSemester() != 2) {
            throw new IllegalArgumentException("Semester must be 1 or 2");
        }
    }

    public void deleteEvent(String eventID) {
        repository.delete(eventID);
    }
}

