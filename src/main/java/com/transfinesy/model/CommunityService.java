package com.transfinesy.model;

import java.time.LocalDate;

public class CommunityService {
    private String serviceID;
    private String studID;
    private int hoursRendered;
    private double creditAmount;
    private LocalDate date;
    private String description;

    public CommunityService() {
    }

    public CommunityService(String serviceID, String studID, int hoursRendered, double creditAmount, LocalDate date) {
        this.serviceID = serviceID;
        this.studID = studID;
        this.hoursRendered = hoursRendered;
        this.creditAmount = creditAmount;
        this.date = date;
    }

    public CommunityService(String serviceID, String studID, int hoursRendered, double creditAmount, LocalDate date, String description) {
        this.serviceID = serviceID;
        this.studID = studID;
        this.hoursRendered = hoursRendered;
        this.creditAmount = creditAmount;
        this.date = date;
        this.description = description;
    }

    public String getServiceID() {
        return serviceID;
    }

    public void setServiceID(String serviceID) {
        this.serviceID = serviceID;
    }

    public String getStudID() {
        return studID;
    }

    public void setStudID(String studID) {
        this.studID = studID;
    }

    public int getHoursRendered() {
        return hoursRendered;
    }

    public void setHoursRendered(int hoursRendered) {
        this.hoursRendered = hoursRendered;
    }

    public double getCreditAmount() {
        return creditAmount;
    }

    public void setCreditAmount(double creditAmount) {
        this.creditAmount = creditAmount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "CommunityService{" +
                "serviceID='" + serviceID + '\'' +
                ", studID='" + studID + '\'' +
                ", hoursRendered=" + hoursRendered +
                ", creditAmount=" + creditAmount +
                ", date=" + date +
                '}';
    }
}

