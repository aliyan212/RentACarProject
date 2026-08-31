package views;

import database.VehicleDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import models.Vehicle;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Optional;

public class VehicleView {

    public static Node getView() {
        VBox content = new VBox(16);
        content.getStyleClass().add("content");
        content.setPadding(new Insets(18));

        Label title = new Label("Fleet");
        title.getStyleClass().add("content-title");

        Label subtitle = new Label("Manage vehicles, purchase prices, and ownership shares");
        subtitle.getStyleClass().add("muted");

        ObservableList<Vehicle> data = FXCollections.observableArrayList();
        VehicleDAO dao = new VehicleDAO();
        try {
            data.addAll(dao.listVehicles());
        } catch (SQLException e) {
            ViewHelper.showError("Could not load vehicles", e);
        }

        TableView<Vehicle> table = new TableView<>(data);
        table.getStyleClass().add("data-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setMinHeight(280);
        table.setPrefHeight(420);

        TableColumn<Vehicle, Integer> idCol = new TableColumn<>("Car ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("carId"));
        idCol.setMinWidth(70);

        TableColumn<Vehicle, String> modelCol = new TableColumn<>("Model");
        modelCol.setCellValueFactory(new PropertyValueFactory<>("model"));
        modelCol.setMinWidth(140);

        TableColumn<Vehicle, String> priceCol = new TableColumn<>("Price (PKR)");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("purchasePrice"));
        priceCol.setMinWidth(120);

        TableColumn<Vehicle, Double> ownerCol = new TableColumn<>("Ownership %");
        ownerCol.setCellValueFactory(new PropertyValueFactory<>("ownershipPercentage"));
        ownerCol.setMinWidth(110);
        ownerCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText((empty || v == null) ? null : String.format(Locale.US, "%.0f%%", v));
            }
        });
        table.getColumns().add(idCol);
        table.getColumns().add(modelCol);
        table.getColumns().add(priceCol);
        table.getColumns().add(ownerCol);

        Button addBtn = new Button("+ Add Vehicle");
        addBtn.getStyleClass().add("btn-primary");
        Button deleteBtn = new Button("🗑 Delete");
        deleteBtn.getStyleClass().add("btn-danger");
        FlowPane toolbar = new FlowPane(10, 10, addBtn, deleteBtn);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        addBtn.setOnAction(e -> showAddDialog().ifPresent(v -> {
            try {
                dao.insert(v.getModel(), v.getPurchasePrice(), v.getOwnershipPercentage());
                data.setAll(dao.listVehicles()); // reload to get auto-generated car_ID
            } catch (SQLException ex) {
                ViewHelper.showError("Could not add vehicle", ex);
            }
        }));

        deleteBtn.setOnAction(e -> {
            Vehicle sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                ViewHelper.showWarning("Select a row first.");
                return;
            }
            if (!ViewHelper.confirmDelete("Car ID " + sel.getCarId()))
                return;
            try {
                dao.delete(sel.getCarId());
                data.remove(sel);
            } catch (SQLException ex) {
                ViewHelper.showError("Could not delete", ex);
            }
        });

        StackPane card = new StackPane(table);
        card.getStyleClass().add("card");
        content.getChildren().addAll(title, subtitle, toolbar, card);
        return ViewHelper.createResponsiveScroll(content);
    }

    private static Optional<Vehicle> showAddDialog() {
        Dialog<Vehicle> dlg = new Dialog<>();
        dlg.setTitle("Add Vehicle");
        dlg.setHeaderText(null);
        ViewHelper.styleDialog(dlg.getDialogPane());

        TextField modelField = ViewHelper.field("e.g. Toyota Corolla 2024");
        TextField priceField = ViewHelper.field("e.g. 3500000");
        TextField pctField = ViewHelper.field("e.g. 100");

        dlg.getDialogPane().setContent(ViewHelper.form(
                "Model", modelField,
                "Price (PKR)", priceField,
                "Ownership %", pctField));

        ButtonType save = new ButtonType("Add Vehicle", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

        dlg.setResultConverter(btn -> {
            if (btn != save)
                return null;
            try {
                return new Vehicle(0, modelField.getText().trim(),
                        priceField.getText().trim(),
                        Double.parseDouble(pctField.getText().trim()));
            } catch (NumberFormatException ex) {
                ViewHelper.showWarning("Ownership % must be a number.");
                return null;
            }
        });
        return dlg.showAndWait();
    }
}
