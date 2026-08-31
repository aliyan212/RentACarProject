package database;

import models.Payment;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {
    public List<Payment> listPayments() throws SQLException {
        String sql = "SELECT payment_id, sale_id, money_paid, payment_date FROM Payment";

        try (
                Connection conn = DBConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            List<Payment> payments = new ArrayList<>();
            while (rs.next()) {
                Date d = rs.getDate("payment_date");
                payments.add(new Payment(
                        rs.getInt("payment_id"),
                        rs.getInt("sale_id"),
                        rs.getDouble("money_paid"),
                        d != null ? d.toLocalDate() : null));
            }
            return payments;
        }
    }

    public void insert(int saleId, double moneyPaid, LocalDate date) throws SQLException {
        String sql = "INSERT INTO Payment (sale_id, money_paid, payment_date) VALUES (?, ?, ?)";
        try (Connection conn = DBConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, saleId);
            ps.setDouble(2, moneyPaid);
            ps.setDate(3, date != null ? Date.valueOf(date) : null);
            ps.executeUpdate();
        }
    }

    public void delete(int paymentId) throws SQLException {
        String sql = "DELETE FROM Payment WHERE payment_id = ?";
        try (Connection conn = DBConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paymentId);
            ps.executeUpdate();
        }
    }
}
