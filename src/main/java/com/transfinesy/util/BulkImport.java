package com.transfinesy.util;

import com.transfinesy.service.StudentService;

public class BulkImport {

    public static void main(String[] args) {
        String csvFile = "transFINESy_bulk_students.csv";
        if (args.length > 0) {
            csvFile = args[0];
        }

        try {
            StudentService studentService = new StudentService();
            int imported = CSVImporter.importStudents(csvFile, studentService);
            System.out.println("Successfully imported " + imported + " students from " + csvFile);
        } catch (Exception e) {
            System.err.println("Error importing students: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

