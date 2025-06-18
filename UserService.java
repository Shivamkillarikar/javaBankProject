import java.security.MessageDigest;
import java.sql.*;

public class UserService {
    public void register(String username, String password) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, hashPassword(password));
            stmt.executeUpdate();
            System.out.println("User registered successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public User login(String username, String password) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String hash = rs.getString("password_hash");
                double balance = rs.getDouble("balance");
                int failedAttempts = rs.getInt("failed_attempts");

                if (failedAttempts >= 3) {
                    System.out.println("Account locked.");
                    return null;
                }

                if (hash.equals(hashPassword(password))) {
                    resetFailedAttempts(id);
                    return new User(id, username, hash, balance, failedAttempts);
                } else {
                    incrementFailedAttempts(id);
                    System.out.println("Invalid credentials.");
                }
            } else {
                System.out.println("User not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

public void deposit(User user, double amount) {
    try (Connection conn = DBConnection.getConnection()) {
        String sql = "UPDATE users SET balance = balance + ? WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setDouble(1, amount);
        stmt.setInt(2, user.getId());
        stmt.executeUpdate();

        // Fetch updated balance
        PreparedStatement fetch = conn.prepareStatement("SELECT balance FROM users WHERE id = ?");
        fetch.setInt(1, user.getId());
        ResultSet rs = fetch.executeQuery();
        if (rs.next()) {
            user.setBalance(rs.getDouble("balance"));
        }

        logTransaction(user.getId(), "DEPOSIT", amount, null);
        System.out.println("Deposited ₹" + amount);
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

public void withdraw(User user, double amount) {
    if (user.getBalance() < amount) {
        System.out.println("Insufficient funds.");
        return;
    }
    try (Connection conn = DBConnection.getConnection()) {
        String sql = "UPDATE users SET balance = balance - ? WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setDouble(1, amount);
        stmt.setInt(2, user.getId());
        stmt.executeUpdate();

        // Fetch updated balance
        PreparedStatement fetch = conn.prepareStatement("SELECT balance FROM users WHERE id = ?");
        fetch.setInt(1, user.getId());
        ResultSet rs = fetch.executeQuery();
        if (rs.next()) {
            user.setBalance(rs.getDouble("balance"));
        }

        logTransaction(user.getId(), "WITHDRAW", amount, null);
        System.out.println("Withdrew ₹" + amount);
    } catch (SQLException e) {
        e.printStackTrace();
    }
}


    public void checkBalance(User user) {
        System.out.println("Current Balance: ₹" + user.getBalance());
    }

    public void showTransactionHistory(User user) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM transactions WHERE user_id = ? ORDER BY timestamp DESC LIMIT 10";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, user.getId());
            ResultSet rs = stmt.executeQuery();

            System.out.println("Recent Transactions:");
            while (rs.next()) {
                System.out.printf("[%s] %s ₹%.2f\n", rs.getString("timestamp"), rs.getString("type"), rs.getDouble("amount"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void transferFunds(User sender, String recipientUsername, double amount) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            String findSql = "SELECT * FROM users WHERE username = ?";
            PreparedStatement findStmt = conn.prepareStatement(findSql);
            findStmt.setString(1, recipientUsername);
            ResultSet rs = findStmt.executeQuery();

            if (rs.next()) {
                int recipientId = rs.getInt("id");

                if (sender.getBalance() < amount) {
                    System.out.println("Insufficient funds.");
                    return;
                }

                PreparedStatement deduct = conn.prepareStatement("UPDATE users SET balance = balance - ? WHERE id = ?");
                deduct.setDouble(1, amount);
                deduct.setInt(2, sender.getId());
                deduct.executeUpdate();

                PreparedStatement add = conn.prepareStatement("UPDATE users SET balance = balance + ? WHERE id = ?");
                add.setDouble(1, amount);
                add.setInt(2, recipientId);
                add.executeUpdate();

                logTransaction(sender.getId(), "TRANSFER_OUT", amount, recipientId);
                logTransaction(recipientId, "TRANSFER_IN", amount, sender.getId());

                conn.commit();
                System.out.println("Transferred ₹" + amount);
            } else {
                System.out.println("Recipient not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void incrementFailedAttempts(int userId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("UPDATE users SET failed_attempts = failed_attempts + 1 WHERE id = ?");
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    private void resetFailedAttempts(int userId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("UPDATE users SET failed_attempts = 0 WHERE id = ?");
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    private void logTransaction(int userId, String type, double amount, Integer refUserId) {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO transactions (user_id, type, amount, reference_user_id) VALUES (?, ?, ?, ?)");
            stmt.setInt(1, userId);
            stmt.setString(2, type);
            stmt.setDouble(3, amount);
            if (refUserId != null) stmt.setInt(4, refUserId);
            else stmt.setNull(4, java.sql.Types.INTEGER);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
