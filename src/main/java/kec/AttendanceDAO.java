package kec;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AttendanceDAO {

    public void recordAttendance(
            String rollNo,
            String courseCode,
            int semester,
            int classesHeld,
            int classesAttended,
            boolean medicalCondonation) {

        if (classesHeld < 0 || classesAttended < 0
                || classesAttended > classesHeld) {
            System.out.println("Invalid attendance values.");
            return;
        }

        String sql = "UPDATE enrollments SET classes_held = ?, " +
                "classes_attended = ?, medical_condonation = ? " +
                "WHERE roll_no = ? AND course_code = ? AND semester = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, classesHeld);
            ps.setInt(2, classesAttended);
            ps.setBoolean(3, medicalCondonation);
            ps.setString(4, rollNo);
            ps.setString(5, courseCode);
            ps.setInt(6, semester);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Attendance recorded successfully!");
                displayAttendance(rollNo, courseCode, semester);
            } else {
                System.out.println(
                    "Enrollment not found. Enroll the student first."
                );
            }

        } catch (SQLException e) {
            System.out.println("Attendance error: " + e.getMessage());
        }
    }

    public void displayAttendance(
            String rollNo, String courseCode, int semester) {

        String sql = "SELECT classes_held, classes_attended, " +
                "medical_condonation FROM enrollments " +
                "WHERE roll_no = ? AND course_code = ? AND semester = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, rollNo);
            ps.setString(2, courseCode);
            ps.setInt(3, semester);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    System.out.println("Enrollment not found.");
                    return;
                }

                int held = rs.getInt("classes_held");
                int attended = rs.getInt("classes_attended");
                boolean condonation =
                        rs.getBoolean("medical_condonation");

                double percentage = held == 0
                        ? 0.0
                        : attended * 100.0 / held;

                System.out.printf(
                    "Attendance: %.2f%% (%d/%d classes)%n",
                    percentage, attended, held
                );

                if (held == 0) {
                    System.out.println(
                        "Status: Attendance not yet available."
                    );
                } else if (percentage >= 75.0) {
                    System.out.println("Status: ESE Eligible");
                } else if (percentage >= 65.0) {
                    if (condonation) {
                        System.out.println(
                            "Status: Conditional attendance - " +
                            "medical condonation recorded; verify approval."
                        );
                    } else {
                        System.out.println(
                            "Status: Conditional - medical condonation " +
                            "required; ESE eligibility not yet confirmed."
                        );
                    }
                } else {
                    System.out.println("Status: SA - ESE barred");
                }
            }

        } catch (SQLException e) {
            System.out.println(
                "Error displaying attendance: " + e.getMessage()
            );
        }
    }

    public void viewAllAttendance() {

        String sql = "SELECT e.roll_no, s.student_name, " +
                "e.course_code, c.course_title, e.semester, " +
                "e.classes_held, e.classes_attended, " +
                "e.medical_condonation " +
                "FROM enrollments e " +
                "JOIN students s ON e.roll_no = s.roll_no " +
                "JOIN courses c ON e.course_code = c.course_code " +
                "ORDER BY e.roll_no, e.course_code";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n===== ATTENDANCE REPORT =====");

            while (rs.next()) {
                int held = rs.getInt("classes_held");
                int attended = rs.getInt("classes_attended");

                double percentage = held == 0
                        ? 0.0
                        : attended * 100.0 / held;

                System.out.println(
                    "\nRoll Number: " + rs.getString("roll_no")
                );
                System.out.println(
                    "Student: " + rs.getString("student_name")
                );
                System.out.println(
                    "Course: " + rs.getString("course_code") +
                    " - " + rs.getString("course_title")
                );
                System.out.println("Semester: " + rs.getInt("semester"));

                System.out.printf(
                    "Attendance: %.2f%% (%d/%d)%n",
                    percentage, attended, held
                );

                if (held == 0) {
                    System.out.println("Status: Not yet available");
                } else if (percentage >= 75.0) {
                    System.out.println("Status: ESE Eligible");
                } else if (percentage >= 65.0) {
                    System.out.println(
                        "Status: Conditional - check medical condonation"
                    );
                } else {
                    System.out.println("Status: SA - ESE barred");
                }

                System.out.println("-------------------------------");
            }

        } catch (SQLException e) {
            System.out.println(
                "Error generating attendance report: " + e.getMessage()
            );
        }
    }
}
