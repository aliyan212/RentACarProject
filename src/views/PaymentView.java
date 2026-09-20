package views;

import database.PaymentDAO;
import database.SaleDAO;
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
import models.Payment;
import models.Sale;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javafx.application.Platform;

public class PaymentView {

    public static Node getView() {
        VBox content = new VBox(16);
        content.getStyleClass().add("content");
        content.setPadding(new Insets(18));

        Label title = new Label("Payments");
        title.getStyleClass().add("content-title");

        Label subtitle = new Label("Record customer payments and view transaction history");
        subtitle.getStyleClass().add("muted");

        ObservableList<Payment> data = FXCollections.observableArrayList();
        PaymentDAO dao = new PaymentDAO();
        try {
            data.addAll(dao.listPayments());
        } catch (SQLException e) {
            ViewHelper.showError("Could not load payments", e);
        }

        FilteredList<Payment> filteredData = new FilteredList<>(data, p -> true);
        SortedList<Payment> sortedData = new SortedList<>(filteredData);

        TableView<Payment> table = new TableView<>(sortedData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.getStyleClass().add("data-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setMinHeight(280);
        table.setPrefHeight(420);

        TableColumn<Payment, Integer> idCol = new TableColumn<>("Payment #");
        idCol.setCellValueFactory(new PropertyValueFactory<>("paymentId"));
        idCol.setMinWidth(90);

        TableColumn<Payment, Integer> saleCol = new TableColumn<>("Sale #");
        saleCol.setCellValueFactory(new PropertyValueFactory<>("saleId"));
        saleCol.setMinWidth(90);

        TableColumn<Payment, Double> amtCol = new TableColumn<>("Amount (PKR)");
        amtCol.setCellValueFactory(new PropertyValueFactory<>("moneyPaid"));
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

        TableColumn<Payment, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        dateCol.setMinWidth(110);

        table.getColumns().add(idCol);
        table.getColumns().add(saleCol);
        table.getColumns().add(amtCol);
        table.getColumns().add(dateCol);

        TextField searchField = ViewHelper.field("🔍 Search payments by sale #, payment #...");
        searchField.setPrefWidth(270);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(p -> {
                if (newVal == null || newVal.isBlank())
                    return true;
                String lower = newVal.toLowerCase().trim();
                return String.valueOf(p.getPaymentId()).contains(lower)
                        || String.valueOf(p.getSaleId()).contains(lower)
                        || (p.getPaymentDate() != null && p.getPaymentDate().toString().contains(lower));
            });
        });

        Button addBtn = new Button("+ Record Payment");
        addBtn.getStyleClass().add("btn-primary");
        Button deleteBtn = new Button("🗑 Delete");
        deleteBtn.getStyleClass().add("btn-danger");
        FlowPane toolbar = new FlowPane(10, 10, searchField, addBtn, deleteBtn);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        addBtn.setOnAction(e -> showAddDialog().ifPresent(p -> {
            try {
                dao.insert(p.getSaleId(), p.getMoneyPaid(), p.getPaymentDate());
                data.setAll(dao.listPayments());
            } catch (SQLException ex) {
                ViewHelper.showError("Could not add payment", ex);
            }
        }));

        deleteBtn.setOnAction(e -> {
            Payment sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                ViewHelper.showWarning("Select a row first.");
                return;
            }
            if (!ViewHelper.confirmDelete("Payment #" + sel.getPaymentId()))
                return;
            try {
                dao.delete(sel.getPaymentId());
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

    private static Optional<Payment> showAddDialog() {
        Dialog<Payment> dlg = new Dialog<>();
        dlg.setTitle("Record Payment");
        dlg.setHeaderText(null);
        ViewHelper.styleDialog(dlg.getDialogPane());

        List<Sale> saleList = List.of();
        try {
            saleList = new SaleDAO().listSalesWithBalance();
        } catch (SQLException e) {
            ViewHelper.showError("Could not load sales", e);
        }
        ObservableList<Sale> allSales = FXCollections.observableArrayList(saleList);
        FilteredList<Sale> filtSales = new FilteredList<>(allSales, p -> true);
        ComboBox<Sale> saleCombo = new ComboBox<>(filtSales);
        saleCombo.setEditable(true);
        saleCombo.setMaxWidth(Double.MAX_VALUE);
        saleCombo.setPromptText("Type to search…");

        NumberFormat fmt = NumberFormat.getNumberInstance(Locale.US);

        saleCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Sale s) {
                if (s == null)
                    return "";
                String balStr = s.getBalance() <= 0 ? "Paid" : "Due: PKR " + fmt.format(s.getBalance());
                return "Sale #" + s.getSaleId() + " | Cust " + s.getCustomerCnic() + " | " + balStr;
            }

            @Override
            public Sale fromString(String str) {
                return allSales.stream().filter(s -> toString(s).equals(str)).findFirst().orElse(null);
            }
        });

        Label balanceInfoLabel = new Label("");
        balanceInfoLabel.setWrapText(true);

        TextField amountField = ViewHelper.field("e.g. 5000");
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setMaxWidth(Double.MAX_VALUE);

        boolean[] saleSelecting = { false };
        saleCombo.valueProperty().addListener((obs, o, n) -> {
            saleSelecting[0] = true;
            if (n != null) {
                long bal = (long) Math.ceil(n.getBalance());
                if (bal > 0) {
                    balanceInfoLabel.setText(String.format("Billed: PKR %s  |  Paid: PKR %s  |  Due: PKR %s",
                            fmt.format(n.getTotalAmount()), fmt.format(n.getAmountPaid()), fmt.format(bal)));
                    balanceInfoLabel.setStyle("-fx-text-fill: #ffd43b; -fx-font-size: 12px; -fx-font-weight: 700;");
                    amountField.setText(String.valueOf(bal));
                } else {
                    balanceInfoLabel.setText(String.format("Billed: PKR %s  |  Paid: PKR %s  |  Fully Paid (PKR 0 due)",
                            fmt.format(n.getTotalAmount()), fmt.format(n.getAmountPaid())));
                    balanceInfoLabel.setStyle("-fx-text-fill: #69db7c; -fx-font-size: 12px; -fx-font-weight: 700;");
                    amountField.setText("0");
                }
            } else {
                balanceInfoLabel.setText("");
            }
        });

        saleCombo.getEditor().textProperty().addListener((obs, o, n) -> {
            if (saleSelecting[0]) {
                saleSelecting[0] = false;
                return;
            }
            String q = n == null ? "" : n.toLowerCase();
            filtSales.setPredicate(s -> q.isEmpty()
                    || String.valueOf(s.getSaleId()).contains(q)
                    || String.valueOf(s.getCustomerCnic()).contains(q)
                    || s.getRentalType().toLowerCase().contains(q));
            Platform.runLater(() -> {
                if (!saleCombo.isShowing())
                    saleCombo.show();
            });
        });

        VBox saleBox = new VBox(4, saleCombo, balanceInfoLabel);

        dlg.getDialogPane().setContent(ViewHelper.form(
                "Sale", saleBox,
                "Amount (PKR)", amountField,
                "Date", datePicker));

        ButtonType save = new ButtonType("Save Payment", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

        dlg.setResultConverter(btn -> {
            if (btn != save)
                return null;
            Sale sale = saleCombo.getValue();
            if (sale == null) {
                ViewHelper.showWarning("Please select a sale.");
                return null;
            }
            if (datePicker.getValue() == null) {
                ViewHelper.showWarning("Please select a valid payment date.");
                return null;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountField.getText().trim());
                if (amount <= 0) {
                    ViewHelper.showWarning("Payment amount must be greater than 0.");
                    return null;
                }
            } catch (Exception ex) {
                ViewHelper.showWarning("Amount must be a valid number.");
                return null;
            }

            if (sale.getBalance() <= 0) {
                ViewHelper.showWarning("Sale #" + sale.getSaleId() + " is already fully paid. No further payments are due.");
                return null;
            }

            if (amount > sale.getBalance() + 0.001) {
                ViewHelper.showWarning(String.format("Payment amount (PKR %s) exceeds the remaining balance (PKR %s) for Sale #%d.",
                        fmt.format(amount), fmt.format(sale.getBalance()), sale.getSaleId()));
                return null;
            }

            return new Payment(0,
                    sale.getSaleId(),
                    amount,
                    datePicker.getValue());
        });
        return dlg.showAndWait();
    }
}
