package models;

public class Vehicle {
    private int carId;
    private String model;
    private String purchasePrice;
    private double ownershipPercentage;

    public Vehicle(int carId, String model, String purchasePrice, double ownershipPercentage) {
        this.carId = carId;
        this.model = model;
        this.purchasePrice = purchasePrice;
        this.ownershipPercentage = ownershipPercentage;
    }

    public int getCarId() {
        return carId;
    }

    public String getModel() {
        return model;
    }

    public String getPurchasePrice() {
        return purchasePrice;
    }

    public double getOwnershipPercentage() {
        return ownershipPercentage;
    }
}
