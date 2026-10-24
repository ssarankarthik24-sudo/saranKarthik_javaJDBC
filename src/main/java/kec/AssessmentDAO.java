package kec;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class AssessmentDAO {

    public void recordAssessmentMarks() {

        Scanner sc = new Scanner(System.in);

        System.out.println("\n===== RECORD ASSESSMENT MARKS =====");

        System.out.print("Enter Student Roll Number: ");
        String rollNo = sc.nextLine().trim();

        System.out.print("Enter Course Code: ");
        String courseCode = sc.nextLine().trim();

        int semester;
        double cia1;
        double cia2;
        double assignmentMarks;
        double quizMarks;
        double labEvaluation;
        double eseMarks;
        String eseStatus;

        try {
            System.out.print("Enter Semester: ");
            semester = Integer.parseInt(sc.nextLine().trim());

            if (semester < 1 || semester > 8) {
                System.out.println("Semester must be between 1 and 8.");
                return;
            }

            String courseType = getCourseType(courseCode);

            if (courseType == null) {
                System.out.println("Course not found.");
                return;
            }

            int eseMaximum;

            switch (courseType) {
                case "THEORY":
                    eseMaximum = 60;
                    break;

                case "PRACTICAL":
                    eseMaximum = 40;
                    break;

                case "EMBEDDED":
                    eseMaximum = 50;
                    break;

                default:
                    System.out.println("Invalid course type.");
                    return;
            }

            int enrollmentId =
                    getEnrollmentId(rollNo, courseCode, semester);

            if (enrollmentId == -1) {
                System.out.println(
                        "Student is not enrolled in this course and semester."
                );
                return;
            }

            System.out.print("Enter CIA1 marks (0-100): ");
            cia1 = readMark(sc);

            System.out.print("Enter CIA2 marks (0-100): ");
            cia2 = readMark(sc);

            System.out.print("Enter Assignment marks (0-100): ");
            assignmentMarks = readMark(sc);

            System.out.print("Enter Quiz marks (0-100): ");
            quizMarks = readMark(sc);

            System.out.print(
                    "Enter Lab Evaluation marks (0-100, enter 0 if not applicable): "
            );
            labEvaluation = readMark(sc);

            System.out.print("Was the student absent for ESE? (Y/N): ");
            String absentChoice = sc.nextLine().trim();

            if (!absentChoice.equalsIgnoreCase("Y")
                    && !absentChoice.equalsIgnoreCase("N")) {
                System.out.println("Please enter Y or N.");
                return;
            }

            if (absentChoice.equalsIgnoreCase("Y")) {
                eseMarks = 0;
                eseStatus = "AB";
            } else {
                System.out.print(
                        "Enter ESE marks (0-" + eseMaximum + "): "
                );

                eseMarks = readMark(sc);

                if (eseMarks > eseMaximum) {
                    System.out.println(
                            "ESE marks cannot exceed " + eseMaximum + "."
                    );
                    return;
                }

                eseStatus = "PRESENT";
            }

            saveMarks(
                    enrollmentId,
                    cia1,
                    cia2,
                    assignmentMarks,
                    quizMarks,
                    labEvaluation,
                    eseMarks,
                    eseStatus
            );

        } catch (NumberFormatException e) {
            System.out.println(
                    "Invalid input. Please enter numeric values for marks."
            );
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    private double readMark(Scanner sc) {
        double mark = Double.parseDouble(sc.nextLine().trim());

        if (!Double.isFinite(mark) || mark < 0 || mark > 100) {
            throw new NumberFormatException(
                    "Marks must be between 0 and 100."
            );
        }

        return mark;
    }

    private String getCourseType(String courseCode)
            throws SQLException {

        String sql =
                "SELECT course_type FROM courses WHERE course_code = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, courseCode);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("course_type");
                }
            }
        }

        return null;
    }

    private int getEnrollmentId(
            String rollNo,
            String courseCode,
            int semester) throws SQLException {

        String sql = """
                SELECT enrollment_id
                FROM enrollments
                WHERE roll_no = ?
                  AND course_code = ?
                  AND semester = ?
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, rollNo);
            ps.setString(2, courseCode);
            ps.setInt(3, semester);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("enrollment_id");
                }
            }
        }

        return -1;
    }

    private void saveMarks(
            int enrollmentId,
            double cia1,
            double cia2,
            double assignmentMarks,
            double quizMarks,
            double labEvaluation,
            double eseMarks,
            String eseStatus) throws SQLException {

        String sql = """
                INSERT INTO assessment_marks
                    (enrollment_id, cia1, cia2, assignment_marks,
                     quiz_marks, lab_evaluation, ese_marks, ese_status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    cia1 = VALUES(cia1),
                    cia2 = VALUES(cia2),
                    assignment_marks = VALUES(assignment_marks),
                    quiz_marks = VALUES(quiz_marks),
                    lab_evaluation = VALUES(lab_evaluation),
                    ese_marks = VALUES(ese_marks),
                    ese_status = VALUES(ese_status)
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, enrollmentId);
            ps.setDouble(2, cia1);
            ps.setDouble(3, cia2);
            ps.setDouble(4, assignmentMarks);
            ps.setDouble(5, quizMarks);
            ps.setDouble(6, labEvaluation);
            ps.setDouble(7, eseMarks);
            ps.setString(8, eseStatus);

            ps.executeUpdate();

            System.out.println(
                    "\nAssessment marks saved successfully!"
            );

            System.out.println("ESE Status: " + eseStatus);
        }
    }
}
