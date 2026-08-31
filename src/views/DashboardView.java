package views;

import database.DashboardDAO;
import database.DashboardDAO.Period;
import database.DashboardDAO.ExpenseCategoryTotal;
import database.DashboardDAO.Stats;
import database.DashboardDAO.VehicleEarning;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import javafx.util.StringConverter;

public class DashboardView {

    public static Node getView() {
        VBox content = new VBox(20);
        content.getStyleClass().add("content");
        content.setPadding(new Insets(18));

        ScrollPane scroll = ViewHelper.createResponsiveScroll(content);

        Label title = new Label("Dashboard");
        title.getStyleClass().add("content-title");

        Label subtitle = new Label("Live summary from your database");
        subtitle.getStyleClass().add("muted");

        // Period selector (Overall vs current calendar month)
        ChoiceBox<Period> periodChoice = new ChoiceBox<>(
                FXCollections.observableArrayList(Period.OVERALL, Period.CURRENT_MONTH));
        periodChoice.setConverter(new StringConverter<>() {
            @Override
            public String toString(Period p) {
                if (p == null)
                    return "";
                return p == Period.CURRENT_MONTH ? "This Month" : "Overall";
            }

            @Override
            public Period fromString(String s) {
                if (s == null)
                    return Period.OVERALL;
                return s.toLowerCase().contains("month") ? Period.CURRENT_MONTH : Period.OVERALL;
            }
        });
        periodChoice.setValue(Period.OVERALL);
        periodChoice.setPrefWidth(180);
        periodChoice.setMaxWidth(Double.MAX_VALUE);

        FlowPane periodBar = new FlowPane(10, 8);
        periodBar.setAlignment(Pos.CENTER_LEFT);
        Label viewLbl = new Label("Period:");
        viewLbl.getStyleClass().add("filter-chip-label");
        periodBar.getChildren().addAll(viewLbl, periodChoice);

        DashboardDAO dao = new DashboardDAO();
        NumberFormat fmt = NumberFormat.getNumberInstance(Locale.US);

        Label opsLabel = sectionLabel("Operations");
        FlowPane opsRow = new FlowPane(12, 12);
        opsRow.setAlignment(Pos.CENTER_LEFT);
        opsRow.setMaxWidth(Double.MAX_VALUE);

        Label finLabel = sectionLabel("Financials");
        FlowPane finRow = new FlowPane(12, 12);
        finRow.setAlignment(Pos.CENTER_LEFT);
        finRow.setMaxWidth(Double.MAX_VALUE);

        Label breakdownLabel = sectionLabel("Expense Breakdown");
        TableView<ExpenseCategoryTotal> eTable = new TableView<>();
        eTable.setMinHeight(180);
        eTable.setPrefHeight(220);
        eTable.getStyleClass().add("data-table");
        eTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ExpenseCategoryTotal, String> eCatCol = new TableColumn<>("Category");
        eCatCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().category));
        eCatCol.setMinWidth(110);

        TableColumn<ExpenseCategoryTotal, String> eBizCol = new TableColumn<>("Business (PKR)");
        eBizCol.setCellValueFactory(
                d -> new javafx.beans.property.SimpleStringProperty("PKR " + fmt.format(d.getValue().businessAmount)));
        eBizCol.setMinWidth(120);

        TableColumn<ExpenseCategoryTotal, String> eCustCol = new TableColumn<>("Customer (PKR)");
        eCustCol.setCellValueFactory(
                d -> new javafx.beans.property.SimpleStringProperty("PKR " + fmt.format(d.getValue().customerAmount)));
        eCustCol.setMinWidth(120);

        eTable.getColumns().add(eCatCol);
        eTable.getColumns().add(eBizCol);
        eTable.getColumns().add(eCustCol);
        StackPane eCard = new StackPane(eTable);
        eCard.getStyleClass().add("card");

        Label earningsLabel = sectionLabel("Earnings per Vehicle");
        TableView<VehicleEarning> vTable = new TableView<>();
        vTable.setMinHeight(200);
        vTable.setPrefHeight(260);
        vTable.getStyleClass().add("data-table");
        vTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<VehicleEarning, Integer> vIdCol = new TableColumn<>("Car ID");
        vIdCol.setCellValueFactory(d -> new javafx.beans.property.SimpleIntegerProperty(d.getValue().carId).asObject());
        vIdCol.setMinWidth(60);

        TableColumn<VehicleEarning, String> vModelCol = new TableColumn<>("Model");
        vModelCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().model));
        vModelCol.setMinWidth(120);

        TableColumn<VehicleEarning, String> vBilledCol = new TableColumn<>("Billed");
        vBilledCol.setCellValueFactory(
                d -> new javafx.beans.property.SimpleStringProperty("PKR " + fmt.format(d.getValue().totalBilled)));
        vBilledCol.setMinWidth(100);

        TableColumn<VehicleEarning, String> vRecCol = new TableColumn<>("Received");
        vRecCol.setCellValueFactory(
                d -> new javafx.beans.property.SimpleStringProperty("PKR " + fmt.format(d.getValue().totalReceived)));
        vRecCol.setMinWidth(100);

        TableColumn<VehicleEarning, String> vExpCol = new TableColumn<>("Expenses");
        vExpCol.setCellValueFactory(
                d -> new javafx.beans.property.SimpleStringProperty("PKR " + fmt.format(d.getValue().totalExpenses)));
        vExpCol.setMinWidth(100);

        TableColumn<VehicleEarning, String> vProfitCol = new TableColumn<>("Net Profit");
        vProfitCol.setCellValueFactory(
                d -> new javafx.beans.property.SimpleStringProperty("PKR " + fmt.format(d.getValue().netProfit)));
        vProfitCol.setMinWidth(110);
        vProfitCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(v);
                VehicleEarning ve = getTableView().getItems().get(getIndex());
                setStyle(ve.netProfit >= 0
                        ? "-fx-text-fill: #69db7c; -fx-font-weight:700;"
                        : "-fx-text-fill: #ff6b6b; -fx-font-weight:700;");
            }
        });

        vTable.getColumns().add(vIdCol);
        vTable.getColumns().add(vModelCol);
        vTable.getColumns().add(vBilledCol);
        vTable.getColumns().add(vRecCol);
        vTable.getColumns().add(vExpCol);
        vTable.getColumns().add(vProfitCol);
        StackPane vCard = new StackPane(vTable);
        vCard.getStyleClass().add("card");

        Runnable refresh = () -> {
            Period p = periodChoice.getValue() == null ? Period.OVERALL : periodChoice.getValue();
            String periodText = (p == Period.CURRENT_MONTH) ? "This Month" : "Overall";

            Stats stats;
            try {
                stats = dao.getStats(p);
            } catch (SQLException e) {
                stats = new Stats();
            }

            opsLabel.setText("Operations — " + periodText);
            finLabel.setText("Financials — " + periodText);
            earningsLabel.setText("Earnings per Vehicle — " + periodText);

            opsRow.getChildren().setAll(
                    statCard("Vehicles", String.valueOf(stats.totalVehicles), "#3d5afe"),
                    statCard("Customers", String.valueOf(stats.totalCustomers), "#00bcd4"),
                    statCard("Drivers", String.valueOf(stats.totalDrivers), "#7c4dff"),
                    statCard((p == Period.CURRENT_MONTH ? "Sales (Month)" : "Total Sales"),
                            String.valueOf(stats.totalSales), "#ff9100"),
                    statCard((p == Period.CURRENT_MONTH ? "Completed (Mo)" : "Completed"),
                            String.valueOf(stats.completedRentals), "#ffd43b"),
                    statCard("Active Rentals", String.valueOf(stats.activeSales), "#00c853"),
                    statCard("Overdue", String.valueOf(stats.overdueRentals), "#ff5252"),
                    statCard((p == Period.CURRENT_MONTH ? "Payments (Mo)" : "Payments"),
                            String.valueOf(stats.paymentTransactions), "#00c853"),
                    statCard((p == Period.CURRENT_MONTH ? "Expenses (Mo)" : "Expenses"),
                            String.valueOf(stats.expenseEntries), "#ff5252"),
                    statCard((p == Period.CURRENT_MONTH ? "Avg Sale (Mo)" : "Avg Sale"),
                            "PKR " + fmt.format(stats.avgSaleValue), "#7c4dff"));

            finRow.getChildren().setAll(
                    statCard("Billed", "PKR " + fmt.format(stats.totalBilled), "#ff9100"),
                    statCard("Received", "PKR " + fmt.format(stats.totalReceived), "#00c853"),
                    statCard("Business Exp", "PKR " + fmt.format(stats.businessExpenses), "#ff5252"),
                    statCard("Customer Exp", "PKR " + fmt.format(stats.customerExpenses), "#00bcd4"),
                    statCard("Profit/Loss", "PKR " + fmt.format(stats.netProfit),
                            stats.netProfit >= 0 ? "#69db7c" : "#ff6b6b"),
                    statCard("Outstanding", "PKR " + fmt.format(stats.outstanding), "#ffd43b"));

            breakdownLabel.setText("Expense Breakdown — " + periodText);
            List<ExpenseCategoryTotal> cats = List.of();
            try {
                cats = dao.getExpenseCategoryTotals(p);
            } catch (SQLException e) {
                /* stay empty */ }
            eTable.getItems().setAll(cats);

            List<VehicleEarning> earnings = List.of();
            try {
                earnings = dao.getVehicleEarnings(p);
            } catch (SQLException e) {
                /* stay empty */ }
            vTable.getItems().setAll(earnings);
        };

        periodChoice.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> refresh.run());
        refresh.run();

        content.getChildren().addAll(
                title, subtitle, periodBar,
                opsLabel, opsRow,
                finLabel, finRow,
                breakdownLabel, eCard,
                earningsLabel, vCard);
        return scroll;
    }

    private static Label sectionLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:14px; -fx-font-weight:800; -fx-text-fill:#c5cae9; -fx-padding:8 0 2 0;");
        return l;
    }

    private static VBox statCard(String label, String value, String accent) {
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 800; -fx-text-fill: " + accent + ";");

        Label nameLabel = new Label(label);
        nameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight:600; -fx-text-fill: #a0a5b8;");

        VBox card = new VBox(4, valueLabel, nameLabel);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(14, 16, 14, 16));
        card.setMinWidth(140);
        card.setPrefWidth(160);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle(
                "-fx-background-color: #1a1a24;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: " + accent + "44;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 1;");
        return card;
    }
}
