package kec;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StudentDAO {

    public boolean addStudent(String rollNo, String name,
                              String department, int semester) {

        String sql = "INSERT INTO students " +
                     "(roll_no, student_name, department, current_semester) " +
                     "VALUES (?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, rollNo);
            ps.setString(2, name);
            ps.setString(3, department);
            ps.setInt(4, semester);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Student registered successfully!");
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Error registering student: " + e.getMessage());
        }

        return false;
    }
}

