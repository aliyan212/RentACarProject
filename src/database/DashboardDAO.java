package database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DashboardDAO {

    public enum Period {
        OVERALL,
        CURRENT_MONTH
    }

    public static class ExpenseCategoryTotal {
        public String category;
        public long businessAmount;
        public long customerAmount;
    }

    public static class Stats {
        public int totalVehicles;
        public int totalCustomers;
        public int totalDrivers;
        public int totalSales;
        public int activeSales;
        public int overdueRentals;
        public long totalBilled;
        public long totalReceived;
        public long totalExpenses;
        public long businessExpenses;
        public long customerExpenses;
        public long netProfit;
        public long outstanding;

        public int completedRentals;
        public int paymentTransactions;
        public int expenseEntries;
        public long avgSaleValue;
    }

    public static class VehicleEarning {
        public int carId;
        public String model;
        public double ownershipPercentage;
        public long totalBilled;
        public long totalReceived;
        public long totalExpenses;
        public long netProfit;
    }

    public Stats getStats() throws SQLException {
        return getStats(Period.OVERALL);
    }

    public Stats getStats(Period period) throws SQLException {
        Stats s = new Stats();

        String monthStartExpr = "date('now', 'start of month')";
        String monthEndExpr = "date('now', 'start of month', '+1 month')";

        String salePeriodWhere = period == Period.CURRENT_MONTH
                ? (" WHERE start_date >= " + monthStartExpr + " AND start_date < " + monthEndExpr + " ")
                : "";
        String paymentPeriodWhere = period == Period.CURRENT_MONTH
                ? (" WHERE payment_date >= " + monthStartExpr + " AND payment_date < " + monthEndExpr + " ")
                : "";
        String expensePeriodWhere = period == Period.CURRENT_MONTH
                ? (" WHERE date >= " + monthStartExpr + " AND date < " + monthEndExpr + " ")
                : "";

        try (Connection conn = DBConfig.getConnection();
                Statement st = conn.createStatement()) {

            ResultSet rs;

            rs = st.executeQuery("SELECT COUNT(*) FROM Vehicles");
            if (rs.next())
                s.totalVehicles = rs.getInt(1);

            rs = st.executeQuery("SELECT COUNT(*) FROM Customers");
            if (rs.next())
                s.totalCustomers = rs.getInt(1);

            rs = st.executeQuery("SELECT COUNT(*) FROM Drivers");
            if (rs.next())
                s.totalDrivers = rs.getInt(1);

            rs = st.executeQuery("SELECT COUNT(*) FROM Sales" + salePeriodWhere);
            if (rs.next())
                s.totalSales = rs.getInt(1);

            rs = st.executeQuery(
                    "SELECT COUNT(*) FROM Sales s " +
                        "WHERE s.start_date <= date('now') AND s.end_date >= date('now')");
            if (rs.next())
                s.activeSales = rs.getInt(1);

            rs = st.executeQuery(
                    "SELECT COUNT(*) FROM Sales s " +
                        "WHERE s.end_date < date('now') " +
                            "  AND s.Total > COALESCE((SELECT SUM(p.money_paid) FROM Payment p WHERE p.sale_id = s.sale_id), 0)");
            if (rs.next())
                s.overdueRentals = rs.getInt(1);

            rs = st.executeQuery("SELECT COALESCE(SUM(Total), 0) FROM Sales" + salePeriodWhere);
            if (rs.next())
                s.totalBilled = rs.getLong(1);

            rs = st.executeQuery("SELECT COALESCE(SUM(money_paid), 0) FROM Payment" + paymentPeriodWhere);
            if (rs.next())
                s.totalReceived = rs.getLong(1);

            rs = st.executeQuery("SELECT COALESCE(SUM(amount), 0) FROM Expenses" + expensePeriodWhere);
            if (rs.next())
                s.totalExpenses = rs.getLong(1);

            String baseExpenseSplitWhere = period == Period.CURRENT_MONTH
                    ? (" AND date >= " + monthStartExpr + " AND date < " + monthEndExpr + " ")
                    : "";

            rs = st.executeQuery(
                    "SELECT COALESCE(SUM(amount), 0) FROM Expenses " +
                            "WHERE LOWER(COALESCE(payer, '')) = 'customer'" + baseExpenseSplitWhere);
            if (rs.next())
                s.customerExpenses = rs.getLong(1);

            rs = st.executeQuery(
                    "SELECT COALESCE(SUM(amount), 0) FROM Expenses " +
                            "WHERE LOWER(COALESCE(payer, '')) <> 'customer'" + baseExpenseSplitWhere);
            if (rs.next())
                s.businessExpenses = rs.getLong(1);

            s.netProfit = s.totalReceived - s.businessExpenses;

            // Outstanding balance: sum of remaining unpaid amounts on sales
            String outstandingWhere = period == Period.CURRENT_MONTH
                    ? (" WHERE (s.Total - COALESCE(p.paid, 0)) > 0 AND s.start_date >= " + monthStartExpr + " AND s.start_date < " + monthEndExpr)
                    : " WHERE (s.Total - COALESCE(p.paid, 0)) > 0";
            rs = st.executeQuery(
                    "SELECT COALESCE(SUM(s.Total - COALESCE(p.paid, 0)), 0) " +
                    "FROM Sales s " +
                    "LEFT JOIN (SELECT sale_id, SUM(money_paid) AS paid FROM Payment GROUP BY sale_id) p ON s.sale_id = p.sale_id" +
                    outstandingWhere);
            if (rs.next())
                s.outstanding = rs.getLong(1);

            if (period == Period.CURRENT_MONTH) {
                rs = st.executeQuery(
                        "SELECT COUNT(*) FROM Sales WHERE end_date >= " + monthStartExpr +
                                " AND end_date < " + monthEndExpr);
                if (rs.next())
                    s.completedRentals = rs.getInt(1);

                rs = st.executeQuery("SELECT COUNT(*) FROM Payment" + paymentPeriodWhere);
                if (rs.next())
                    s.paymentTransactions = rs.getInt(1);

                rs = st.executeQuery("SELECT COUNT(*) FROM Expenses" + expensePeriodWhere);
                if (rs.next())
                    s.expenseEntries = rs.getInt(1);

                rs = st.executeQuery("SELECT COALESCE(AVG(Total), 0) FROM Sales" + salePeriodWhere);
                if (rs.next())
                    s.avgSaleValue = rs.getLong(1);
            } else {
                // OVERALL equivalents
                rs = st.executeQuery("SELECT COUNT(*) FROM Sales WHERE end_date < date('now')");
                if (rs.next())
                    s.completedRentals = rs.getInt(1);

                rs = st.executeQuery("SELECT COUNT(*) FROM Payment");
                if (rs.next())
                    s.paymentTransactions = rs.getInt(1);

                rs = st.executeQuery("SELECT COUNT(*) FROM Expenses");
                if (rs.next())
                    s.expenseEntries = rs.getInt(1);

                rs = st.executeQuery("SELECT COALESCE(AVG(Total), 0) FROM Sales");
                if (rs.next())
                    s.avgSaleValue = rs.getLong(1);
            }
        }

        return s;
    }

    public java.util.List<VehicleEarning> getVehicleEarnings() throws SQLException {
        return getVehicleEarnings(Period.OVERALL);
    }

    public java.util.List<VehicleEarning> getVehicleEarnings(Period period) throws SQLException {
        String monthStartExpr = "date('now', 'start of month')";
        String monthEndExpr = "date('now', 'start of month', '+1 month')";

        String salesWhere = period == Period.CURRENT_MONTH
                ? (" WHERE start_date >= " + monthStartExpr + " AND start_date < " + monthEndExpr + " ")
                : "";
        String paymentWhere = period == Period.CURRENT_MONTH
                ? (" WHERE p.payment_date >= " + monthStartExpr + " AND p.payment_date < " + monthEndExpr + " ")
                : "";
        String expenseWhere = period == Period.CURRENT_MONTH
                ? (" WHERE date >= " + monthStartExpr + " AND date < " + monthEndExpr + " ")
                : "";

        String sql = "SELECT v.car_ID, v.model, COALESCE(v.ownership_Percentage, 100.0) AS ownership, " +
                "  COALESCE(billed.total, 0)    AS total_billed, " +
                "  COALESCE(received.total, 0)  AS total_received, " +
                "  COALESCE(expenses.total, 0)  AS total_expenses " +
                "FROM Vehicles v " +
                "LEFT JOIN ( " +
                "    SELECT car_id, SUM(Total) AS total FROM Sales" + salesWhere + " GROUP BY car_id " +
                ") billed ON billed.car_id = v.car_ID " +
                "LEFT JOIN ( " +
                "    SELECT s.car_id, SUM(p.money_paid) AS total " +
                "    FROM Payment p JOIN Sales s ON p.sale_id = s.sale_id " +
                (period == Period.CURRENT_MONTH
                        ? ("    " + paymentWhere + " GROUP BY s.car_id ")
                        : "    GROUP BY s.car_id ")
                +
                ") received ON received.car_id = v.car_ID " +
                "LEFT JOIN ( " +
                "    SELECT vehicle_id, SUM(CASE WHEN LOWER(COALESCE(payer, '')) <> 'customer' THEN amount ELSE 0 END) AS total "
                +
                "    FROM Expenses" + expenseWhere + (expenseWhere.isEmpty() ? " WHERE " : " AND ") +
                " vehicle_id IS NOT NULL AND vehicle_id > 0 GROUP BY vehicle_id " +
                ") expenses ON expenses.vehicle_id = v.car_ID " +
                "ORDER BY total_billed DESC";

        try (Connection conn = DBConfig.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {

            java.util.List<VehicleEarning> list = new java.util.ArrayList<>();
            while (rs.next()) {
                VehicleEarning ve = new VehicleEarning();
                ve.carId = rs.getInt("car_ID");
                ve.model = rs.getString("model");
                ve.ownershipPercentage = rs.getDouble("ownership");
                ve.totalBilled = rs.getLong("total_billed");
                ve.totalReceived = rs.getLong("total_received");
                ve.totalExpenses = rs.getLong("total_expenses");
                double margin = ve.totalReceived - ve.totalExpenses;
                if (ve.ownershipPercentage > 0 && ve.ownershipPercentage <= 100.0) {
                    ve.netProfit = Math.round(margin * (ve.ownershipPercentage / 100.0));
                } else {
                    ve.netProfit = (long) margin;
                }
                list.add(ve);
            }
            return list;
        }
    }

    public java.util.List<ExpenseCategoryTotal> getExpenseCategoryTotals(Period period) throws SQLException {
        String monthStartExpr = "date('now', 'start of month')";
        String monthEndExpr = "date('now', 'start of month', '+1 month')";
        String expenseWhere = period == Period.CURRENT_MONTH
                ? (" WHERE date >= " + monthStartExpr + " AND date < " + monthEndExpr + " ")
                : "";

        String sql = "SELECT cat, " +
                "  COALESCE(SUM(CASE WHEN LOWER(COALESCE(payer,'')) <> 'customer' THEN amount ELSE 0 END), 0) AS business_amount, "
                +
                "  COALESCE(SUM(CASE WHEN LOWER(COALESCE(payer,''))  = 'customer' THEN amount ELSE 0 END), 0) AS customer_amount "
                +
                "FROM ( " +
                "  SELECT amount, payer, " +
                "    CASE " +
                "      WHEN LOWER(COALESCE(type,'')) LIKE 'fuel%' THEN 'Fuel' " +
                "      WHEN LOWER(COALESCE(type,'')) LIKE 'repair%' THEN 'Repair' " +
                "      WHEN LOWER(COALESCE(type,'')) LIKE 'insurance%' THEN 'Insurance' " +
                "      ELSE 'Other' " +
                "    END AS cat " +
                "  FROM Expenses" + expenseWhere +
                ") x " +
                "GROUP BY cat";

        try (Connection conn = DBConfig.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {

            java.util.Map<String, ExpenseCategoryTotal> map = new java.util.HashMap<>();
            while (rs.next()) {
                ExpenseCategoryTotal t = new ExpenseCategoryTotal();
                t.category = rs.getString("cat");
                t.businessAmount = rs.getLong("business_amount");
                t.customerAmount = rs.getLong("customer_amount");
                map.put(t.category, t);
            }

            java.util.List<ExpenseCategoryTotal> out = new java.util.ArrayList<>();
            for (String cat : java.util.List.of("Fuel", "Repair", "Insurance", "Other")) {
                ExpenseCategoryTotal t = map.get(cat);
                if (t == null) {
                    t = new ExpenseCategoryTotal();
                    t.category = cat;
                    t.businessAmount = 0;
                    t.customerAmount = 0;
                }
                out.add(t);
            }
            return out;
        }
    }
}
