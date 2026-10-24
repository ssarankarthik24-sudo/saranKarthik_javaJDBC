package kec;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class SGPADao {

    public void calculateSGPA() {

        Scanner sc = new Scanner(System.in);

        System.out.println("\n===== SGPA CALCULATION =====");

        System.out.print("Enter Student Roll Number: ");
        String rollNo = sc.nextLine().trim();

        System.out.print("Enter Semester: ");

        int semester;

        try {
            semester = Integer.parseInt(sc.nextLine().trim());

            if (semester < 1 || semester > 8) {
                System.out.println("Semester must be between 1 and 8.");
                return;
            }

            calculateSGPAForStudent(rollNo, semester);

        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid semester.");
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    private void calculateSGPAForStudent(
            String rollNo,
            int semester) throws SQLException {

        String sql = """
                SELECT
                    s.student_name,
                    s.department,
                    c.course_code,
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
                  AND e.semester = ?
                ORDER BY c.course_code
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, rollNo);
            ps.setInt(2, semester);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.isBeforeFirst()) {
                    System.out.println(
                            "No enrollments found for this student and semester."
                    );
                    return;
                }

                double weightedGradePoints = 0;
                int totalCredits = 0;
                int courseCount = 0;
                boolean allResultsAvailable = true;
                String studentName = "";
                String department = "";

                System.out.println("\n==============================================================");
                System.out.println("                 SEMESTER GRADE REPORT");
                System.out.println("==============================================================");

                System.out.printf(
                        "%-10s %-25s %7s %8s %8s%n",
                        "Code", "Course", "Credits", "Grade", "Points"
                );

                System.out.println(
                        "--------------------------------------------------------------"
                );

                while (rs.next()) {

                    courseCount++;

                    studentName = rs.getString("student_name");
                    department = rs.getString("department");

                    String courseCode = rs.getString("course_code");
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
                                classesAttended * 100.0 / classesHeld;
                    }

                    Object cia1Value = rs.getObject("cia1");
                    Object cia2Value = rs.getObject("cia2");
                    Object assignmentValue =
                            rs.getObject("assignment_marks");
                    Object quizValue = rs.getObject("quiz_marks");
                    Object labValue = rs.getObject("lab_evaluation");
                    Object eseValue = rs.getObject("ese_marks");

                    String eseStatus = rs.getString("ese_status");

                    if (classesHeld == 0
                            || attendance < 65
                            || (attendance < 75 && !condonation)) {

                        System.out.printf(
                                "%-10s %-25s %7d %8s %8d%n",
                                courseCode,
                                shorten(courseTitle, 25),
                                credits,
                                "SA",
                                0
                        );

                        totalCredits += credits;
                        allResultsAvailable = false;
                        continue;
                    }

                    if (cia1Value == null
                            || cia2Value == null
                            || assignmentValue == null
                            || quizValue == null
                            || labValue == null
                            || eseValue == null
                            || eseStatus == null) {

                        System.out.printf(
                                "%-10s %-25s %7d %8s %8s%n",
                                courseCode,
                                shorten(courseTitle, 25),
                                credits,
                                "PENDING",
                                "-"
                        );

                        totalCredits += credits;
                        allResultsAvailable = false;
                        continue;
                    }

                    double cia1 = rs.getDouble("cia1");
                    double cia2 = rs.getDouble("cia2");
                    double assignment =
                            rs.getDouble("assignment_marks");
                    double quiz = rs.getDouble("quiz_marks");
                    double lab = rs.getDouble("lab_evaluation");
                    double ese = rs.getDouble("ese_marks");

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
                            System.out.println(
                                    "Unknown course type: " + courseType
                            );
                            allResultsAvailable = false;
                            continue;
                    }

                    double finalCIA = cia * ciaWeight;
                    double finalESE =
                            (ese / eseMaximum * 100) * eseWeight;

                    double total = finalCIA + finalESE;

                    String grade;
                    int gradePoint;

                    if ("AB".equalsIgnoreCase(eseStatus)) {
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

                    System.out.printf(
                            "%-10s %-25s %7d %8s %8d%n",
                            courseCode,
                            shorten(courseTitle, 25),
                            credits,
                            grade,
                            gradePoint
                    );

                    weightedGradePoints += credits * gradePoint;
                    totalCredits += credits;
                }

                System.out.println(
                        "--------------------------------------------------------------"
                );

                System.out.println("Student Name: " + studentName);
                System.out.println("Department  : " + department);
                System.out.println("Semester    : " + semester);
                System.out.println("Courses     : " + courseCount);
                System.out.println("Total Credits: " + totalCredits);

                if (!allResultsAvailable || totalCredits == 0) {
                    System.out.println(
                            "\nSGPA cannot be finalized because one or more course results are incomplete or attendance-ineligible."
                    );
                    return;
                }

                double sgpa = weightedGradePoints / totalCredits;
                double percentage = sgpa * 10;

                System.out.printf("SGPA        : %.2f / 10.00%n", sgpa);
                System.out.printf(
                        "Percentage  : %.2f%%%n",
                        percentage
                );

                System.out.println("==============================================================");
            }
        }
    }

    private String shorten(String text, int maxLength) {

        if (text == null) {
            return "";
        }

        if (text.length() <= maxLength) {
            return text;
        }

        return text.substring(0, maxLength - 3) + "...";
    }
}