import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        UserService service = new UserService();
        User currentUser = null;

        while (true) {
            if (currentUser == null) {
                System.out.println("1. Register\n2. Login\n3. Exit");
                int choice = sc.nextInt();
                sc.nextLine();

                if (choice == 1) {
                    System.out.print("Enter username: ");
                    String username = sc.nextLine();
                    System.out.print("Enter password: ");
                    String password = sc.nextLine();
                    service.register(username, password);
                } else if (choice == 2) {
                    System.out.print("Enter username: ");
                    String username = sc.nextLine();
                    System.out.print("Enter password: ");
                    String password = sc.nextLine();
                    currentUser = service.login(username, password);
                } else {
                    break;
                }
            } else {
                System.out.println("1. Deposit\n2. Withdraw\n3. Check Balance\n4. Logout\n5. Show Transaction History");
                int choice = sc.nextInt();

                switch (choice) {
                    case 1 -> {
                        System.out.print("Enter amount to deposit: ");
                        double amount = sc.nextDouble();
                        service.deposit(currentUser, amount);
                    }
                    case 2 -> {
                        System.out.print("Enter amount to withdraw: ");
                        double amount = sc.nextDouble();
                        service.withdraw(currentUser, amount);
                    }
                    case 3 -> service.checkBalance(currentUser);
                    case 4 -> currentUser = null;
                    case 5 -> service.showTransactionHistory(currentUser);
                }
            }
        }

        sc.close();
    }
}
