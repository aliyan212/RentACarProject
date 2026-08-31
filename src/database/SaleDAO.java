package database;

import models.Sale;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SaleDAO {

    public List<Sale> listSales() throws SQLException {
        String sql = "SELECT sale_id, customer_cnic, car_id, driver_cnic,  start_date, end_date, rental_type, Total FROM Sales";

        try (
                Connection conn = DBConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            List<Sale> sales = new ArrayList<>();
            while (rs.next()) {
                Date start = rs.getDate("start_date");
                Date end = rs.getDate("end_date");

                sales.add(new Sale(
                        rs.getInt("sale_id"),
                        rs.getLong("customer_cnic"),
                        rs.getInt("car_id"),
                        rs.getLong("driver_cnic"),
                        start != null ? start.toLocalDate() : null,
                        end != null ? end.toLocalDate() : null,
                        rs.getString("rental_type"),
                        rs.getInt("Total")));
            }
            return sales;
        }
    }

    public void insert(long customerCnic, int carId, long driverCnic,
            LocalDate start, LocalDate end, String rentalType, int total) throws SQLException {
        String sql = "INSERT INTO Sales (customer_cnic, car_id, driver_cnic, start_date, end_date, rental_type, Total) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerCnic);
            ps.setInt(2, carId);
            if (driverCnic > 0)
                ps.setLong(3, driverCnic);
            else
                ps.setNull(3, java.sql.Types.BIGINT);
            ps.setDate(4, start != null ? Date.valueOf(start) : null);
            ps.setDate(5, end != null ? Date.valueOf(end) : null);
            ps.setString(6, rentalType);
            ps.setInt(7, total);
            ps.executeUpdate();
        }
    }

    public void delete(int saleId) throws SQLException {
        String sql = "DELETE FROM Sales WHERE sale_id = ?";
        try (Connection conn = DBConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, saleId);
            ps.executeUpdate();
        }
    }

    public List<Sale> listSalesWithBalance() throws SQLException {
        String sql = "SELECT s.sale_id, s.customer_cnic, s.car_id, s.driver_cnic, " +
                "       s.start_date, s.end_date, s.rental_type, s.Total, " +
                "       COALESCE(SUM(p.money_paid), 0) AS amount_paid " +
                "FROM Sales s " +
                "LEFT JOIN Payment p ON p.sale_id = s.sale_id " +
                "GROUP BY s.sale_id, s.customer_cnic, s.car_id, s.driver_cnic, " +
                "         s.start_date, s.end_date, s.rental_type, s.Total " +
                "ORDER BY s.sale_id DESC";

        try (Connection conn = DBConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            List<Sale> list = new ArrayList<>();
            while (rs.next()) {
                Date start = rs.getDate("start_date");
                Date end = rs.getDate("end_date");
                int total = rs.getInt("Total");
                double paid = rs.getDouble("amount_paid");

                Sale sale = new Sale(
                        rs.getInt("sale_id"),
                        rs.getLong("customer_cnic"),
                        rs.getInt("car_id"),
                        rs.getLong("driver_cnic"),
                        start != null ? start.toLocalDate() : null,
                        end != null ? end.toLocalDate() : null,
                        rs.getString("rental_type"),
                        total);
                sale.setAmountPaid(paid);
                sale.setBalance(total - paid);
                if (paid >= total)
                    sale.setPaymentStatus("Paid");
                else if (paid > 0)
                    sale.setPaymentStatus("Partial");
                else
                    sale.setPaymentStatus("Unpaid");
                list.add(sale);
            }
            return list;
        }
    }
}
