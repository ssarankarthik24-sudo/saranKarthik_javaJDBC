
package kec;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AcademicDAO {

    public void viewStudents() {

        String sql = "SELECT * FROM students";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n===== REGISTERED STUDENTS =====");

            boolean found = false;

            while (rs.next()) {
                found = true;

                System.out.println("Roll Number: " + rs.getString("roll_no"));
                System.out.println("Name: " + rs.getString("student_name"));
                System.out.println("Department: " + rs.getString("department"));
                System.out.println("Semester: " + rs.getInt("current_semester"));
                System.out.println("-------------------------------");
            }

            if (!found) {
                System.out.println("No students registered yet.");
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving students: " + e.getMessage());
        }
    }
}