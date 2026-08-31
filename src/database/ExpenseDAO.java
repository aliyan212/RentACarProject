package database;

import models.Expense;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExpenseDAO {
    public List<Expense> listExpenses() throws SQLException {
        String sql = "SELECT expense_id, type, payer, sale_id, vehicle_id, amount, date FROM Expenses";

        try (
                Connection conn = DBConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            List<Expense> expenses = new ArrayList<>();
            while (rs.next()) {
                Date d = rs.getDate("date");
                expenses.add(new Expense(
                        rs.getInt("expense_id"),
                        rs.getString("type"),
                        rs.getString("payer"),
                        rs.getInt("sale_id"),
                        rs.getInt("vehicle_id"),
                        rs.getDouble("amount"),
                        d != null ? d.toLocalDate() : null));
            }
            return expenses;
        }
    }

    public void insert(String type, String payer, int saleId, int vehicleId, double amount, LocalDate date)
            throws SQLException {
        String sql = "INSERT INTO Expenses (type, payer, sale_id, vehicle_id, amount, date) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type);
            ps.setString(2, payer);
            if (saleId > 0)
                ps.setInt(3, saleId);
            else
                ps.setNull(3, java.sql.Types.INTEGER);
            if (vehicleId > 0)
                ps.setInt(4, vehicleId);
            else
                ps.setNull(4, java.sql.Types.INTEGER);
            ps.setDouble(5, amount);
            ps.setDate(6, date != null ? Date.valueOf(date) : null);
            ps.executeUpdate();
        }
    }

    public void delete(int expenseId) throws SQLException {
        String sql = "DELETE FROM Expenses WHERE expense_id = ?";
        try (Connection conn = DBConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, expenseId);
            ps.executeUpdate();
        }
    }
}
