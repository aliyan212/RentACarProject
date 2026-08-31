package database;

import models.Driver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DriverDAO {

    public List<Driver> listDrivers() throws SQLException {
    String sql = "SELECT d.driver_cnic, d.driver_name, d.license_number, d.phone_number, " +
                " CASE WHEN active.driver_cnic IS NOT NULL THEN 'On Duty' " +
                " ELSE d.status END AS eff_status " +
                "FROM Drivers d " +
                "LEFT JOIN ( " + " SELECT DISTINCT driver_cnic FROM Sales " +
        " WHERE end_date >= date('now') AND driver_cnic > 0 " +
                ") active ON active.driver_cnic = d.driver_cnic";

        try (
                Connection conn = DBConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            List<Driver> drivers = new ArrayList<>();
            while (rs.next()) {
                drivers.add(new Driver(
                        rs.getLong("driver_cnic"),
                        rs.getString("driver_name"),
                        rs.getLong("license_number"),
                        rs.getLong("phone_number"),
                        rs.getString("eff_status")));
            }
            return drivers;
        }
    }

    public void insert(long cnic, String name, long license, long phone, String status) throws SQLException {
        String sql = "INSERT INTO Drivers (driver_cnic, driver_name, license_number, phone_number, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, cnic);
            ps.setString(2, name);
            ps.setLong(3, license);
            ps.setLong(4, phone);
            ps.setString(5, status);
            ps.executeUpdate();
        }
    }

    public void delete(long cnic) throws SQLException {
        String sql = "DELETE FROM Drivers WHERE driver_cnic = ?";
        try (Connection conn = DBConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, cnic);
            ps.executeUpdate();
        }
    }
}
