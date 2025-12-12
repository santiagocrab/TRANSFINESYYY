package com.transfinesy.util;

import com.transfinesy.model.Student;
import com.transfinesy.service.StudentService;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVImporter {

    public static int importStudents(String csvFilePath, StudentService studentService) {
        List<Student> students = new ArrayList<>();
        int imported = 0;
        int skipped = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] values = line.split(",");
                if (values.length >= 6) {
                    String rfidTag = values[0].trim();
                    String studID = values[1].trim();
                    String firstName = values[2].trim();
                    String lastName = values[3].trim();
                    String course = values[4].trim();
                    String yearLevel = values[5].trim();
                    String section = values.length > 6 ? values[6].trim() : "";

                    if (studID.isEmpty()) {
                        skipped++;
                        continue;
                    }

                    if (rfidTag.isEmpty()) {
                        rfidTag = null;
                    }

                    Student student = new Student(studID, firstName, lastName, course, yearLevel, section, rfidTag);
                    students.add(student);
                } else {
                    skipped++;
                }
            }

            for (Student student : students) {
                try {

                    if (studentService.getStudentById(student.getStudID()) == null) {
                        studentService.addStudent(student);
                        imported++;
                    } else {

                        Student existing = studentService.getStudentById(student.getStudID());
                        if (existing.getRfidTag() == null || existing.getRfidTag().isEmpty()) {
                            existing.setRfidTag(student.getRfidTag());
                            studentService.updateStudent(existing);
                            imported++;
                        } else {
                            skipped++;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error importing student " + student.getStudID() + ": " + e.getMessage());
                    skipped++;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Error reading CSV file: " + e.getMessage(), e);
        }

        System.out.println("Import completed: " + imported + " imported, " + skipped + " skipped");
        return imported;
    }
}
