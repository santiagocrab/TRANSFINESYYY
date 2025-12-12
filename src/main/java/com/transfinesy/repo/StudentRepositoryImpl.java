package com.transfinesy.repo;

import com.transfinesy.config.DBConfig;
import com.transfinesy.model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentRepositoryImpl implements StudentRepository {

    @Override
    public List<Student> findAll() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT stud_id, first_name, last_name, course, year_level, section, rfid_tag FROM students ORDER BY last_name, first_name";

        try (Connection conn = DBConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Student student = new Student(
                    rs.getString("stud_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("course"),
                    rs.getString("year_level"),
                    rs.getString("section"),
                    rs.getString("rfid_tag")
                );
                students.add(student);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return students;
    }

    @Override
    public Student findById(String studID) {
        String sql = "SELECT stud_id, first_name, last_name, course, year_level, section, rfid_tag FROM students WHERE stud_id = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Student(
                    rs.getString("stud_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("course"),
                    rs.getString("year_level"),
                    rs.getString("section"),
                    rs.getString("rfid_tag")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void save(Student s) {
        String sql = "INSERT INTO students (stud_id, first_name, last_name, course, year_level, section, rfid_tag) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, s.getStudID());
            pstmt.setString(2, s.getFirstName());
            pstmt.setString(3, s.getLastName());
            pstmt.setString(4, s.getCourse());
            pstmt.setString(5, s.getYearLevel());
            pstmt.setString(6, s.getSection());
            pstmt.setString(7, s.getRfidTag());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            String errorMsg = "Database error: " + e.getMessage();
            if (e.getMessage().contains("Unknown database")) {
                errorMsg = "Database 'transfinesy' does not exist. Please create it first.";
            } else if (e.getMessage().contains("Table") && e.getMessage().contains("doesn't exist")) {
                errorMsg = "Database tables not found. Please run the schema.sql script.";
            } else if (e.getMessage().contains("Access denied")) {
                errorMsg = "Database access denied. Please check your credentials in db.properties.";
            } else if (e.getMessage().contains("Communications link failure") || e.getMessage().contains("Connection refused")) {
                errorMsg = "Cannot connect to database. Please ensure MySQL is running and check your connection settings.";
            }
            throw new RuntimeException(errorMsg, e);
        }
    }

    @Override
    public void update(Student s) {
        String sql = "UPDATE students SET first_name = ?, last_name = ?, course = ?, year_level = ?, section = ?, rfid_tag = ? WHERE stud_id = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, s.getFirstName());
            pstmt.setString(2, s.getLastName());
            pstmt.setString(3, s.getCourse());
            pstmt.setString(4, s.getYearLevel());
            pstmt.setString(5, s.getSection());
            pstmt.setString(6, s.getRfidTag());
            pstmt.setString(7, s.getStudID());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update student", e);
        }
    }

    @Override
    public void delete(String studID) {
        String sql = "DELETE FROM students WHERE stud_id = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to delete student", e);
        }
    }

    @Override
    public List<Student> findByCourse(String course) {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT stud_id, first_name, last_name, course, year_level, section, rfid_tag FROM students WHERE course = ? ORDER BY last_name, first_name";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, course);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Student student = new Student(
                    rs.getString("stud_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("course"),
                    rs.getString("year_level"),
                    rs.getString("section"),
                    rs.getString("rfid_tag")
                );
                students.add(student);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return students;
    }

    @Override
    public List<Student> searchByName(String name) {
        List<Student> students = new ArrayList<>();

        String sql = "SELECT stud_id, first_name, last_name, course, year_level, section, rfid_tag FROM students " +
                     "WHERE first_name LIKE ? OR last_name LIKE ? " +
                     "OR CONCAT(first_name, ' ', last_name) LIKE ? " +
                     "OR CONCAT(last_name, ' ', first_name) LIKE ? " +
                     "OR CONCAT(first_name, last_name) LIKE ? " +
                     "OR CONCAT(last_name, first_name) LIKE ? " +
                     "ORDER BY last_name, first_name";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String normalizedName = name.trim().replaceAll("\\s+", " ");
            String searchPattern = "%" + normalizedName + "%";
            String noSpacePattern = "%" + normalizedName.replaceAll("\\s+", "") + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            pstmt.setString(4, searchPattern);
            pstmt.setString(5, noSpacePattern);
            pstmt.setString(6, noSpacePattern);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Student student = new Student(
                    rs.getString("stud_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("course"),
                    rs.getString("year_level"),
                    rs.getString("section"),
                    rs.getString("rfid_tag")
                );
                students.add(student);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return students;
    }

    @Override
    public List<Student> findByYearLevel(String yearLevel) {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT stud_id, first_name, last_name, course, year_level, section, rfid_tag FROM students WHERE year_level = ? ORDER BY last_name, first_name";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, yearLevel);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Student student = new Student(
                    rs.getString("stud_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("course"),
                    rs.getString("year_level"),
                    rs.getString("section"),
                    rs.getString("rfid_tag")
                );
                students.add(student);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return students;
    }

    @Override
    public List<Student> findBySection(String section) {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT stud_id, first_name, last_name, course, year_level, section, rfid_tag FROM students WHERE section = ? ORDER BY last_name, first_name";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, section);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Student student = new Student(
                    rs.getString("stud_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("course"),
                    rs.getString("year_level"),
                    rs.getString("section"),
                    rs.getString("rfid_tag")
                );
                students.add(student);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return students;
    }

    @Override
    public List<Student> searchByID(String studID) {
        List<Student> students = new ArrayList<>();

        String sql = "SELECT stud_id, first_name, last_name, course, year_level, section, rfid_tag FROM students WHERE stud_id LIKE ? ORDER BY last_name, first_name";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + studID + "%";
            pstmt.setString(1, searchPattern);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Student student = new Student(
                    rs.getString("stud_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("course"),
                    rs.getString("year_level"),
                    rs.getString("section"),
                    rs.getString("rfid_tag")
                );
                students.add(student);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return students;
    }

    @Override
    public List<Student> search(String query) {
        List<Student> students = new ArrayList<>();

        String sql = "SELECT stud_id, first_name, last_name, course, year_level, section, rfid_tag FROM students " +
                     "WHERE stud_id LIKE ? OR first_name LIKE ? OR last_name LIKE ? OR course LIKE ? OR year_level LIKE ? OR section LIKE ? OR rfid_tag LIKE ? " +
                     "OR CONCAT(first_name, ' ', last_name) LIKE ? " +
                     "OR CONCAT(last_name, ' ', first_name) LIKE ? " +
                     "OR CONCAT(first_name, last_name) LIKE ? " +
                     "OR CONCAT(last_name, first_name) LIKE ? " +
                     "ORDER BY last_name, first_name";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String normalizedQuery = query.trim().replaceAll("\\s+", " ");
            String searchPattern = "%" + normalizedQuery + "%";
            String noSpacePattern = "%" + normalizedQuery.replaceAll("\\s+", "") + "%";

            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            pstmt.setString(4, searchPattern);
            pstmt.setString(5, searchPattern);
            pstmt.setString(6, searchPattern);
            pstmt.setString(7, searchPattern);
            pstmt.setString(8, searchPattern);
            pstmt.setString(9, searchPattern);
            pstmt.setString(10, noSpacePattern);
            pstmt.setString(11, noSpacePattern);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Student student = new Student(
                    rs.getString("stud_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("course"),
                    rs.getString("year_level"),
                    rs.getString("section"),
                    rs.getString("rfid_tag")
                );
                students.add(student);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return students;
    }

    @Override
    public Student findByRFID(String rfidTag) {

        String sql = "SELECT stud_id, first_name, last_name, course, year_level, section, rfid_tag FROM students WHERE rfid_tag = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, rfidTag);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Student student = new Student(
                    rs.getString("stud_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("course"),
                    rs.getString("year_level"),
                    rs.getString("section"),
                    rs.getString("rfid_tag")
                );
                return student;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Student> searchByRFIDPartial(String rfidTag) {
        List<Student> students = new ArrayList<>();

        String sql = "SELECT stud_id, first_name, last_name, course, year_level, section, rfid_tag FROM students WHERE rfid_tag LIKE ? ORDER BY last_name, first_name";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + rfidTag + "%";
            pstmt.setString(1, searchPattern);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Student student = new Student(
                    rs.getString("stud_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("course"),
                    rs.getString("year_level"),
                    rs.getString("section"),
                    rs.getString("rfid_tag")
                );
                students.add(student);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return students;
    }

    @Override
    public List<String> getDistinctYearLevels() {
        List<String> yearLevels = new ArrayList<>();
        String sql = "SELECT DISTINCT year_level FROM students WHERE year_level IS NOT NULL AND year_level != '' ORDER BY year_level";

        try (Connection conn = DBConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String yearLevel = rs.getString("year_level");
                if (yearLevel != null && !yearLevel.isEmpty()) {
                    yearLevels.add(yearLevel);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return yearLevels;
    }

    @Override
    public List<String> getDistinctSections() {
        List<String> sections = new ArrayList<>();
        String sql = "SELECT DISTINCT section FROM students WHERE section IS NOT NULL AND section != '' ORDER BY section";

        try (Connection conn = DBConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String section = rs.getString("section");
                if (section != null && !section.isEmpty()) {
                    sections.add(section);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sections;
    }

    @Override
    public List<String> getDistinctCourses() {
        List<String> courses = new ArrayList<>();
        String sql = "SELECT DISTINCT course FROM students WHERE course IS NOT NULL AND course != '' ORDER BY course";

        try (Connection conn = DBConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String course = rs.getString("course");
                if (course != null && !course.isEmpty()) {
                    courses.add(course);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return courses;
    }
}

