package GYM;

import GYM.exception.MemberNotFoundException;
import GYM.Database.DatabaseManager;
import GYM.exception.ExpiredMembershipException;
import GYM.model.BasicMember;
import GYM.model.Member;
import GYM.model.PremiumMember;
import GYM.util.ReportGenerator;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static DatabaseManager db;
    private static ReportGenerator reportGen;
    private static Scanner scanner;

    public static void main(String[] args) throws Exception{

        db        = new DatabaseManager();
        reportGen = new ReportGenerator();
        scanner   = new Scanner(System.in);

        System.out.println("     WELCOME TO GYM MANAGER      ");
        System.out.println("==========================================");

        boolean running = true;
        while (running) {
            printMenu();
            int choice = getIntInput("Enter choice: ");

            switch (choice) {
                case 1:  registerMember();     break;
                case 2:  viewAllMembers();     break;
                case 3:  checkInMember();      break;
                case 4:  renewMembership();    break;
                case 5:  viewCheckInHistory(); break;
                case 6:  viewExpiringSoon();   break;
                case 7:  generateReport();     break;
                case 0:
                    System.out.println("Goodbye!");
                    db.close();
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }

        scanner.close();
    }

    static void printMenu() {
        System.out.println("              MAIN MENU");
        System.out.println("==========================================");
        System.out.println(" 1. Register New Member");
        System.out.println(" 2. View All Members");
        System.out.println(" 3. Check In Member");
        System.out.println(" 4. Renew Membership");
        System.out.println(" 5. View Check-In History");
        System.out.println(" 6. View Expiring Soon (7 days)");
        System.out.println(" 7. Generate Membership Report");
        System.out.println(" 0. Exit");
        System.out.println("==========================================");
    }


    static void registerMember() {
        System.out.println("\n--- REGISTER NEW MEMBER ---");
        System.out.print("Full Name : ");
        String name = scanner.nextLine().trim();
        System.out.print("Phone     : ");
        String phone = scanner.nextLine().trim();

        System.out.println("Member Type:");
        System.out.println("  1. Basic   — 300 ETB/month (Gym Floor Only)");
        System.out.println("  2. Premium — 600 ETB/month (Gym + Pool + Sauna)");
        int type = getIntInput("Choose: ");

        LocalDate today  = LocalDate.now();
        LocalDate expiry = today.plusMonths(1);

        Member member;

        if (type == 1) {
            member = new BasicMember(0, name, phone, today, expiry);

        } else if (type == 2) {
            System.out.print("Add personal trainer? (+200 ETB) [y/n]: ");
            boolean trainer = scanner.nextLine().trim().equalsIgnoreCase("y");

            member = new PremiumMember(0, name, phone, today, expiry, trainer);

        } else {
            System.out.println("Invalid type. Registration cancelled.");
            return;
        }

        System.out.printf("Monthly Fee: %.2f ETB%n", member.calculateRenewalFee());

        if (db.addMember(member)) {
            Member.incrementRegistrations();
            System.out.println("Member registered successfully!");
            System.out.println("Members registered this session: "
                    + Member.getSessionRegistrations());
        } else {
            System.out.println("Registration failed.");
        }
    }

    static void viewAllMembers() {
        System.out.println("\n--- ALL MEMBERS ---");
        List<Member> members = db.getAllMembers();

        if (members.isEmpty()) {
            System.out.println("No members registered yet.");
            return;
        }

        for (Member m : members) {
            System.out.println(m);
        }
        System.out.println("\nTotal: " + members.size() + " member(s)");
    }

    static void checkInMember() throws MemberNotFoundException {
        System.out.println("\n--- MEMBER CHECK-IN ---");
        int id = getIntInput("Enter Member ID: ");

        try {
            Member member = db.getMemberById(id);

            if (member.isExpired()) {
                throw new ExpiredMembershipException(
                        "Membership expired on " + member.getExpiryDate()
                                + ". Please renew first.", id);
            }

            System.out.print("Note (optional, Enter to skip): ");
            String note = scanner.nextLine().trim();

            if (db.checkIn(id, note)) {
                System.out.println("Check-in successful! Welcome, " + member.getName() + "!");
            }

         } catch (MemberNotFoundException _) {
    } catch (ExpiredMembershipException e) {
        System.out.println("Membership has expired: " + e.getMessage());
    }
    }

    static void renewMembership() {
        System.out.println("\n--- RENEW MEMBERSHIP ---");
        int id = getIntInput("Enter Member ID: ");

        try {
            Member member = db.getMemberById(id);
            System.out.println("Member : " + member.getName());
            System.out.println("Current expiry: " + member.getExpiryDate());

            System.out.printf("Renewal fee: %.2f ETB%n", member.calculateRenewalFee());

            System.out.print("Confirm renewal? [y/n]: ");
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {

                LocalDate base      = member.isExpired() ? LocalDate.now() : member.getExpiryDate();
                LocalDate newExpiry = base.plusMonths(1);

                if (db.renewMembership(id, newExpiry)) {
                    System.out.println("Membership renewed until: " + newExpiry);
                }
            } else {
                System.out.println("Renewal cancelled.");
            }

        } catch (MemberNotFoundException e) { // 👈 Make sure there is no dot prefix here!
            System.out.println("⚠️ Member wasn't found: " + e.getMessage());

        }
    }

    static void viewCheckInHistory() {
        System.out.println("\n--- CHECK-IN HISTORY ---");
        int id = getIntInput("Enter Member ID: ");

        try {
            Member member       = db.getMemberById(id);
            List<String> history = db.getCheckInHistory(id);

            System.out.println("History for: " + member.getName());

            if (history.isEmpty()) {
                System.out.println("No check-ins recorded.");
            } else {
                history.forEach(h -> System.out.println("  > " + h));
            }

            System.out.print("\nExport to file? [y/n]: ");
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                reportGen.generateAttendanceLog(id, member.getName(), history);
            }

        } catch (MemberNotFoundException e) {
        System.out.println("⚠️ Member wasn't found: " + e.getMessage());

    }
    }


    static void viewExpiringSoon() {
        System.out.println("\n--- EXPIRING IN NEXT 7 DAYS ---");
        List<Member> members = Main.db.getExpiringSoon(7);

        if (members.isEmpty()) {
            System.out.println("No memberships expiring in the next 7 days.");
        } else {
            for (Member m : members) {
                System.out.printf("  %s | Expires: %s | Renewal: %.2f ETB%n",
                        m.getName(), m.getExpiryDate(), m.calculateRenewalFee());
            }
        }
    }

    static void generateReport() {
        System.out.println("\n--- GENERATING REPORT ---");
        List<Member> members = Main.db.getAllMembers();

        if (members.isEmpty()) {
            System.out.println("No members to report.");
            return;
        }

        Main.reportGen.generateMembershipReport(members);
    }

    static int getIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(Main.scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }
}
