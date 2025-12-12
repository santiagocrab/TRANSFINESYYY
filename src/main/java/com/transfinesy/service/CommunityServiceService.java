package com.transfinesy.service;

import com.transfinesy.model.CommunityService;
import com.transfinesy.model.Ledger;
import com.transfinesy.repo.CommunityServiceRepository;
import com.transfinesy.repo.CommunityServiceRepositoryImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class CommunityServiceService {
    private CommunityServiceRepository repository;
    private LedgerService ledgerService;

    private static final double CREDIT_PER_HOUR = 50.0;

    public CommunityServiceService() {
        this.repository = new CommunityServiceRepositoryImpl();
        this.ledgerService = new LedgerService();
    }

    public CommunityServiceService(LedgerService ledgerService) {
        this.repository = new CommunityServiceRepositoryImpl();
        this.ledgerService = ledgerService;
    }

    public double calculateCreditAmount(int hoursRendered) {
        return hoursRendered * CREDIT_PER_HOUR;
    }

    public CommunityServiceResult recordCommunityService(String studID, int hoursRendered, LocalDate date) {
        return recordCommunityService(studID, hoursRendered, date, null);
    }

    public CommunityServiceResult recordCommunityService(String studID, int hoursRendered, LocalDate date, String description) {
        if (studID == null || studID.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID is required");
        }
        if (hoursRendered <= 0) {
            throw new IllegalArgumentException("Hours rendered must be positive");
        }

        Ledger currentLedger = ledgerService.getLedgerForStudent(studID);
        double remainingBalance = currentLedger.getBalance();

        double computedCredit = calculateCreditAmount(hoursRendered);
        int originalHours = hoursRendered;
        boolean wasAdjusted = false;

        if (remainingBalance <= 0) {
            throw new IllegalArgumentException("Student balance is already cleared. No community service credit can be applied.");
        } else if (computedCredit > remainingBalance) {
            computedCredit = remainingBalance;
            hoursRendered = (int) Math.ceil(remainingBalance / CREDIT_PER_HOUR);
            if (hoursRendered < 1 && remainingBalance > 0) {
                hoursRendered = 1;
                computedCredit = Math.min(remainingBalance, CREDIT_PER_HOUR);
            }
            wasAdjusted = true;
        }

        String serviceID = "SVC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        CommunityService service = new CommunityService(serviceID, studID, hoursRendered, computedCredit, date, description);
        repository.save(service);

        return new CommunityServiceResult(service, wasAdjusted, originalHours, hoursRendered, remainingBalance, computedCredit);
    }

    public static class CommunityServiceResult {
        private final CommunityService service;
        private final boolean wasAdjusted;
        private final int originalHours;
        private final int adjustedHours;
        private final double remainingBalance;
        private final double appliedCredit;

        public CommunityServiceResult(CommunityService service, boolean wasAdjusted, int originalHours,
                                     int adjustedHours, double remainingBalance, double appliedCredit) {
            this.service = service;
            this.wasAdjusted = wasAdjusted;
            this.originalHours = originalHours;
            this.adjustedHours = adjustedHours;
            this.remainingBalance = remainingBalance;
            this.appliedCredit = appliedCredit;
        }

        public CommunityService getService() {
            return service;
        }

        public boolean wasAdjusted() {
            return wasAdjusted;
        }

        public int getOriginalHours() {
            return originalHours;
        }

        public int getAdjustedHours() {
            return adjustedHours;
        }

        public double getRemainingBalance() {
            return remainingBalance;
        }

        public double getAppliedCredit() {
            return appliedCredit;
        }
    }

    public List<CommunityService> getServicesByStudent(String studID) {
        return repository.findByStudent(studID);
    }

    public CommunityService getServiceById(String serviceID) {
        return repository.findById(serviceID);
    }

    public List<CommunityService> getAllServices() {
        return repository.findAll();
    }

    public void updateService(CommunityService service) {
        if (service == null) {
            throw new IllegalArgumentException("Service cannot be null");
        }
        repository.update(service);
    }

    public void deleteService(String serviceID) {
        repository.delete(serviceID);
    }
}
