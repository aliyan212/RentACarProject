package views;

import database.CustomerDAO;
import database.DriverDAO;
import database.SaleDAO;
import database.VehicleDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.StringConverter;
import models.Customer;
import models.Driver;
import models.Sale;
import models.Vehicle;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javafx.application.Platform;

public class SalesView {

    public static Node getView() {
        VBox content = new VBox(16);
        content.getStyleClass().add("content");
        content.setPadding(new Insets(18));

        Label title = new Label("Sales (Rentals)");
        title.getStyleClass().add("content-title");

        Label subtitle = new Label("Manage active rentals, billing, and payment balances");
        subtitle.getStyleClass().add("muted");

        ObservableList<Sale> data = FXCollections.observableArrayList();
        SaleDAO dao = new SaleDAO();
        try {
            data.addAll(dao.listSalesWithBalance());
        } catch (SQLException e) {
            ViewHelper.showError("Could not load sales", e);
        }

        FilteredList<Sale> filtered = new FilteredList<>(data, s -> true);

        TableView<Sale> table = new TableView<>(filtered);
        table.getStyleClass().add("data-table");
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setMinHeight(300);
        table.setPrefHeight(450);

        TableColumn<Sale, Integer> idCol = new TableColumn<>("Sale #");
        idCol.setCellValueFactory(new PropertyValueFactory<>("saleId"));
        idCol.setMinWidth(70);
        idCol.setPrefWidth(75);

        TableColumn<Sale, Integer> customerCol = new TableColumn<>("Customer CNIC");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerCnic"));
        customerCol.setMinWidth(120);
        customerCol.setPrefWidth(130);

        TableColumn<Sale, Integer> carCol = new TableColumn<>("Car ID");
        carCol.setCellValueFactory(new PropertyValueFactory<>("carId"));
        carCol.setMinWidth(70);
        carCol.setPrefWidth(80);

        TableColumn<Sale, Integer> driverCol = new TableColumn<>("Driver CNIC");
        driverCol.setCellValueFactory(new PropertyValueFactory<>("driverCnic"));
        driverCol.setMinWidth(110);
        driverCol.setPrefWidth(120);

        TableColumn<Sale, LocalDate> startCol = new TableColumn<>("Start Date");
        startCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        startCol.setMinWidth(100);
        startCol.setPrefWidth(105);

        TableColumn<Sale, LocalDate> endCol = new TableColumn<>("End Date");
        endCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        endCol.setMinWidth(100);
        endCol.setPrefWidth(105);

        TableColumn<Sale, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("rentalType"));
        typeCol.setMinWidth(80);
        typeCol.setPrefWidth(90);

        TableColumn<Sale, Integer> totalCol = new TableColumn<>("Billed (PKR)");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        totalCol.setMinWidth(100);
        totalCol.setPrefWidth(110);

        TableColumn<Sale, Double> paidCol = new TableColumn<>("Paid (PKR)");
        paidCol.setCellValueFactory(new PropertyValueFactory<>("amountPaid"));
        paidCol.setMinWidth(100);
        paidCol.setPrefWidth(110);
        paidCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText((empty || v == null || v < 0) ? null
                        : String.format("%,.0f", v));
            }
        });

        TableColumn<Sale, Double> balCol = new TableColumn<>("Balance (PKR)");
        balCol.setCellValueFactory(new PropertyValueFactory<>("balance"));
        balCol.setMinWidth(105);
        balCol.setPrefWidth(115);
        balCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null || v < 0) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(String.format("%,.0f", v));
                setStyle(v > 0 ? "-fx-text-fill: #ff6b6b; -fx-font-weight:700;"
                        : "-fx-text-fill: #69db7c; -fx-font-weight:700;");
            }
        });

        TableColumn<Sale, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        statusCol.setMinWidth(120);
        statusCol.setPrefWidth(130);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null || v.isEmpty()) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(v);
                Sale row = getTableView().getItems().get(getIndex());
                String color = switch (v) {
                    case "Paid" -> "-fx-text-fill: #69db7c; -fx-font-weight:700;";
                    case "Partial" -> "-fx-text-fill: #ffd43b; -fx-font-weight:700;";
                    default -> "-fx-text-fill: #ff6b6b; -fx-font-weight:700;";
                };
                setStyle(color);
                if (row != null && row.isOverdue() && !"Paid".equals(v))
                    setText(v + " ⚠ Overdue");
            }
        });

        table.getColumns().add(idCol);
        table.getColumns().add(customerCol);
        table.getColumns().add(carCol);
        table.getColumns().add(driverCol);
        table.getColumns().add(startCol);
        table.getColumns().add(endCol);
        table.getColumns().add(typeCol);
        table.getColumns().add(totalCol);
        table.getColumns().add(paidCol);
        table.getColumns().add(balCol);
        table.getColumns().add(statusCol);

        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Sale s, boolean empty) {
                super.updateItem(s, empty);
                if (!empty && s != null && s.isOverdue())
                    setStyle("-fx-background-color: #2a1a0a;");
                else
                    setStyle("");
            }
        });

        Button addBtn = new Button("+ New Rental");
        addBtn.getStyleClass().add("btn-primary");
        Button receiptBtn = new Button("📄 View Receipt");
        receiptBtn.getStyleClass().add("btn-secondary");
        Button deleteBtn = new Button("🗑 Delete");
        deleteBtn.getStyleClass().add("btn-danger");
        FlowPane toolbar = new FlowPane(10, 10, addBtn, receiptBtn, deleteBtn);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        receiptBtn.setOnAction(e -> {
            Sale sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                ViewHelper.showWarning("Select a rental row first.");
                return;
            }
            showReceiptDialog(sel);
        });

        ComboBox<String> statusFilter = new ComboBox<>(FXCollections.observableArrayList(
                "All", "Paid", "Partial", "Unpaid", "Overdue"));
        statusFilter.setValue("All");
        statusFilter.setPrefWidth(120);

        DatePicker fromDate = new DatePicker();
        fromDate.setPromptText("From Date");
        fromDate.setPrefWidth(130);

        DatePicker toDate = new DatePicker();
        toDate.setPromptText("To Date");
        toDate.setPrefWidth(130);

        TextField custFilter = ViewHelper.field("Cust CNIC");
        TextField carFilter = ViewHelper.field("Car ID");
        custFilter.setPrefWidth(140);
        carFilter.setPrefWidth(100);

        Button clearFilters = new Button("Reset");
        clearFilters.getStyleClass().add("btn-secondary");

        FlowPane filters = new FlowPane(10, 8);
        filters.getStyleClass().add("filter-bar");
        filters.setAlignment(Pos.CENTER_LEFT);
        filters.getChildren().addAll(
                new Label("Status:"), statusFilter,
                new Label("From:"), fromDate,
                new Label("To:"), toDate,
                custFilter,
                carFilter,
                clearFilters);

        Runnable applyFilters = () -> {
            String status = statusFilter.getValue() == null ? "All" : statusFilter.getValue();
            LocalDate from = fromDate.getValue();
            LocalDate to = toDate.getValue();
            String custQ = custFilter.getText() == null ? "" : custFilter.getText().trim();
            String carQ = carFilter.getText() == null ? "" : carFilter.getText().trim();

            filtered.setPredicate(sale -> {
                if (sale == null)
                    return false;

                // Status filter
                if (!"All".equalsIgnoreCase(status)) {
                    if ("Overdue".equalsIgnoreCase(status)) {
                        if (!sale.isOverdue())
                            return false;
                    } else {
                        String ps = sale.getPaymentStatus() == null ? "" : sale.getPaymentStatus();
                        if (!ps.equalsIgnoreCase(status))
                            return false;
                    }
                }

                LocalDate start = sale.getStartDate();
                if (from != null) {
                    if (start == null || start.isBefore(from))
                        return false;
                }
                if (to != null) {
                    if (start == null || start.isAfter(to))
                        return false;
                }

                if (!custQ.isEmpty()) {
                    if (!String.valueOf(sale.getCustomerCnic()).contains(custQ))
                        return false;
                }

                if (!carQ.isEmpty()) {
                    if (!String.valueOf(sale.getCarId()).contains(carQ))
                        return false;
                }

                return true;
            });
        };

        statusFilter.valueProperty().addListener((obs, o, n) -> applyFilters.run());
        fromDate.valueProperty().addListener((obs, o, n) -> applyFilters.run());
        toDate.valueProperty().addListener((obs, o, n) -> applyFilters.run());
        custFilter.textProperty().addListener((obs, o, n) -> applyFilters.run());
        carFilter.textProperty().addListener((obs, o, n) -> applyFilters.run());

        clearFilters.setOnAction(e -> {
            statusFilter.setValue("All");
            fromDate.setValue(null);
            toDate.setValue(null);
            custFilter.clear();
            carFilter.clear();
            applyFilters.run();
        });

        applyFilters.run();

        addBtn.setOnAction(e -> showAddDialog().ifPresent(s -> {
            try {
                dao.insert(s.getCustomerCnic(), s.getCarId(), s.getDriverCnic(),
                        s.getStartDate(), s.getEndDate(), s.getRentalType(), s.getTotalAmount());
                data.setAll(dao.listSalesWithBalance());
                applyFilters.run();
            } catch (SQLException ex) {
                ViewHelper.showError("Could not add sale", ex);
            }
        }));

        deleteBtn.setOnAction(e -> {
            Sale sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                ViewHelper.showWarning("Select a row first.");
                return;
            }
            if (!ViewHelper.confirmDelete("Sale #" + sel.getSaleId()))
                return;
            try {
                dao.delete(sel.getSaleId());
                data.remove(sel);
                applyFilters.run();
            } catch (SQLException ex) {
                ViewHelper.showError("Could not delete", ex);
            }
        });

        StackPane card = new StackPane(table);
        card.getStyleClass().add("card");
        content.getChildren().addAll(title, subtitle, toolbar, filters, card);
        return ViewHelper.createResponsiveScroll(content);
    }

    private static Optional<Sale> showAddDialog() {
        Dialog<Sale> dlg = new Dialog<>();
        dlg.setTitle("New Rental");
        dlg.setHeaderText(null);
        ViewHelper.styleDialog(dlg.getDialogPane());

        // ── Customer searchable dropdown ─────────────────────────────────
        List<Customer> custList = List.of();
        try {
            custList = new CustomerDAO().listCustomers();
        } catch (SQLException e) {
            ViewHelper.showError("Could not load customers", e);
        }
        ObservableList<Customer> allCust = FXCollections.observableArrayList(custList);
        FilteredList<Customer> filtCust = new FilteredList<>(allCust, p -> true);
        ComboBox<Customer> custCombo = new ComboBox<>(filtCust);
        custCombo.setEditable(true);
        custCombo.setMaxWidth(Double.MAX_VALUE);
        custCombo.setPromptText("Type to search…");
        custCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Customer c) {
                return c == null ? "" : c.getCnic() + " – " + c.getName();
            }

            @Override
            public Customer fromString(String s) {
                return allCust.stream().filter(c -> toString(c).equals(s)).findFirst().orElse(null);
            }
        });
        boolean[] custSelecting = { false };
        custCombo.valueProperty().addListener((obs, o, n) -> custSelecting[0] = true);
        custCombo.getEditor().textProperty().addListener((obs, o, n) -> {
            if (custSelecting[0]) {
                custSelecting[0] = false;
                return;
            }
            String q = n == null ? "" : n.toLowerCase();
            filtCust.setPredicate(c -> q.isEmpty()
                    || String.valueOf(c.getCnic()).contains(q)
                    || c.getName().toLowerCase().contains(q));
            Platform.runLater(() -> {
                if (!custCombo.isShowing())
                    custCombo.show();
            });
        });

        List<Vehicle> vehList = List.of();
        try {
            vehList = new VehicleDAO().listVehicles();
        } catch (SQLException e) {
            ViewHelper.showError("Could not load vehicles", e);
        }
        ObservableList<Vehicle> allVeh = FXCollections.observableArrayList(vehList);
        FilteredList<Vehicle> filtVeh = new FilteredList<>(allVeh, p -> true);
        ComboBox<Vehicle> carCombo = new ComboBox<>(filtVeh);
        carCombo.setEditable(true);
        carCombo.setMaxWidth(Double.MAX_VALUE);
        carCombo.setPromptText("Type to search…");
        carCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Vehicle v) {
                return v == null ? "" : v.getCarId() + " – " + v.getModel();
            }

            @Override
            public Vehicle fromString(String s) {
                return allVeh.stream().filter(v -> toString(v).equals(s)).findFirst().orElse(null);
            }
        });
        boolean[] carSelecting = { false };
        carCombo.valueProperty().addListener((obs, o, n) -> carSelecting[0] = true);
        carCombo.getEditor().textProperty().addListener((obs, o, n) -> {
            if (carSelecting[0]) {
                carSelecting[0] = false;
                return;
            }
            String q = n == null ? "" : n.toLowerCase();
            filtVeh.setPredicate(v -> q.isEmpty()
                    || String.valueOf(v.getCarId()).contains(q)
                    || v.getModel().toLowerCase().contains(q));
            Platform.runLater(() -> {
                if (!carCombo.isShowing())
                    carCombo.show();
            });
        });

        List<Driver> drvList = List.of();
        try {
            drvList = new DriverDAO().listDrivers();
        } catch (SQLException e) {
            ViewHelper.showError("Could not load drivers", e);
        }
        Driver selfDrive = new Driver(0, "Self-drive", 0, 0, "");
        ObservableList<Driver> allDrv = FXCollections.observableArrayList();
        allDrv.add(selfDrive);
        allDrv.addAll(drvList);
        FilteredList<Driver> filtDrv = new FilteredList<>(allDrv, p -> true);
        ComboBox<Driver> driverCombo = new ComboBox<>(filtDrv);
        driverCombo.setEditable(true);
        driverCombo.setMaxWidth(Double.MAX_VALUE);
        driverCombo.setPromptText("Type to search…");
        driverCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Driver d) {
                if (d == null)
                    return "";
                return d.getCnic() == 0 ? "0 – Self-drive" : d.getCnic() + " – " + d.getName();
            }

            @Override
            public Driver fromString(String s) {
                return allDrv.stream().filter(d -> toString(d).equals(s)).findFirst().orElse(null);
            }
        });
        boolean[] drvSelecting = { false };
        driverCombo.valueProperty().addListener((obs, o, n) -> drvSelecting[0] = true);
        driverCombo.getEditor().textProperty().addListener((obs, o, n) -> {
            if (drvSelecting[0]) {
                drvSelecting[0] = false;
                return;
            }
            String q = n == null ? "" : n.toLowerCase();
            filtDrv.setPredicate(d -> q.isEmpty()
                    || String.valueOf(d.getCnic()).contains(q)
                    || d.getName().toLowerCase().contains(q));
            Platform.runLater(() -> {
                if (!driverCombo.isShowing())
                    driverCombo.show();
            });
        });
        driverCombo.setValue(selfDrive);

        TextField startField = ViewHelper.field("YYYY-MM-DD");
        TextField endField = ViewHelper.field("YYYY-MM-DD");
        TextField typeField = ViewHelper.field("Daily / Weekly / Monthly");
        TextField totalField = ViewHelper.field("e.g. 15000");

        dlg.getDialogPane().setContent(ViewHelper.form(
                "Customer", custCombo,
                "Vehicle", carCombo,
                "Driver", driverCombo,
                "Start Date", startField,
                "End Date", endField,
                "Rental Type", typeField,
                "Total (PKR)", totalField));

        ButtonType save = new ButtonType("Save Rental", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

        dlg.setResultConverter(btn -> {
            if (btn != save)
                return null;
            Customer cust = custCombo.getValue();
            Vehicle car = carCombo.getValue();
            Driver drv = driverCombo.getValue();
            if (cust == null) {
                ViewHelper.showWarning("Please select a customer.");
                return null;
            }
            if (car == null) {
                ViewHelper.showWarning("Please select a vehicle.");
                return null;
            }
            if (drv == null) {
                ViewHelper.showWarning("Please select a driver (or Self-drive).");
                return null;
            }
            try {
                return new Sale(
                        cust.getCnic(),
                        car.getCarId(),
                        drv.getCnic(),
                        LocalDate.parse(startField.getText().trim()),
                        LocalDate.parse(endField.getText().trim()),
                        typeField.getText().trim(),
                        Integer.parseInt(totalField.getText().trim()));
            } catch (Exception ex) {
                ViewHelper.showWarning("Check dates (YYYY-MM-DD) and Total (number).");
                return null;
            }
        });
        return dlg.showAndWait();
    }

    private static void showReceiptDialog(Sale s) {
        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Rental Invoice #" + s.getSaleId());
        dlg.setHeaderText(null);
        ViewHelper.styleDialog(dlg.getDialogPane());

        VBox box = new VBox(14);
        box.setPadding(new Insets(10));

        NumberFormat fmt = NumberFormat.getNumberInstance(Locale.US);

        Label invoiceTitle = new Label("Rental Agreement & Invoice");
        invoiceTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: #ffffff;");

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(8);

        String[][] details = {
                { "Agreement ID", "#" + s.getSaleId() },
                { "Customer CNIC", String.valueOf(s.getCustomerCnic()) },
                { "Vehicle ID", String.valueOf(s.getCarId()) },
                { "Driver", s.getDriverCnic() == 0 ? "Self-drive" : "CNIC " + s.getDriverCnic() },
                { "Rental Period", s.getStartDate() + " to " + s.getEndDate() },
                { "Rental Type", s.getRentalType() },
                { "Total Amount", "PKR " + fmt.format(s.getTotalAmount()) },
                { "Advance Paid", "PKR " + fmt.format(s.getAmountPaid()) },
                { "Balance Due", "PKR " + fmt.format(s.getBalance()) },
                { "Payment Status", s.getPaymentStatus() + (s.isOverdue() ? " (Overdue)" : "") }
        };

        for (int i = 0; i < details.length; i++) {
            Label lblKey = new Label(details[i][0] + ":");
            lblKey.setStyle("-fx-font-weight: 700; -fx-text-fill: #8c9eff;");
            Label lblVal = new Label(details[i][1]);
            lblVal.setStyle("-fx-text-fill: #e8eaed;");
            grid.add(lblKey, 0, i);
            grid.add(lblVal, 1, i);
        }

        Button copyBtn = new Button("📋 Copy Invoice Summary");
        copyBtn.getStyleClass().add("btn-primary");
        copyBtn.setOnAction(e -> {
            StringBuilder sb = new StringBuilder();
            sb.append("═══════════════════════════════════\n");
            sb.append("   ALIEON'S RENT-A-CAR INVOICE     \n");
            sb.append("═══════════════════════════════════\n");
            sb.append("Rental ID:       #").append(s.getSaleId()).append("\n");
            sb.append("Customer CNIC:   ").append(s.getCustomerCnic()).append("\n");
            sb.append("Vehicle ID:      ").append(s.getCarId()).append("\n");
            sb.append("Driver:          ").append(s.getDriverCnic() == 0 ? "Self-drive" : s.getDriverCnic()).append("\n");
            sb.append("Duration:        ").append(s.getStartDate()).append(" to ").append(s.getEndDate()).append("\n");
            sb.append("Rental Type:     ").append(s.getRentalType()).append("\n");
            sb.append("───────────────────────────────────\n");
            sb.append("Total Bill:      PKR ").append(fmt.format(s.getTotalAmount())).append("\n");
            sb.append("Advance Paid:    PKR ").append(fmt.format(s.getAmountPaid())).append("\n");
            sb.append("Balance Due:     PKR ").append(fmt.format(s.getBalance())).append("\n");
            sb.append("Status:          ").append(s.getPaymentStatus()).append(s.isOverdue() ? " (Overdue)" : "").append("\n");
            sb.append("═══════════════════════════════════\n");

            javafx.scene.input.ClipboardContent cc = new javafx.scene.input.ClipboardContent();
            cc.putString(sb.toString());
            javafx.scene.input.Clipboard.getSystemClipboard().setContent(cc);
            copyBtn.setText("✓ Copied to Clipboard!");
        });

        box.getChildren().addAll(invoiceTitle, new Separator(), grid, new Separator(), copyBtn);
        dlg.getDialogPane().setContent(ViewHelper.createResponsiveScroll(box));
        dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dlg.showAndWait();
    }
}
