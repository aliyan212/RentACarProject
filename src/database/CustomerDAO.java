package database;

import models.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    public List<Customer> listCustomers() throws SQLException {
        String sql = "SELECT customer_cnic, custome_name, address, phone_number, license_number FROM Customers";

        try (
                Connection conn = DBConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            List<Customer> customers = new ArrayList<>();
            while (rs.next()) {
                customers.add(new Customer(
                        rs.getLong("customer_cnic"),
                        rs.getString("custome_name"),
                        rs.getString("address"),
                        rs.getLong("phone_number"),
                        rs.getString("license_number")));
            }
            return customers;
        }
    }

    public void insert(long cnic, String name, String address, long phone, String license) throws SQLException {
        String sql = "INSERT INTO Customers (customer_cnic, custome_name, address, phone_number, license_number) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, cnic);
            ps.setString(2, name);
            ps.setString(3, address);
            ps.setLong(4, phone);
            ps.setString(5, license);
            ps.executeUpdate();
        }
    }

    public void delete(long cnic) throws SQLException {
        String sql = "DELETE FROM Customers WHERE customer_cnic = ?";
        try (Connection conn = DBConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, cnic);
            ps.executeUpdate();
        }
    }
}
