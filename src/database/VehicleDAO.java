package database;

import models.Vehicle;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VehicleDAO {

    public List<Vehicle> listVehicles() throws SQLException {
        String sql = "SELECT car_ID, model, purchase_Price, ownership_Percentage FROM Vehicles";

        try (
                Connection conn = DBConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            List<Vehicle> vehicles = new ArrayList<>();

            while (rs.next()) {
                vehicles.add(new Vehicle(
                        rs.getInt("car_ID"),
                        rs.getString("model"),
                        rs.getString("purchase_Price"),
                        rs.getDouble("ownership_Percentage")));
            }

            return vehicles;
        }
    }

    public void insert(String model, String purchasePrice, double ownershipPercentage) throws SQLException {
        String sql = "INSERT INTO Vehicles (model, purchase_Price, ownership_Percentage) VALUES (?, ?, ?)";
        try (Connection conn = DBConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, model);
            ps.setString(2, purchasePrice);
            ps.setDouble(3, ownershipPercentage);
            ps.executeUpdate();
        }
    }

    public void delete(int carId) throws SQLException {
        try (Connection conn = DBConfig.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psCheck = conn.prepareStatement(
                    "SELECT COUNT(*) FROM Sales WHERE car_id = ?")) {
                psCheck.setInt(1, carId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        conn.rollback();
                        throw new SQLException("Cannot delete vehicle: there are sales referencing this vehicle. Remove or reassign those sales first.");
                    }
                }
            }

            try (PreparedStatement psClear = conn.prepareStatement(
                    "UPDATE Expenses SET vehicle_id = NULL WHERE vehicle_id = ?")) {
                psClear.setInt(1, carId);
                psClear.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM Vehicles WHERE car_ID = ?")) {
                ps.setInt(1, carId);
                ps.executeUpdate();
            }

            conn.commit();
        }
    }
}
