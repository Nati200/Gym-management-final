package GYM.util;

import GYM.model.Member;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


public class ReportGenerator {

    private static final String REPORT_DIR = "reports/";

    public ReportGenerator() {
        // Chapter 6: File operations
        File dir = new File(REPORT_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    // Chapter 6: Writing to file using BufferedWriter + FileWriter
    public void generateMembershipReport(List<Member> members) {
        String filename = REPORT_DIR + "membership_report_" + LocalDate.now() + ".txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {

            String header = "GYM MEMBERSHIP REPORT — Generated: "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            writer.write("=================================================");
            writer.newLine();
            writer.write(header);
            writer.newLine();
            writer.write("=================================================");
            writer.newLine();

            int basic = 0, premium = 0, expired = 0;

            for (Member m : members) {
                writer.write(m.toString());
                writer.newLine();
                if ("BASIC".equals(m.getMemberType()))   basic++;
                else                                     premium++;
                if (m.isExpired())                       expired++;
            }
            writer.newLine();
            writer.write("-------------------------------------------------");
            writer.newLine();
            writer.write("SUMMARY");
            writer.newLine();
            writer.write("  Total Members   : " + members.size());
            writer.newLine();
            writer.write("  Basic           : " + basic);
            writer.newLine();
            writer.write("  Premium         : " + premium);
            writer.newLine();
            writer.write("  Expired         : " + expired);
            writer.newLine();
            writer.write("=================================================");
            writer.newLine();

            System.out.println("Report saved → " + filename);

        } catch (IOException e) {
            System.out.println("Error writing report: " + e.getMessage());
        }
    }

    // Chapter 6: Appending to file using FileWriter(file, true)
    public void generateAttendanceLog(int memberId, String memberName,
                                      List<String> checkIns) {
        String filename = REPORT_DIR + "attendance_member_" + memberId + ".txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {

            writer.write("=================================================");
            writer.newLine();
            writer.write("ATTENDANCE LOG — " + memberName + " (ID: " + memberId + ")");
            writer.newLine();
            writer.write("Generated: " + LocalDate.now());
            writer.newLine();
            writer.write("=================================================");
            writer.newLine();

            if (checkIns.isEmpty()) {
                writer.write("No check-ins recorded.");
                writer.newLine();
            } else {
                for (String entry : checkIns) {
                    writer.write("  > " + entry);
                    writer.newLine();
                }
            }

            writer.write("Total Check-ins: " + checkIns.size());
            writer.newLine();
            writer.write("=================================================");
            writer.newLine();

            System.out.println("Attendance log saved → " + filename);

        } catch (IOException e) {
            System.out.println("Error writing attendance log: " + e.getMessage());
        }
    }

    // Chapter 6: Reading from file using BufferedReader + FileReader
    public void readReport(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + filename);
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }
}