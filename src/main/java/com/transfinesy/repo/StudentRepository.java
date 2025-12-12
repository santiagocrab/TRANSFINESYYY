package com.transfinesy.repo;

import com.transfinesy.model.Student;
import java.util.List;

public interface StudentRepository {
    List<Student> findAll();
    Student findById(String studID);
    void save(Student s);
    void update(Student s);
    void delete(String studID);
    List<Student> findByCourse(String course);
    List<Student> findByYearLevel(String yearLevel);
    List<Student> findBySection(String section);
    Student findByRFID(String rfidTag);
    List<Student> searchByName(String name);
    List<Student> searchByID(String studID);
    List<Student> search(String query);
    List<Student> searchByRFIDPartial(String rfidTag);

    List<String> getDistinctYearLevels();

    List<String> getDistinctSections();

    List<String> getDistinctCourses();
}

