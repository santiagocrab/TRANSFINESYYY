package com.transfinesy.repo;

import com.transfinesy.model.Fine;
import java.util.List;

public interface FineRepository {
    List<Fine> findAll();
    Fine findById(String fineID);
    List<Fine> findByStudent(String studID);
    List<Fine> findByEvent(String eventID);
    void save(Fine f);
    void update(Fine f);
    void delete(String fineID);

    double getTotalSum();

    double getSumByEvent(String eventID);
}

