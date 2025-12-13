package com.transfinesy.repo;

import com.transfinesy.model.Event;
import java.util.List;

public interface EventRepository {
    List<Event> findAll();
    Event findById(String eventID);
    void save(Event e);
    void update(Event e);
    void delete(String eventID);
}

