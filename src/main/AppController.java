package main;

import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import views.*;

public class AppController {
    @FXML private BorderPane root;
    @FXML private VBox sidebar;
    @FXML private ToggleButton btnDash, btnVehicles, btnCustomers, btnDrivers, btnSales, btnPayments, btnExpenses;

    private ToggleGroup nav = new ToggleGroup();

    @FXML
    public void initialize() {
        btnDash.setToggleGroup(nav); btnVehicles.setToggleGroup(nav); btnCustomers.setToggleGroup(nav);
        btnDrivers.setToggleGroup(nav); btnSales.setToggleGroup(nav); btnPayments.setToggleGroup(nav); btnExpenses.setToggleGroup(nav);

        btnDash.setOnAction(e -> root.setCenter(DashboardView.getView()));
        btnVehicles.setOnAction(e -> root.setCenter(VehicleView.getView()));
        btnCustomers.setOnAction(e -> root.setCenter(CustomerView.getView()));
        btnDrivers.setOnAction(e -> root.setCenter(DriverView.getView()));
        btnSales.setOnAction(e -> root.setCenter(SalesView.getView()));
        btnPayments.setOnAction(e -> root.setCenter(PaymentView.getView()));
        btnExpenses.setOnAction(e -> root.setCenter(ExpenseView.getView()));

        btnDash.setSelected(true);
        root.setCenter(DashboardView.getView());
    }
}