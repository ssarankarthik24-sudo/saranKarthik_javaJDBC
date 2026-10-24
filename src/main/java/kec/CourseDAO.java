package kec;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CourseDAO {

    public boolean addCourse(String code, String title, int credits,
                             String type) {

        String sql = "INSERT INTO courses " +
                     "(course_code, course_title, credits, course_type) " +
                     "VALUES (?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, code);
            ps.setString(2, title);
            ps.setInt(3, credits);
            ps.setString(4, type.toUpperCase());

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Course added successfully!");
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Error adding course: " + e.getMessage());
        }

        return false;
    }

    public void viewCourses() {

        String sql = "SELECT * FROM courses ORDER BY course_code";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n===== COURSE CATALOG =====");

            boolean found = false;

            while (rs.next()) {
                found = true;

                System.out.println("Course Code: " +
                                   rs.getString("course_code"));
                System.out.println("Course Title: " +
                                   rs.getString("course_title"));
                System.out.println("Credits: " + rs.getInt("credits"));
                System.out.println("Type: " + rs.getString("course_type"));
                System.out.println("-------------------------------");
            }

            if (!found) {
                System.out.println("No courses added yet.");
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving courses: " + e.getMessage());
        }
    }
}