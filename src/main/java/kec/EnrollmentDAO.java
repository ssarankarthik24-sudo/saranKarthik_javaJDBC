package kec;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EnrollmentDAO {

    public boolean enrollStudent(String rollNo, String courseCode,
                                 int semester) {

        String checkSql =
                "SELECT current_semester FROM students WHERE roll_no = ?";

        String insertSql =
                "INSERT INTO enrollments " +
                "(roll_no, course_code, semester, classes_held, " +
                "classes_attended, medical_condonation) " +
                "VALUES (?, ?, ?, 0, 0, FALSE)";

        try (Connection con = DBConnection.getConnection()) {

            // Check whether the student exists and matches the semester.
            try (PreparedStatement check = con.prepareStatement(checkSql)) {
                check.setString(1, rollNo);

                try (ResultSet rs = check.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Student not found.");
                        return false;
                    }

                    if (rs.getInt("current_semester") != semester) {
                        System.out.println(
                            "Semester does not match the student's current semester."
                        );
                        return false;
                    }
                }
            }

            // Insert the enrollment.
            try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                ps.setString(1, rollNo);
                ps.setString(2, courseCode);
                ps.setInt(3, semester);

                int rows = ps.executeUpdate();

                if (rows > 0) {
                    System.out.println("Student enrolled successfully!");
                    return true;
                }
            }

        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState())) {
                System.out.println(
                    "Enrollment failed. Check the course code or whether " +
                    "the student is already enrolled in this course."
                );
            } else {
                System.out.println("Enrollment error: " + e.getMessage());
            }
        }

        return false;
    }

    public void viewEnrollments() {

        String sql =
                "SELECT e.roll_no, s.student_name, e.course_code, " +
                "c.course_title, e.semester " +
                "FROM enrollments e " +
                "JOIN students s ON e.roll_no = s.roll_no " +
                "JOIN courses c ON e.course_code = c.course_code " +
                "ORDER BY e.roll_no, e.course_code";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n===== STUDENT ENROLLMENTS =====");

            boolean found = false;

            while (rs.next()) {
                found = true;

                System.out.println("Roll Number: " + rs.getString("roll_no"));
                System.out.println("Student: " + rs.getString("student_name"));
                System.out.println("Course Code: " + rs.getString("course_code"));
                System.out.println("Course: " + rs.getString("course_title"));
                System.out.println("Semester: " + rs.getInt("semester"));
                System.out.println("-------------------------------");
            }

            if (!found) {
                System.out.println("No enrollments found.");
            }

        } catch (SQLException e) {
            System.out.println("Error viewing enrollments: " + e.getMessage());
        }
    }
}