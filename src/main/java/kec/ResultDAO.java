package kec;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class ResultDAO {

    public void calculateStudentResult() {

        Scanner sc = new Scanner(System.in);

        System.out.println("\n===== STUDENT RESULT CALCULATION =====");

        System.out.print("Enter Student Roll Number: ");
        String rollNo = sc.nextLine().trim();

        System.out.print("Enter Course Code: ");
        String courseCode = sc.nextLine().trim();

        System.out.print("Enter Semester: ");

        int semester;

        try {
            semester = Integer.parseInt(sc.nextLine().trim());

            if (semester < 1 || semester > 8) {
                System.out.println("Semester must be between 1 and 8.");
                return;
            }

            calculateResult(rollNo, courseCode, semester);

        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid semester.");
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    private void calculateResult(
            String rollNo,
            String courseCode,
            int semester) throws SQLException {

        String sql = """
                SELECT
                    s.student_name,
                    s.department,
                    c.course_title,
                    c.course_type,
                    c.credits,
                    e.classes_held,
                    e.classes_attended,
                    e.medical_condonation,
                    a.cia1,
                    a.cia2,
                    a.assignment_marks,
                    a.quiz_marks,
                    a.lab_evaluation,
                    a.ese_marks,
                    a.ese_status
                FROM enrollments e
                JOIN students s
                    ON e.roll_no = s.roll_no
                JOIN courses c
                    ON e.course_code = c.course_code
                LEFT JOIN assessment_marks a
                    ON e.enrollment_id = a.enrollment_id
                WHERE e.roll_no = ?
                  AND e.course_code = ?
                  AND e.semester = ?
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, rollNo);
            ps.setString(2, courseCode);
            ps.setInt(3, semester);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    System.out.println(
                            "Student enrollment not found."
                    );
                    return;
                }

                String studentName = rs.getString("student_name");
                String department = rs.getString("department");
                String courseTitle = rs.getString("course_title");
                String courseType = rs.getString("course_type");
                int credits = rs.getInt("credits");

                int classesHeld = rs.getInt("classes_held");
                int classesAttended = rs.getInt("classes_attended");
                boolean condonation =
                        rs.getBoolean("medical_condonation");

                double attendance = 0;

                if (classesHeld > 0) {
                    attendance =
                            (classesAttended * 100.0) / classesHeld;
                }

                System.out.println("\n========================================");
                System.out.println("          STUDENT RESULT");
                System.out.println("========================================");
                System.out.println("Roll Number : " + rollNo);
                System.out.println("Name        : " + studentName);
                System.out.println("Department  : " + department);
                System.out.println("Course Code : " + courseCode);
                System.out.println("Course Name : " + courseTitle);
                System.out.println("Course Type : " + courseType);
                System.out.println("Credits     : " + credits);
                System.out.printf(
                        "Attendance  : %.2f%%%n", attendance
                );

                // Check attendance eligibility.
                if (classesHeld == 0) {
                    System.out.println(
                            "Result      : SA - Attendance not recorded"
                    );
                    System.out.println("Grade Point : 0");
                    System.out.println("========================================");
                    return;
                }

                if (attendance < 65) {
                    System.out.println(
                            "Result      : SA - ESE barred"
                    );
                    System.out.println("Grade Point : 0");
                    System.out.println("========================================");
                    return;
                }

                if (attendance < 75 && !condonation) {
                    System.out.println(
                            "Result      : SA - Medical condonation required"
                    );
                    System.out.println("Grade Point : 0");
                    System.out.println("========================================");
                    return;
                }

                if (attendance < 75) {
                    System.out.println(
                            "Attendance Status: Conditional - condonation recorded"
                    );
                } else {
                    System.out.println(
                            "Attendance Status: ESE Eligible"
                    );
                }

                // Check whether assessment marks exist.
                Object cia1Value = rs.getObject("cia1");
                Object cia2Value = rs.getObject("cia2");
                Object assignmentValue =
                        rs.getObject("assignment_marks");
                Object quizValue = rs.getObject("quiz_marks");
                Object labValue = rs.getObject("lab_evaluation");
                Object eseValue = rs.getObject("ese_marks");

                String eseStatus = rs.getString("ese_status");

                if (cia1Value == null
                        || cia2Value == null
                        || assignmentValue == null
                        || quizValue == null
                        || labValue == null
                        || eseValue == null
                        || eseStatus == null
                        || eseStatus.equals("PENDING")) {

                    System.out.println(
                            "Result      : Assessment marks are incomplete."
                    );
                    System.out.println("========================================");
                    return;
                }

                double cia1 = rs.getDouble("cia1");
                double cia2 = rs.getDouble("cia2");
                double assignment =
                        rs.getDouble("assignment_marks");
                double quiz = rs.getDouble("quiz_marks");
                double lab = rs.getDouble("lab_evaluation");
                double ese = rs.getDouble("ese_marks");

                // Assumption: CIA is the average of five components.
                double cia =
                        (cia1 + cia2 + assignment + quiz + lab) / 5.0;

                double ciaWeight;
                double eseWeight;
                double eseMaximum;
                double eseMinimum;

                switch (courseType) {

                    case "THEORY":
                        ciaWeight = 0.40;
                        eseWeight = 0.60;
                        eseMaximum = 60;
                        eseMinimum = 27;
                        break;

                    case "PRACTICAL":
                        ciaWeight = 0.60;
                        eseWeight = 0.40;
                        eseMaximum = 40;
                        eseMinimum = 20;
                        break;

                    case "EMBEDDED":
                        ciaWeight = 0.50;
                        eseWeight = 0.50;
                        eseMaximum = 50;
                        eseMinimum = 22.5;
                        break;

                    default:
                        System.out.println("Unknown course type.");
                        return;
                }

                double finalCIA = cia * ciaWeight;
                double finalESE = (ese / eseMaximum * 100) * eseWeight;
                double total = finalCIA + finalESE;

                String grade;
                int gradePoint;

                if (eseStatus.equals("AB")) {
                    grade = "AB";
                    gradePoint = 0;
                } else if (ese < eseMinimum || total < 50) {
                    grade = "RA";
                    gradePoint = 0;
                } else if (total >= 91) {
                    grade = "O";
                    gradePoint = 10;
                } else if (total >= 81) {
                    grade = "A+";
                    gradePoint = 9;
                } else if (total >= 71) {
                    grade = "A";
                    gradePoint = 8;
                } else if (total >= 61) {
                    grade = "B+";
                    gradePoint = 7;
                } else {
                    grade = "B";
                    gradePoint = 6;
                }

                System.out.printf("CIA Average : %.2f / 100%n", cia);
                System.out.printf("CIA Weighted: %.2f%n", finalCIA);
                System.out.printf(
                        "ESE Marks   : %.2f / %.0f%n",
                        ese, eseMaximum
                );
                System.out.printf("ESE Weighted: %.2f%n", finalESE);
                System.out.printf("Final Total : %.2f / 100%n", total);
                System.out.println("Grade       : " + grade);
                System.out.println("Grade Point : " + gradePoint);

                if (gradePoint == 0) {
                    System.out.println("Status      : FAIL");
                } else {
                    System.out.println("Status      : PASS");
                }

                System.out.println("========================================");
            }
        }
    }
}
