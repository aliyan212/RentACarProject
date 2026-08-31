package models;

import java.time.LocalDate;

public class Payment {
    private int paymentId;
    private int saleId;
    private double moneyPaid;
    private LocalDate paymentDate;

    public Payment(int paymentId, int saleId, double moneyPaid, LocalDate paymentDate) {
        this.paymentId = paymentId;
        this.saleId = saleId;
        this.moneyPaid = moneyPaid;
        this.paymentDate = paymentDate;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public int getSaleId() {
        return saleId;
    }

    public double getMoneyPaid() {
        return moneyPaid;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }
}
