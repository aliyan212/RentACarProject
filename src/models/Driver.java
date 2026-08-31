package models;

public class Driver {
    private long cnic;
    private String name;
    private long license;
    private long phone;
    private String status;

    public Driver(long cnic, String name, long license, long phone, String status) {
        this.cnic = cnic;
        this.name = name;
        this.license = license;
        this.phone = phone;
        this.status = status;
    }

    public long getCnic() {
        return cnic;
    }

    public String getName() {
        return name;
    }

    public long getLicense() {
        return license;
    }

    public long getPhone() {
        return phone;
    }

    public String getStatus() {
        return status;
    }
}
