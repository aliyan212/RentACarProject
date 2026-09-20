package views;

import database.DriverDAO;
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
import models.Driver;
import java.sql.SQLException;
import java.util.Optional;

public class DriverView {

    public static Node getView() {
        VBox content = new VBox(16);
        content.getStyleClass().add("content");
        content.setPadding(new Insets(18));

        Label title = new Label("Drivers");
        title.getStyleClass().add("content-title");

        Label subtitle = new Label("Manage driver roster, licensing, and duty availability");
        subtitle.getStyleClass().add("muted");

        ObservableList<Driver> data = FXCollections.observableArrayList();
        DriverDAO dao = new DriverDAO();
        try {
            data.addAll(dao.listDrivers());
        } catch (SQLException e) {
            ViewHelper.showError("Could not load drivers", e);
        }

        FilteredList<Driver> filteredData = new FilteredList<>(data, p -> true);
        SortedList<Driver> sortedData = new SortedList<>(filteredData);

        TableView<Driver> table = new TableView<>(sortedData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.getStyleClass().add("data-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setMinHeight(260);
        table.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Driver, Long> cnicCol = new TableColumn<>("CNIC");
        cnicCol.setCellValueFactory(new PropertyValueFactory<>("cnic"));
        cnicCol.setMinWidth(120);

        TableColumn<Driver, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setMinWidth(130);

        TableColumn<Driver, Long> licCol = new TableColumn<>("License #");
        licCol.setCellValueFactory(new PropertyValueFactory<>("license"));
        licCol.setMinWidth(110);

        TableColumn<Driver, Long> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setMinWidth(110);

        TableColumn<Driver, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setMinWidth(110);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(v);
                setStyle(switch (v) {
                    case "On Duty" -> "-fx-text-fill: #ffd43b; -fx-font-weight:700;";
                    case "Available" -> "-fx-text-fill: #69db7c; -fx-font-weight:700;";
                    default -> "-fx-text-fill: #ff6b6b; -fx-font-weight:700;";
                });
            }
        });
        table.getColumns().add(cnicCol);
        table.getColumns().add(nameCol);
        table.getColumns().add(licCol);
        table.getColumns().add(phoneCol);
        table.getColumns().add(statusCol);

        TextField searchField = ViewHelper.field("🔍 Search driver by name, CNIC, status...");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(d -> {
                if (newVal == null || newVal.isBlank())
                    return true;
                String lower = newVal.toLowerCase().trim();
                return (d.getName() != null && d.getName().toLowerCase().contains(lower))
                        || String.valueOf(d.getCnic()).contains(lower)
                        || String.valueOf(d.getLicense()).contains(lower)
                        || String.valueOf(d.getPhone()).contains(lower)
                        || (d.getStatus() != null && d.getStatus().toLowerCase().contains(lower));
            });
        });

        Button addBtn = new Button("+ Add Driver");
        addBtn.getStyleClass().add("btn-primary");
        Button deleteBtn = new Button("🗑 Delete");
        deleteBtn.getStyleClass().add("btn-danger");
        Node toolbar = ViewHelper.createResponsiveToolbar(searchField, addBtn, deleteBtn);

        addBtn.setOnAction(e -> showAddAndSaveDialog().ifPresent(d -> {
            try {
                data.setAll(dao.listDrivers());
            } catch (SQLException ex) {
                ViewHelper.showError("Could not refresh drivers", ex);
            }
        }));

        deleteBtn.setOnAction(e -> {
            Driver sel = table.getSelectionModel().getSelectedItem();
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
        VBox.setVgrow(card, Priority.ALWAYS);
        VBox.setVgrow(content, Priority.ALWAYS);
        content.getChildren().addAll(title, subtitle, toolbar, card);
        return ViewHelper.createResponsiveScroll(content);
    }

    public static Optional<Driver> showAddAndSaveDialog() {
        Optional<Driver> opt = showAddDialog();
        if (opt.isPresent()) {
            Driver d = opt.get();
            try {
                new DriverDAO().insert(d.getCnic(), d.getName(), d.getLicense(), d.getPhone(), d.getStatus());
                return Optional.of(d);
            } catch (SQLException ex) {
                ViewHelper.showError("Could not add driver", ex);
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    public static Optional<Driver> showAddDialog() {
        Dialog<Driver> dlg = new Dialog<>();
        dlg.setTitle("Add Driver");
        dlg.setHeaderText(null);
        ViewHelper.styleDialog(dlg.getDialogPane());

        TextField cnicField = ViewHelper.field("e.g. 3520112345671");
        TextField nameField = ViewHelper.field("Full name");
        TextField licField = ViewHelper.field("License number");
        TextField phoneField = ViewHelper.field("e.g. 3001234567");
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Available", "Off Duty");
        statusCombo.setValue("Available");
        statusCombo.setMaxWidth(Double.MAX_VALUE);

        dlg.getDialogPane().setContent(ViewHelper.form(
                "CNIC", cnicField,
                "Name", nameField,
                "License #", licField,
                "Phone", phoneField,
                "Status", statusCombo));

        ButtonType save = new ButtonType("Add Driver", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

        dlg.setResultConverter(btn -> {
            if (btn != save)
                return null;
            try {
                return new Driver(
                        Long.parseLong(cnicField.getText().trim()),
                        nameField.getText().trim(),
                        Long.parseLong(licField.getText().trim()),
                        Long.parseLong(phoneField.getText().trim()),
                        statusCombo.getValue());
            } catch (NumberFormatException ex) {
                ViewHelper.showWarning("CNIC, License #, and Phone must be numbers.");
                return null;
            }
        });
        return dlg.showAndWait();
    }
}
