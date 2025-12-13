package com.transfinesy.repo;

import com.transfinesy.model.CommunityService;
import java.time.LocalDate;
import java.util.List;

public interface CommunityServiceRepository {
    List<CommunityService> findAll();
    CommunityService findById(String serviceID);
    List<CommunityService> findByStudent(String studID);
    void save(CommunityService cs);
    void update(CommunityService cs);
    void delete(String serviceID);

    int getTotalHours();

    double getTotalCredits();

    int getHoursByDateRange(LocalDate startDate, LocalDate endDate);

    double getCreditsByDateRange(LocalDate startDate, LocalDate endDate);
}

