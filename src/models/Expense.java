package models;

import java.time.LocalDate;

public class Expense {
    private int expenseId;
    private String type;
    private String payer;
    private int saleId;
    private int vehicleId;
    private double amount;
    private LocalDate date;

    public Expense(int expenseId, String type, String payer,
            int saleId, int vehicleId, double amount, LocalDate date) {
        this.expenseId = expenseId;
        this.type = type;
        this.payer = payer;
        this.saleId = saleId;
        this.vehicleId = vehicleId;
        this.amount = amount;
        this.date = date;
    }

    public int getExpenseId() {
        return expenseId;
    }

    public String getType() {
        return type;
    }

    public String getPayer() {
        return payer;
    }

    public int getSaleId() {
        return saleId;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDate getDate() {
        return date;
    }
}
