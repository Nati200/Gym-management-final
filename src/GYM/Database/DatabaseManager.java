package GYM.Database;

import GYM.exception.MemberNotFoundException;
import GYM.exception.ExpiredMembershipException;
import GYM.model.BasicMember;
import GYM.model.Member;
import GYM.model.PremiumMember;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static final String URL = "jdbc:sqlite:gym.db";
    private Connection connection;

    public DatabaseManager() {
        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:gym.db");

            createTables();

        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
    }

    private void connect() {
        try {
            connection = DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.out.println("[DB] Connection failed: " + e.getMessage());
        }
    }

    private void createTables() {
        String membersTable =
            "CREATE TABLE IF NOT EXISTS members (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT NOT NULL, " +
            "phone TEXT, " +
            "member_type TEXT NOT NULL, " +
            "join_date TEXT NOT NULL, " +
            "expiry_date TEXT NOT NULL, " +
            "has_trainer INTEGER DEFAULT 0)";

        String checkinsTable =
            "CREATE TABLE IF NOT EXISTS checkins (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "member_id INTEGER NOT NULL, " +
            "checkin_time TEXT NOT NULL, " +
            "note TEXT, " +
            "FOREIGN KEY(member_id) REFERENCES members(id))";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(membersTable);
            stmt.execute(checkinsTable);
        } catch (SQLException e) {
            System.out.println("[DB] Table creation error: " + e.getMessage());
        }
    }

    public boolean addMember(Member member) {
        String sql = "INSERT INTO members (name, phone, member_type, join_date, expiry_date, has_trainer) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, member.getName());
            pstmt.setString(2, member.getPhone());
            pstmt.setString(3, member.getMemberType());
            pstmt.setString(4, member.getJoinDate().toString());
            pstmt.setString(5, member.getExpiryDate().toString());

            boolean trainer = (member instanceof PremiumMember)
                    && ((PremiumMember) member).hasPersonalTrainer();
            pstmt.setInt(6, trainer ? 1 : 0);

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("[DB] Add member error: " + e.getMessage());
            return false;
        }
    }

    public List<Member> getAllMembers() {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM members ORDER BY id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                members.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("[DB] Fetch error: " + e.getMessage());
        }
        return members;
    }

    public Member getMemberById(int id) throws MemberNotFoundException {
        String sql = "SELECT * FROM members WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            } else {
                throw new MemberNotFoundException(id); // Cleaned up
            }
        } catch (SQLException e) {
            throw new MemberNotFoundException(id); // Cleaned up
        }
    }

    public boolean renewMembership(int id, LocalDate newExpiry) {
        String sql = "UPDATE members SET expiry_date = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newExpiry.toString());
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[DB] Renew error: " + e.getMessage());
            return false;
        }
    }

    public boolean checkIn(int memberId, String note) {
        String sql = "INSERT INTO checkins (member_id, checkin_time, note) VALUES (?, ?, ?)";
        String time = LocalDate.now() + " " + LocalTime.now().toString().substring(0, 8);
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, memberId);
            pstmt.setString(2, time);
            pstmt.setString(3, note);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("[DB] Check-in error: " + e.getMessage());
            return false;
        }
    }

    public List<String> getCheckInHistory(int memberId) {
        List<String> history = new ArrayList<>();
        String sql = "SELECT checkin_time, note FROM checkins WHERE member_id = ? ORDER BY checkin_time DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, memberId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String note = rs.getString("note");
                String entry = rs.getString("checkin_time");
                if (note != null && !note.isEmpty()) entry += "  [" + note + "]";
                history.add(entry);
            }
        } catch (SQLException e) {
            System.out.println("[DB] History error: " + e.getMessage());
        }
        return history;
    }
    public List<Member> getExpiringSoon(int days) {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM members WHERE expiry_date <= ? AND expiry_date >= ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, LocalDate.now().plusDays(days).toString());
            pstmt.setString(2, LocalDate.now().toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                members.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("[DB] Expiry query error: " + e.getMessage());
        }
        return members;
    }

    private Member mapRow(ResultSet rs) throws SQLException {
        int id           = rs.getInt("id");
        String name      = rs.getString("name");
        String phone     = rs.getString("phone");
        String type      = rs.getString("member_type");
        LocalDate join   = LocalDate.parse(rs.getString("join_date"));
        LocalDate expiry = LocalDate.parse(rs.getString("expiry_date"));
        boolean trainer  = rs.getInt("has_trainer") == 1;

        if ("BASIC".equals(type)) {
            return new BasicMember(id, name, phone, join, expiry);
        } else {
            return new PremiumMember(id, name, phone, join, expiry, trainer);
        }
    }

    public void close() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException e) {
            System.out.println("[DB] Close error: " + e.getMessage());
        }
    }
}