package com.transfinesy.model;

import java.time.LocalDateTime;

public class Attendance {
    private String attendanceID;
    private String studID;
    private String eventID;
    private AttendanceStatus status;
    private int minutesLate;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private String scanSource;
    private String session;
    private String recordType;

    public Attendance() {
    }

    public Attendance(String attendanceID, String studID, String eventID, AttendanceStatus status, int minutesLate) {
        this.attendanceID = attendanceID;
        this.studID = studID;
        this.eventID = eventID;
        this.status = status;
        this.minutesLate = minutesLate;
    }

    public Attendance(String attendanceID, String studID, String eventID, AttendanceStatus status, int minutesLate, LocalDateTime checkInTime, LocalDateTime checkOutTime) {
        this.attendanceID = attendanceID;
        this.studID = studID;
        this.eventID = eventID;
        this.status = status;
        this.minutesLate = minutesLate;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.scanSource = "MANUAL";
    }

    public Attendance(String attendanceID, String studID, String eventID, AttendanceStatus status, int minutesLate, LocalDateTime checkInTime, LocalDateTime checkOutTime, String scanSource) {
        this.attendanceID = attendanceID;
        this.studID = studID;
        this.eventID = eventID;
        this.status = status;
        this.minutesLate = minutesLate;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.scanSource = scanSource;
    }

    public String getAttendanceID() {
        return attendanceID;
    }

    public void setAttendanceID(String attendanceID) {
        this.attendanceID = attendanceID;
    }

    public String getStudID() {
        return studID;
    }

    public void setStudID(String studID) {
        this.studID = studID;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public int getMinutesLate() {
        return minutesLate;
    }

    public void setMinutesLate(int minutesLate) {
        this.minutesLate = minutesLate;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalDateTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public String getScanSource() {
        return scanSource;
    }

    public void setScanSource(String scanSource) {
        this.scanSource = scanSource;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    @Override
    public String toString() {
        return "Attendance{" +
                "attendanceID='" + attendanceID + '\'' +
                ", studID='" + studID + '\'' +
                ", eventID='" + eventID + '\'' +
                ", status=" + status +
                ", minutesLate=" + minutesLate +
                ", checkInTime=" + checkInTime +
                ", checkOutTime=" + checkOutTime +
                ", scanSource='" + scanSource + '\'' +
                ", session='" + session + '\'' +
                ", recordType='" + recordType + '\'' +
                '}';
    }
}

