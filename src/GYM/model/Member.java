package GYM.model;

import java.time.LocalDate;

public abstract class Member {

    private int id;
    private String name;
    private String phone;
    private LocalDate joinDate;
    private LocalDate expiryDate;

    private static int sessionRegistrations = 0;
    public Member(int id, String name, String phone,
                  LocalDate joinDate, LocalDate expiryDate) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.joinDate = joinDate;
        this.expiryDate = expiryDate;
    }

    public abstract double calculateRenewalFee();
    public abstract String getMemberType();

    public static int getSessionRegistrations() {
        return sessionRegistrations;
    }

    public static void incrementRegistrations() {
        sessionRegistrations++;
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }

    public int getId()             { return id; }
    public String getName()        { return name; }
    public String getPhone()       { return phone; }
    public LocalDate getJoinDate() { return joinDate; }
    public LocalDate getExpiryDate(){ return expiryDate; }

    public void setId(int id)                       { this.id = id; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    @Override
    public String toString() {
        return String.format("ID: %-4d | %-20s | %-7s | Expires: %-12s | %s",
                id, name, getMemberType(), expiryDate,
                isExpired() ? "EXPIRED" : "ACTIVE");
    }
}