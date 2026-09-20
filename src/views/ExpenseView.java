package views;

import database.ExpenseDAO;
import database.SaleDAO;
import database.VehicleDAO;
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
import javafx.util.StringConverter;
import models.Expense;
import models.Sale;
import models.Vehicle;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javafx.application.Platform;

public class ExpenseView {

    public static Node getView() {
        VBox content = new VBox(16);
        content.getStyleClass().add("content");
        content.setPadding(new Insets(18));

        Label title = new Label("Expenses");
        title.getStyleClass().add("content-title");

        Label subtitle = new Label("Track business & customer expenses across fleet vehicles and rentals");
        subtitle.getStyleClass().add("muted");

        ObservableList<Expense> data = FXCollections.observableArrayList();
        ExpenseDAO dao = new ExpenseDAO();
        try {
            data.addAll(dao.listExpenses());
        } catch (SQLException e) {
            ViewHelper.showError("Could not load expenses", e);
        }

        FilteredList<Expense> filteredData = new FilteredList<>(data, p -> true);
        SortedList<Expense> sortedData = new SortedList<>(filteredData);

        TableView<Expense> table = new TableView<>(sortedData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.getStyleClass().add("data-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setMinHeight(260);
        table.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Expense, Integer> idCol = new TableColumn<>("Exp #");
        idCol.setCellValueFactory(new PropertyValueFactory<>("expenseId"));
        idCol.setMinWidth(70);

        TableColumn<Expense, String> typeCol = new TableColumn<>("Category");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setMinWidth(120);

        TableColumn<Expense, String> payerCol = new TableColumn<>("Paid By");
        payerCol.setCellValueFactory(new PropertyValueFactory<>("payer"));
        payerCol.setMinWidth(90);

        TableColumn<Expense, Integer> saleCol = new TableColumn<>("Sale #");
        saleCol.setCellValueFactory(new PropertyValueFactory<>("saleId"));
        saleCol.setMinWidth(70);

        TableColumn<Expense, Integer> vehicleCol = new TableColumn<>("Car ID");
        vehicleCol.setCellValueFactory(new PropertyValueFactory<>("vehicleId"));
        vehicleCol.setMinWidth(70);

        TableColumn<Expense, Double> amtCol = new TableColumn<>("Amount (PKR)");
        amtCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amtCol.setMinWidth(120);
        amtCol.setCellFactory(col -> {
            NumberFormat fmt = NumberFormat.getNumberInstance(Locale.US);
            return new TableCell<>() {
                @Override
                protected void updateItem(Double v, boolean empty) {
                    super.updateItem(v, empty);
                    setText((empty || v == null) ? null : "PKR " + fmt.format(v));
                }
            };
        });

        TableColumn<Expense, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setMinWidth(100);

        table.getColumns().add(idCol);
        table.getColumns().add(typeCol);
        table.getColumns().add(payerCol);
        table.getColumns().add(saleCol);
        table.getColumns().add(vehicleCol);
        table.getColumns().add(amtCol);
        table.getColumns().add(dateCol);

        TextField searchField = ViewHelper.field("🔍 Search expenses by category, payer, ID...");
        searchField.setPrefWidth(270);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(exp -> {
                if (newVal == null || newVal.isBlank())
                    return true;
                String lower = newVal.toLowerCase().trim();
                return (exp.getType() != null && exp.getType().toLowerCase().contains(lower))
                        || (exp.getPayer() != null && exp.getPayer().toLowerCase().contains(lower))
                        || String.valueOf(exp.getExpenseId()).contains(lower)
                        || String.valueOf(exp.getSaleId()).contains(lower)
                        || String.valueOf(exp.getVehicleId()).contains(lower);
            });
        });

        Button addBtn = new Button("+ Add Expense");
        addBtn.getStyleClass().add("btn-primary");
        Button deleteBtn = new Button("🗑 Delete");
        deleteBtn.getStyleClass().add("btn-danger");
        FlowPane toolbar = new FlowPane(10, 10, searchField, addBtn, deleteBtn);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        addBtn.setOnAction(e -> showAddDialog().ifPresent(exp -> {
            try {
                dao.insert(exp.getType(), exp.getPayer(), exp.getSaleId(),
                        exp.getVehicleId(), exp.getAmount(), exp.getDate());
                data.setAll(dao.listExpenses());
            } catch (SQLException ex) {
                ViewHelper.showError("Could not add expense", ex);
            }
        }));

        deleteBtn.setOnAction(e -> {
            Expense sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                ViewHelper.showWarning("Select a row first.");
                return;
            }
            if (!ViewHelper.confirmDelete("Expense #" + sel.getExpenseId()))
                return;
            try {
                dao.delete(sel.getExpenseId());
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

    private static Optional<Expense> showAddDialog() {
        Dialog<Expense> dlg = new Dialog<>();
        dlg.setTitle("Add Expense");
        dlg.setHeaderText(null);
        ViewHelper.styleDialog(dlg.getDialogPane());

        List<Sale> saleList = List.of();
        try {
            saleList = new SaleDAO().listSales();
        } catch (SQLException e) {
            ViewHelper.showError("Could not load sales", e);
        }
        Sale noSale = new Sale(0, 0, 0, 0, null, null, "— None —", 0);
        ObservableList<Sale> allSales = FXCollections.observableArrayList();
        allSales.add(noSale);
        allSales.addAll(saleList);
        FilteredList<Sale> filtSales = new FilteredList<>(allSales, p -> true);
        ComboBox<Sale> saleCombo = new ComboBox<>(filtSales);
        saleCombo.setEditable(true);
        saleCombo.setMaxWidth(Double.MAX_VALUE);
        saleCombo.setPromptText("Type to search…");
        saleCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Sale s) {
                if (s == null)
                    return "";
                return s.getSaleId() == 0 ? "0 – None" : "Sale #" + s.getSaleId() + " | Cust " + s.getCustomerCnic();
            }

            @Override
            public Sale fromString(String str) {
                return allSales.stream().filter(s -> toString(s).equals(str)).findFirst().orElse(null);
            }
        });
        boolean[] saleSelecting = { false };
        saleCombo.valueProperty().addListener((obs, o, n) -> saleSelecting[0] = true);
        saleCombo.getEditor().textProperty().addListener((obs, o, n) -> {
            if (saleSelecting[0]) {
                saleSelecting[0] = false;
                return;
            }
            String q = n == null ? "" : n.toLowerCase();
            filtSales.setPredicate(s -> q.isEmpty()
                    || String.valueOf(s.getSaleId()).contains(q)
                    || String.valueOf(s.getCustomerCnic()).contains(q));
            Platform.runLater(() -> {
                if (!saleCombo.isShowing())
                    saleCombo.show();
            });
        });
        saleCombo.setValue(noSale);

        List<Vehicle> vehList = List.of();
        try {
            vehList = new VehicleDAO().listVehicles();
        } catch (SQLException e) {
            ViewHelper.showError("Could not load vehicles", e);
        }
        Vehicle noVeh = new Vehicle(0, "— None —", "0", 0);
        ObservableList<Vehicle> allVeh = FXCollections.observableArrayList();
        allVeh.add(noVeh);
        allVeh.addAll(vehList);
        FilteredList<Vehicle> filtVeh = new FilteredList<>(allVeh, p -> true);
        ComboBox<Vehicle> carCombo = new ComboBox<>(filtVeh);
        carCombo.setEditable(true);
        carCombo.setMaxWidth(Double.MAX_VALUE);
        carCombo.setPromptText("Type to search…");
        carCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Vehicle v) {
                if (v == null)
                    return "";
                return v.getCarId() == 0 ? "0 – None" : v.getCarId() + " – " + v.getModel();
            }

            @Override
            public Vehicle fromString(String str) {
                return allVeh.stream().filter(v -> toString(v).equals(str)).findFirst().orElse(null);
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
        carCombo.setValue(noVeh);

        ComboBox<String> categoryCombo = new ComboBox<>(
                FXCollections.observableArrayList("Fuel", "Repair", "Insurance", "Other"));
        categoryCombo.setMaxWidth(Double.MAX_VALUE);
        categoryCombo.setValue("Fuel");

        TextField otherDetailsField = ViewHelper.field("If Other, add details (optional)");
        otherDetailsField.setDisable(true);
        categoryCombo.valueProperty().addListener((obs, o, n) -> {
            boolean isOther = n != null && n.equalsIgnoreCase("Other");
            otherDetailsField.setDisable(!isOther);
            if (!isOther)
                otherDetailsField.clear();
        });

        ComboBox<String> payerCombo = new ComboBox<>(FXCollections.observableArrayList("Business", "Customer"));
        payerCombo.setMaxWidth(Double.MAX_VALUE);
        payerCombo.setValue("Business");

        TextField amountField = ViewHelper.field("e.g. 2500");
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setMaxWidth(Double.MAX_VALUE);

        dlg.getDialogPane().setContent(ViewHelper.form(
                "Category", categoryCombo,
                "Other", otherDetailsField,
                "Paid By", payerCombo,
                "Sale", saleCombo,
                "Vehicle", carCombo,
                "Amount", amountField,
                "Date", datePicker));

        ButtonType save = new ButtonType("Save Expense", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

        dlg.setResultConverter(btn -> {
            if (btn != save)
                return null;
            Sale sale = saleCombo.getValue();
            Vehicle car = carCombo.getValue();
            if (sale == null) {
                ViewHelper.showWarning("Please select a sale (or None).");
                return null;
            }
            if (car == null) {
                ViewHelper.showWarning("Please select a vehicle (or None).");
                return null;
            }
            if (payerCombo.getValue() == null || payerCombo.getValue().trim().isEmpty()) {
                ViewHelper.showWarning("Paid By is required (Business or Customer).");
                return null;
            }
            if (categoryCombo.getValue() == null || categoryCombo.getValue().trim().isEmpty()) {
                ViewHelper.showWarning("Category is required.");
                return null;
            }
            if (datePicker.getValue() == null) {
                ViewHelper.showWarning("Please select a valid expense date.");
                return null;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountField.getText().trim());
                if (amount <= 0) {
                    ViewHelper.showWarning("Expense amount must be greater than 0.");
                    return null;
                }
            } catch (Exception ex) {
                ViewHelper.showWarning("Amount must be a valid number.");
                return null;
            }

            String category = categoryCombo.getValue().trim();
            String storedType = category;
            if ("Other".equalsIgnoreCase(category)) {
                String details = otherDetailsField.getText() == null ? "" : otherDetailsField.getText().trim();
                storedType = details.isEmpty() ? "Other" : ("Other: " + details);
            }
            return new Expense(0,
                    storedType,
                    payerCombo.getValue().trim(),
                    sale.getSaleId(),
                    car.getCarId(),
                    amount,
                    datePicker.getValue());
        });
        return dlg.showAndWait();
    }
}
