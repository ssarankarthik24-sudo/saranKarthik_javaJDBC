package kec;

import java.util.Scanner;

public class App {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        StudentDAO studentDAO = new StudentDAO();
        AcademicDAO academicDAO = new AcademicDAO();
        CourseDAO courseDAO = new CourseDAO();
        EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
        AttendanceDAO attendanceDAO = new AttendanceDAO();
        AssessmentDAO assessmentDAO = new AssessmentDAO();
        ResultDAO resultDAO = new ResultDAO();
        SGPADao sgpaDAO = new SGPADao();

        while (true) {

            System.out.println("\n====================================");
            System.out.println("        KEC ACADEMIC SYSTEM");
            System.out.println("====================================");
            System.out.println("1. Register Student");
            System.out.println("2. View Registered Students");
            System.out.println("3. Add Course");
            System.out.println("4. View Courses");
            System.out.println("5. Enroll Student in Course");
            System.out.println("6. View Enrollments");
            System.out.println("7. Record Attendance");
            System.out.println("8. View Attendance Report");
            System.out.println("9. Record Assessment Marks");
            System.out.println("10. Calculate Student Result");
            System.out.println("11. Calculate SGPA");
            System.out.println("12. Exit");
            System.out.println("====================================");
            System.out.print("Enter your choice (1-12): ");

            if (!sc.hasNextInt()) {
                System.out.println("Invalid input! Enter a number.");
                sc.nextLine();
                continue;
            }

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {

                case 1:
                    System.out.print("Enter Roll Number: ");
                    String rollNo = sc.nextLine();

                    System.out.print("Enter Student Name: ");
                    String studentName = sc.nextLine();

                    System.out.print("Enter Department: ");
                    String department = sc.nextLine();

                    System.out.print("Enter Current Semester: ");
                    if (!sc.hasNextInt()) {
                        System.out.println("Invalid semester!");
                        sc.nextLine();
                        break;
                    }
                    int semester = sc.nextInt();
                    sc.nextLine();

                    studentDAO.addStudent(
                        rollNo, studentName, department, semester
                    );
                    break;

                case 2:
                    academicDAO.viewStudents();
                    break;

                case 3:
                    System.out.print("Enter Course Code: ");
                    String courseCode = sc.nextLine();

                    System.out.print("Enter Course Title: ");
                    String courseTitle = sc.nextLine();

                    System.out.print("Enter Credits: ");
                    if (!sc.hasNextInt()) {
                        System.out.println("Invalid credits!");
                        sc.nextLine();
                        break;
                    }
                    int credits = sc.nextInt();
                    sc.nextLine();

                    System.out.print(
                        "Enter Course Type (THEORY/PRACTICAL/EMBEDDED): "
                    );
                    String courseType = sc.nextLine().trim().toUpperCase();

                    courseDAO.addCourse(
                        courseCode, courseTitle, credits, courseType
                    );
                    break;

                case 4:
                    courseDAO.viewCourses();
                    break;

                case 5:
                    System.out.print("Enter Student Roll Number: ");
                    String enrollRollNo = sc.nextLine();

                    System.out.print("Enter Course Code: ");
                    String enrollCourseCode = sc.nextLine();

                    System.out.print("Enter Semester: ");
                    if (!sc.hasNextInt()) {
                        System.out.println("Invalid semester!");
                        sc.nextLine();
                        break;
                    }
                    int enrollSemester = sc.nextInt();
                    sc.nextLine();

                    enrollmentDAO.enrollStudent(
                        enrollRollNo, enrollCourseCode, enrollSemester
                    );
                    break;

                case 6:
                    enrollmentDAO.viewEnrollments();
                    break;

                case 7:
                    System.out.print("Enter Student Roll Number: ");
                    String attendanceRollNo = sc.nextLine();

                    System.out.print("Enter Course Code: ");
                    String attendanceCourseCode = sc.nextLine();

                    System.out.print("Enter Semester: ");
                    if (!sc.hasNextInt()) {
                        System.out.println("Invalid semester!");
                        sc.nextLine();
                        break;
                    }
                    int attendanceSemester = sc.nextInt();

                    System.out.print("Enter Classes Held: ");
                    if (!sc.hasNextInt()) {
                        System.out.println("Invalid number of classes!");
                        sc.nextLine();
                        break;
                    }
                    int classesHeld = sc.nextInt();

                    System.out.print("Enter Classes Attended: ");
                    if (!sc.hasNextInt()) {
                        System.out.println("Invalid number of classes!");
                        sc.nextLine();
                        break;
                    }
                    int classesAttended = sc.nextInt();
                    sc.nextLine();

                    System.out.print(
                        "Medical Condonation Approved? (Y/N): "
                    );
                    String medicalInput = sc.nextLine().trim();
                    boolean medicalCondonation =
                        medicalInput.equalsIgnoreCase("Y");

                    attendanceDAO.recordAttendance(
                        attendanceRollNo,
                        attendanceCourseCode,
                        attendanceSemester,
                        classesHeld,
                        classesAttended,
                        medicalCondonation
                    );
                    break;

                case 8:
                    attendanceDAO.viewAllAttendance();
                    break;

                case 9:
                    assessmentDAO.recordAssessmentMarks();
                    break;

                case 10:
                    resultDAO.calculateStudentResult();
                    break;

                case 11:
                    sgpaDAO.calculateSGPA();
                    break;

                case 12:
                    System.out.println(
                        "Thank you for using KEC Academic System!"
                    );
                    sc.close();
                    return;

                default:
                    System.out.println(
                        "Invalid choice! Enter a number from 1 to 12."
                    );
            }
        }
    }
}
