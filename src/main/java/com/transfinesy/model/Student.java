package com.transfinesy.model;

public class Student {
    private String studID;
    private String firstName;
    private String lastName;
    private String course;
    private String yearLevel;
    private String section;
    private String rfidTag;

    public Student() {
    }

    public Student(String studID, String firstName, String lastName, String course, String yearLevel, String section) {
        this.studID = studID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.course = course;
        this.yearLevel = yearLevel;
        this.section = section;
    }

    public Student(String studID, String firstName, String lastName, String course, String yearLevel, String section, String rfidTag) {
        this.studID = studID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.course = course;
        this.yearLevel = yearLevel;
        this.section = section;
        this.rfidTag = rfidTag;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getStudID() {
        return studID;
    }

    public void setStudID(String studID) {
        this.studID = studID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getYearLevel() {
        return yearLevel;
    }

    public void setYearLevel(String yearLevel) {
        this.yearLevel = yearLevel;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getRfidTag() {
        return rfidTag;
    }

    public void setRfidTag(String rfidTag) {
        this.rfidTag = rfidTag;
    }

    @Override
    public String toString() {
        return "Student{" +
                "studID='" + studID + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", course='" + course + '\'' +
                ", yearLevel='" + yearLevel + '\'' +
                ", section='" + section + '\'' +
                ", rfidTag='" + rfidTag + '\'' +
                '}';
    }
}

