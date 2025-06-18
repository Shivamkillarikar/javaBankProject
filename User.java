public class User {
    private int id;
    private String username;
    private String passwordHash;
    private double balance;
    private int failedAttempts;

    public User(int id, String username, String passwordHash, double balance, int failedAttempts) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.balance = balance;
        this.failedAttempts = failedAttempts;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public double getBalance() { return balance; }
    public int getFailedAttempts() { return failedAttempts; }

    public void setBalance(double balance) { this.balance = balance; }
}