package GYM.model;

import java.time.LocalDate;
public class BasicMember extends Member {

    public static final double MONTHLY_FEE = 300.0;

    public BasicMember(int id, String name, String phone,
                       LocalDate joinDate, LocalDate expiryDate) {
        super(id, name, phone, joinDate, expiryDate);
    }

    @Override
    public double calculateRenewalFee() {
        return MONTHLY_FEE;
    }

    @Override
    public String getMemberType() {
        return "BASIC";
    }

    public String getAccessPrivileges() {
        return "Gym Floor Only";
    }
}