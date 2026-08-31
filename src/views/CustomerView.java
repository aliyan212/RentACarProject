package views;

import database.CustomerDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import models.Customer;
import main.InputValidator;
import java.sql.SQLException;
import java.util.Optional;

public class CustomerView {

    public static Node getView() {
        VBox content = new VBox(16);
        content.getStyleClass().add("content");
        content.setPadding(new Insets(18));

        Label title = new Label("Customers");
        title.getStyleClass().add("content-title");

        Label subtitle = new Label("Customer records, identity verification, and contact details");
        subtitle.getStyleClass().add("muted");

        ObservableList<Customer> data = FXCollections.observableArrayList();
        CustomerDAO dao = new CustomerDAO();
        try {
            data.addAll(dao.listCustomers());
        } catch (SQLException e) {
            ViewHelper.showError("Could not load customers", e);
        }

        FilteredList<Customer> filteredData = new FilteredList<>(data, p -> true);
        SortedList<Customer> sortedData = new SortedList<>(filteredData);

        TableView<Customer> table = new TableView<>(sortedData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.getStyleClass().add("data-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setMinHeight(280);
        table.setPrefHeight(420);

        TableColumn<Customer, Long> cnicCol = new TableColumn<>("CNIC");
        cnicCol.setCellValueFactory(new PropertyValueFactory<>("cnic"));
        cnicCol.setMinWidth(120);

        TableColumn<Customer, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setMinWidth(130);

        TableColumn<Customer, Long> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setMinWidth(110);

        TableColumn<Customer, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        addressCol.setMinWidth(120);

        TableColumn<Customer, String> licenseCol = new TableColumn<>("License #");
        licenseCol.setCellValueFactory(new PropertyValueFactory<>("license"));
        licenseCol.setMinWidth(100);

        table.getColumns().add(cnicCol);
        table.getColumns().add(nameCol);
        table.getColumns().add(phoneCol);
        table.getColumns().add(addressCol);
        table.getColumns().add(licenseCol);

        TextField searchField = ViewHelper.field("🔍 Search customer by name, CNIC, phone...");
        searchField.setPrefWidth(260);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(c -> {
                if (newVal == null || newVal.isBlank()) return true;
                String lower = newVal.toLowerCase().trim();
                return (c.getName() != null && c.getName().toLowerCase().contains(lower))
                        || String.valueOf(c.getCnic()).contains(lower)
                        || String.valueOf(c.getPhone()).contains(lower)
                        || (c.getAddress() != null && c.getAddress().toLowerCase().contains(lower))
                        || (c.getLicense() != null && c.getLicense().toLowerCase().contains(lower));
            });
        });

        Button addBtn = new Button("+ Add Customer");
        addBtn.getStyleClass().add("btn-primary");
        Button deleteBtn = new Button("🗑 Delete");
        deleteBtn.getStyleClass().add("btn-danger");
        FlowPane toolbar = new FlowPane(10, 10, searchField, addBtn, deleteBtn);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        addBtn.setOnAction(e -> showAddDialog().ifPresent(c -> {
            try {
                dao.insert(c.getCnic(), c.getName(), c.getAddress(), c.getPhone(), c.getLicense());
                data.setAll(dao.listCustomers());
            } catch (SQLException ex) {
                ViewHelper.showError("Could not add customer", ex);
            }
        }));

        deleteBtn.setOnAction(e -> {
            Customer sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                ViewHelper.showWarning("Select a row first.");
                return;
            }
            if (!ViewHelper.confirmDelete(sel.getName()))
                return;
            try {
                dao.delete(sel.getCnic());
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

    private static Optional<Customer> showAddDialog() {
        Dialog<Customer> dlg = new Dialog<>();
        dlg.setTitle("Add Customer");
        dlg.setHeaderText(null);
        ViewHelper.styleDialog(dlg.getDialogPane());

        TextField cnicField = ViewHelper.field("e.g. 3520112345671");
        TextField nameField = ViewHelper.field("Full name");
        TextField phoneField = ViewHelper.field("e.g. 3001234567");
        TextField addressField = ViewHelper.field("City / address");
        TextField licField = ViewHelper.field("License number");

        dlg.getDialogPane().setContent(ViewHelper.form(
                "CNIC", cnicField,
                "Name", nameField,
                "Phone", phoneField,
                "Address", addressField,
                "License #", licField));

        ButtonType save = new ButtonType("Add Customer", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

        dlg.setResultConverter(btn -> {
            if (btn != save)
                return null;
            if (!InputValidator.isNotEmpty(cnicField, "CNIC") ||
                    !InputValidator.isNotEmpty(nameField, "Name") ||
                    !InputValidator.isNumeric(cnicField, "CNIC") ||
                    !InputValidator.isNumeric(phoneField, "Phone")) {
                return null;
            }
            try {
                return new Customer(
                        Long.parseLong(cnicField.getText().trim()),
                        nameField.getText().trim(),
                        addressField.getText().trim(),
                        Long.parseLong(phoneField.getText().trim()),
                        licField.getText().trim());
            } catch (NumberFormatException ex) {
                ViewHelper.showWarning("CNIC and Phone must be numbers.");
                return null;
            }
        });
        return dlg.showAndWait();
    }
}
