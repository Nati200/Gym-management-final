package GYM.model;

import java.time.LocalDate;

public class PremiumMember extends Member {

    public static final double MONTHLY_FEE = 600.0;
    private boolean hasPersonalTrainer;

    public PremiumMember(int id, String name, String phone,
                         LocalDate joinDate, LocalDate expiryDate,
                         boolean hasPersonalTrainer) {
        super(id, name, phone, joinDate, expiryDate);
        this.hasPersonalTrainer = hasPersonalTrainer;
    }

    @Override
    public double calculateRenewalFee() {
        if (hasPersonalTrainer) {
            return MONTHLY_FEE + 200.0; // extra charge
        }
        return MONTHLY_FEE;
    }

    @Override
    public String getMemberType() {
        return "PREMIUM";
    }

    public boolean hasPersonalTrainer()                     { return hasPersonalTrainer; }
    public void setHasPersonalTrainer(boolean val)          { this.hasPersonalTrainer = val; }

    public String getAccessPrivileges() {
        return "Gym Floor + Pool + Sauna"
                + (hasPersonalTrainer ? " + Personal Trainer" : "");
    }
}