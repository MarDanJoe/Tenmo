


package com.techelevator.tenmo;

import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.ConsoleService;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.sql.SQLOutput;
import java.util.List;
import java.math.BigDecimal;

public class App {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String API_BASE_URL = "http://localhost:8080/";

    private final ConsoleService consoleService = new ConsoleService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);

    private AuthenticatedUser currentUser;

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    private void run() {
        consoleService.printGreeting();
        loginMenu();
        if (currentUser != null) {
            mainMenu();
        }
    }

    private void loginMenu() {
        int menuSelection = -1;
        while (menuSelection != 0 && currentUser == null) {
            consoleService.printLoginMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                handleRegister();
            } else if (menuSelection == 2) {
                handleLogin();
            } else if (menuSelection != 0) {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }


    private void handleRegister() {
        System.out.println("Please register a new user account");
        UserCredentials credentials = consoleService.promptForCredentials();
        if (authenticationService.register(credentials)) {
            System.out.println("Registration successful. You can now login.");
        } else {
            consoleService.printErrorMessage();
        }
    }

    private void handleLogin() {
        UserCredentials credentials = consoleService.promptForCredentials();
        currentUser = authenticationService.login(credentials);
        if (currentUser == null) {
            consoleService.printErrorMessage();
        }
    }

    private void mainMenu() {
        int menuSelection = -1;
        while (menuSelection != 0) {
            consoleService.printMainMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                viewCurrentBalance();
            } else if (menuSelection == 2) {
                viewTransferHistory();
            } else if (menuSelection == 3) {
                viewPendingRequests();
            } else if (menuSelection == 4) {
                sendBucks();
            } else if (menuSelection == 5) {
                requestBucks();
            } else if (menuSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
            }
            consoleService.pause();
        }
    }


    private void viewCurrentBalance() {
        BigDecimal balance = authenticationService.getBalance(currentUser);
        System.out.println("The current balance is: $" + balance);
    }

    private String getAuthToken() {
        return currentUser != null ? currentUser.getToken() : null;

    }

    private void viewTransferHistory() {
        try {
            List<TransferDTO> transferHistory = authenticationService.getTransferHistory(currentUser);

            if (transferHistory == null || transferHistory.isEmpty()) {
                System.out.println("No transfer history available.");
                return;
            }

            System.out.println("-------------------------------------------");
            System.out.println("Transfers");
            System.out.println("ID          From/To                 Amount");
            System.out.println("-------------------------------------------");

            for (TransferDTO transfer : transferHistory) {
                String fromTo = transfer.getAccountFrom() == currentUser.getUser().getId() ? "To: " + transfer.getAccountTo() : "From: " + transfer.getAccountFrom();
                String output = transfer.getTransferId() + " " + fromTo + " $" + String.format("%.2f", transfer.getTransferAmount());
                System.out.println(output);

            }


            System.out.println("-------------------------------------------");
        } catch (Exception e) {
            System.out.println("Error retrieving transfer history: " + e.getMessage());
        }
    }

    private void viewPendingRequests() {
        try {
            List<TransferDTO> pendingRequests = authenticationService.getPendingRequests(currentUser);

            if (pendingRequests == null || pendingRequests.isEmpty()) {
                System.out.println("No pending transfer requests.");
                return;
            }

            System.out.println("-------------------------------------------");
            System.out.println("Pending Transfers");
            System.out.println("ID          To                   Amount");
            System.out.println("-------------------------------------------");

            for (TransferDTO transfer : pendingRequests) {
                String toUser = transfer.getAccountTo() != null && transfer.getAccountTo().equals(currentUser.getUser().getId()) ? "You" : (transfer.getAccountTo() != null ? transfer.getAccountTo().toString() : "N/A");
                System.out.printf("%-12s%-20s$ %.2f%n",
                        transfer.getTransferId() != null ? transfer.getTransferId() : "N/A",
                        toUser,
                        transfer.getTransferAmount() != null ? transfer.getTransferAmount() : 0.0
                );
            }

            System.out.println("-------------------------------------------");
        } catch (Exception e) {
            System.out.println("Error retrieving pending requests: " + e.getMessage());
        }
    }

    private void sendBucks() {
        try {

            List<User> users = authenticationService.getAllUsers(currentUser);
            System.out.println("Users:");
            for (User user : users) {
                if (user.getId() !=(currentUser.getUser().getId())) {
                    System.out.println(user.getId() + ": " + user.getUsername());
                }
            }


            int recipientId = consoleService.promptForInt("Enter the ID of the user you want to send TE bucks to: ");


            if (recipientId == currentUser.getUser().getId()) {
                System.out.println("You cannot send money to yourself.");
               // return;
            }

            Double amount = consoleService.promptForBigDecimal("Enter amount to send: ").doubleValue();



            TransferDTO transfer = new TransferDTO();
            transfer.setAccountFrom(currentUser.getUser().getId());
            transfer.setAccountTo(recipientId);
            transfer.setTransferAmount(amount);
            transfer.setTypeId(2);
            transfer.setStatusId(2);



            TransferDTO completedTransfer = authenticationService.sendBucks(currentUser, transfer);


            //System.out.println(transfer.getTransferId());
            System.out.println("Transfer successful! Transfer ID: " + (completedTransfer.getTransferId() != null ? completedTransfer.getTransferId() : "N/A"));
            //List<TransferType> transferTypes = authenticationService.getAllTransferTypes(currentUser);
            //List<TransferStatus> transferStatuses = authenticationService.getAllTransferStatuses(currentUser);
            //System.out.println("Transfer successful! Transfer ID: " + (completedTransfer.getTransferId() != null ? completedTransfer.getTransferId() : "N/A"));
        } catch (Exception e) {
            System.out.println("Broke Bitch");
        }
    }
        private void requestBucks () {
            // TODO Auto-generated method stub

        }


}
