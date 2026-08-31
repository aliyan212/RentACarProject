package models;

import java.time.LocalDate;

public class Sale {
    private int saleId;
    private long customerCnic;
    private int carId;
    private long driverCnic;
    private LocalDate startDate;
    private LocalDate endDate;
    private String rentalType;
    private int totalAmount;

    public Sale(long customerCnic, int carId, long driverCnic,
            LocalDate start, LocalDate end, String type, int total) {
        this.customerCnic = customerCnic;
        this.carId = carId;
        this.driverCnic = driverCnic;
        this.startDate = start;
        this.endDate = end;
        this.rentalType = type;
        this.totalAmount = total;
    }

    public Sale(int saleId, long customerCnic, int carId, long driverCnic,
            LocalDate start, LocalDate end, String type, int total) {
        this(customerCnic, carId, driverCnic, start, end, type, total);
        this.saleId = saleId;
    }

    private double amountPaid = -1;
    private double balance = -1;
    private String paymentStatus = "";

    public void setAmountPaid(double v) {
        this.amountPaid = v;
    }

    public void setBalance(double v) {
        this.balance = v;
    }

    public void setPaymentStatus(String v) {
        this.paymentStatus = v;
    }

    public int getSaleId() {
        return saleId;
    }

    public long getCustomerCnic() {
        return customerCnic;
    }

    public int getCarId() {
        return carId;
    }

    public long getDriverCnic() {
        return driverCnic;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getRentalType() {
        return rentalType;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public double getBalance() {
        return balance;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public boolean isOverdue() {
        return endDate != null && endDate.isBefore(LocalDate.now())
                && !"Paid".equals(paymentStatus);
    }
}
