package com.vaultsys;

import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static User currentUser = null;

    private static final AuthService authService = new AuthService();
    private static final BankingService bankingService = new BankingService();
    private static final AdminService adminService = new AdminService();
    private static final SimulationService simulationService = new SimulationService();

    public static void main(String[] args) {
        System.out.println("Welcome to VaultSys Banking (Student Edition)");

        while (true) {
            if (currentUser == null) {
                showAuthMenu();
            } else {
                showMainMenu();
            }
        }
    }

    private static void showAuthMenu() {
        System.out.println("\n--- Auth Menu ---");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.print("Choose: ");

        String choice = scanner.nextLine();
        switch (choice) {
            case "1":
                System.out.print("Username: ");
                String loginUser = scanner.nextLine();
                System.out.print("Password: ");
                String loginPass = scanner.nextLine();
                currentUser = authService.login(loginUser, loginPass);
                if (currentUser != null) {
                    System.out.println("Login successful! Welcome " + currentUser.getUsername());
                } else {
                    System.out.println("Login failed.");
                }
                break;
            case "2":
                System.out.print("Username: ");
                String regUser = scanner.nextLine();
                System.out.print("Password: ");
                String regPass = scanner.nextLine();
                System.out.print("Full Name: ");
                String regName = scanner.nextLine();
                if (authService.register(regUser, regPass, regName)) {
                    System.out.println("Registration successful! Please login.");
                } else {
                    System.out.println("Registration failed (Username might be taken).");
                }
                break;
            case "3":
                System.out.println("Goodbye!");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private static void showMainMenu() {
        System.out.println("\n--- Main Menu (" + currentUser.getUsername() + ") ---");
        System.out.println("1. View Balance");
        System.out.println("2. Deposit");
        System.out.println("3. Withdraw");
        System.out.println("4. Transfer");
        System.out.println("5. Transaction History");
        System.out.println("6. Logout");

        if ("ADMIN".equals(currentUser.getRole())) {
            System.out.println("7. [ADMIN] View All Users");
            System.out.println("8. [ADMIN] Run Simulation (10k txns)");
        }

        System.out.print("Choose: ");
        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                bankingService.viewBalance(currentUser);
                break;
            case "2":
                System.out.print("Amount to deposit: ");
                try {
                    double depAmount = Double.parseDouble(scanner.nextLine());
                    if (bankingService.deposit(currentUser, depAmount)) {
                        System.out.println("Deposit successful.");
                    } else {
                        System.out.println("Deposit failed.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid amount.");
                }
                break;
            case "3":
                System.out.print("Amount to withdraw: ");
                try {
                    double withAmount = Double.parseDouble(scanner.nextLine());
                    if (bankingService.withdraw(currentUser, withAmount)) {
                        System.out.println("Withdrawal successful.");
                    } else {
                        System.out.println("Withdrawal failed (Insufficient funds?).");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid amount.");
                }
                break;
            case "4":
                System.out.print("Target Username: ");
                String targetUser = scanner.nextLine();
                System.out.print("Amount to transfer: ");
                try {
                    double transAmount = Double.parseDouble(scanner.nextLine());
                    if (bankingService.transfer(currentUser, targetUser, transAmount)) {
                        System.out.println("Transfer successful.");
                    } else {
                        System.out.println("Transfer failed.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid amount.");
                }
                break;
            case "5":
                bankingService.viewHistory(currentUser);
                break;
            case "6":
                currentUser = null;
                System.out.println("Logged out.");
                break;
            case "7":
                if ("ADMIN".equals(currentUser.getRole())) {
                    adminService.viewAllUsers();
                } else {
                    System.out.println("Access Denied.");
                }
                break;
            case "8":
                if ("ADMIN".equals(currentUser.getRole())) {
                    simulationService.runSimulation();
                } else {
                    System.out.println("Access Denied.");
                }
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }
}
