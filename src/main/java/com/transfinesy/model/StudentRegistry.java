package com.transfinesy.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StudentRegistry {
    private List<Student> students;

    public StudentRegistry() {
        this.students = new ArrayList<>();
    }

    public List<Student> getAllStudents() {
        return new ArrayList<>(students);
    }

    public List<Student> getStudentsEligibleForClearance(ClearanceService clearanceService) {

        return new ArrayList<>();
    }

    public List<Student> getStudentsWithBalance(ClearanceService clearanceService) {

        return new ArrayList<>();
    }

    public void addStudent(Student s) {
        if (s != null && !students.contains(s)) {
            students.add(s);
        }
    }

    public void removeStudent(String studID) {
        students.removeIf(s -> s.getStudID().equals(studID));
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students != null ? students : new ArrayList<>();
    }
}

