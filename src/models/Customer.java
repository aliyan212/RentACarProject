package models;

public class Customer {
    private long cnic;
    private String name;
    private String address;
    private long phone;
    private String license;

    public Customer(long cnic, String name, String address, long phone, String license) {
        this.cnic = cnic;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.license = license;
    }

    public long getCnic() {
        return cnic;
    }

    public String getName() {
        return name;
    }

    public long getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getLicense() {
        return license;
    }
}
