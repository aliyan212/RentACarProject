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
        insertWithInitialPayment(customerCnic, carId, driverCnic, start, end, rentalType, total, 0.0);
    }

    public void insertWithInitialPayment(long customerCnic, int carId, long driverCnic,
            LocalDate start, LocalDate end, String rentalType, int total, double initialPayment) throws SQLException {
        String sqlSale = "INSERT INTO Sales (customer_cnic, car_id, driver_cnic, start_date, end_date, rental_type, Total) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlPayment = "INSERT INTO Payment (sale_id, money_paid, payment_date) VALUES (?, ?, ?)";

        try (Connection conn = DBConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int saleId;
                try (PreparedStatement psSale = conn.prepareStatement(sqlSale, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                    psSale.setLong(1, customerCnic);
                    psSale.setInt(2, carId);
                    if (driverCnic > 0)
                        psSale.setLong(3, driverCnic);
                    else
                        psSale.setNull(3, java.sql.Types.BIGINT);
                    psSale.setDate(4, start != null ? Date.valueOf(start) : null);
                    psSale.setDate(5, end != null ? Date.valueOf(end) : null);
                    psSale.setString(6, rentalType);
                    psSale.setInt(7, total);
                    psSale.executeUpdate();

                    try (ResultSet rs = psSale.getGeneratedKeys()) {
                        if (rs.next()) {
                            saleId = rs.getInt(1);
                        } else {
                            throw new SQLException("Failed to retrieve generated sale_id");
                        }
                    }
                }

                if (initialPayment > 0) {
                    try (PreparedStatement psPay = conn.prepareStatement(sqlPayment)) {
                        psPay.setInt(1, saleId);
                        psPay.setDouble(2, initialPayment);
                        psPay.setDate(3, Date.valueOf(LocalDate.now()));
                        psPay.executeUpdate();
                    }
                }

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public boolean isVehicleAvailable(int carId, LocalDate start, LocalDate end, int excludeSaleId) throws SQLException {
        if (start == null || end == null) return true;
        String sql = "SELECT COUNT(*) FROM Sales " +
                     "WHERE car_id = ? AND sale_id <> ? " +
                     "  AND start_date <= ? AND end_date >= ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, carId);
            ps.setInt(2, excludeSaleId);
            ps.setDate(3, Date.valueOf(end));
            ps.setDate(4, Date.valueOf(start));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) == 0;
            }
        }
    }

    public boolean isDriverAvailable(long driverCnic, LocalDate start, LocalDate end, int excludeSaleId) throws SQLException {
        if (driverCnic <= 0 || start == null || end == null) return true;
        String sql = "SELECT COUNT(*) FROM Sales " +
                     "WHERE driver_cnic = ? AND sale_id <> ? " +
                     "  AND start_date <= ? AND end_date >= ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, driverCnic);
            ps.setInt(2, excludeSaleId);
            ps.setDate(3, Date.valueOf(end));
            ps.setDate(4, Date.valueOf(start));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) == 0;
            }
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
