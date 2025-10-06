package pedantry_System.entity;

import java.util.List;
import java.util.Scanner;

import PedantrySystem.dao.DatabaseConnection;
import pedantry_System.util.DocumentDAO;
import pedantry_System.util.UserDAO;

public class PedantrySystem {
    private static final Scanner scanner = new Scanner(System.in);
    private static DatabaseConnection db = new DatabaseConnection();
    private static UserDAO userDAO = new UserDAO(db);
    private static DocumentDAO docDAO = new DocumentDAO(db);
    private static User currentUser  = null;

    public static void main(String[] args) {
        System.out.println("=== Welcome to Pedantry System - Knowledge Management System ===");
        System.out.println("This is an Intranet-style CLI for uploading/searching technical documents.");
        boolean running = true;
        while (running) {
            if (currentUser  == null) {
                showGuestMenu();
            } else {
                if ("ADMIN".equals(currentUser .getRole())) {
                    showAdminMenu();
                } else {
                    showUserMenu();
                }
            }
            // Consume any leftover newline to prevent input issues
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }
        }
        System.out.println("Goodbye!");
    }

    // Guest Menu (Public Access - Read Only)
    private static void showGuestMenu() {
        System.out.println("\n--- Guest Menu ---");
        System.out.println("1. Search Documents (Read Access)");
        System.out.println("2. Login");
        System.out.println("3. Register");
        System.out.println("4. Exit");
        System.out.print("Choose an option: ");
        int choice = getIntInput(1, 4);
        switch (choice) {
            case 1:
                searchDocuments();
                break;
            case 2:
                login();
                break;
            case 3:
                register();
                break;
            case 4:
                System.exit(0);
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    // User Menu (Logged-in Regular User)
    private static void showUserMenu() {
        System.out.println("\n--- User Menu (Logged in as: " + currentUser .getUsername() + " | Role: " + currentUser .getRole() + ") ---");
        System.out.println("1. View/Change Profile (Details & Password)");
        System.out.println("2. Upload Document");
        System.out.println("3. Download/View Documents");
        System.out.println("4. Search Documents");
        System.out.println("5. View Reports (My Uploads)");
        System.out.println("6. Logout");
        System.out.print("Choose an option: ");
        int choice = getIntInput(1, 6);
        switch (choice) {
            case 1:
                manageProfile();
                break;
            case 2:
                uploadDocument();
                break;
            case 3:
                viewDocuments();
                break;
            case 4:
                searchDocuments();
                break;
            case 5:
                showReports();
                break;
            case 6:
                logout();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    // Admin Menu (Logged-in Admin - Extended Features)
    private static void showAdminMenu() {
        System.out.println("\n--- Admin Menu (Logged in as: " + currentUser .getUsername() + ") ---");
        System.out.println("1. View/Change Profile");
        System.out.println("2. Upload Document");
        System.out.println("3. Download/View Documents");
        System.out.println("4. Search Documents");
        System.out.println("5. View Reports (My Uploads)");
        System.out.println("6. View All Users");
        System.out.println("7. Delete Document");
        System.out.println("8. Logout");
        System.out.print("Choose an option: ");
        int choice = getIntInput(1, 8);
        switch (choice) {
            case 1:
                manageProfile();
                break;
            case 2:
                uploadDocument();
                break;
            case 3:
                viewDocuments();
                break;
            case 4:
                searchDocuments();
                break;
            case 5:
                showReports();
                break;
            case 6:
                viewAllUsers();
                break;
            case 7:
                deleteDocument();
                break;
            case 8:
                logout();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    // Search Documents (Keyword Search on Title/Content - Guest/User/Admin)
    private static void searchDocuments() {
        System.out.print("\nEnter search keyword: ");
        String keyword = scanner.nextLine().trim();
        if (keyword.isEmpty()) {
            System.out.println("No keyword entered. Returning to menu.");
            return;
        }
        List<Document> results = docDAO.search(keyword);
        if (results.isEmpty()) {
            System.out.println("No documents found matching '" + keyword + "'.");
        } else {
            System.out.println("\n--- Search Results (" + results.size() + " found) ---");
            for (Document doc : results) {
                System.out.println("ID: " + doc.getId() + " | Title: " + doc.getTitle() +
                                   " | Uploaded: " + doc.getUploadDate() + " | By User ID: " + doc.getUploaderId());
                String preview = doc.getContent().substring(0, Math.min(100, doc.getContent().length()));
                System.out.println("Content Preview: " + preview + (doc.getContent().length() > 100 ? "..." : ""));
                System.out.println("---");
            }
            // Allow viewing full document if logged in
            if (currentUser  != null) {
                System.out.print("Enter document ID to view full content (or 0 to cancel): ");
                int docId = getIntInput(0, Integer.MAX_VALUE);
                if (docId > 0) {
                    viewDocument(docId);
                }
            }
        }
    }

    // Login Functionality
    private static void login() {
        System.out.print("\nUsername: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Username and password cannot be empty.");
            return;
        }
        User user = userDAO.login(username, password);
        if (user != null) {
            currentUser  = user;
            System.out.println("Login successful! Welcome, " + username + ".");
        } else {
            System.out.println("Invalid username or password. Try again.");
        }
    }

    // Register New User
    private static void register() {
        System.out.print("\nUsername: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            System.out.println("All fields are required.");
            return;
        }
        User newUser  = new User(0, username, password, email, "USER", 0);
        if (userDAO.register(newUser )) {
            System.out.println("Registration successful! You can now login.");
        } else {
            System.out.println("Registration failed. Username may already exist.");
        }
    }

    // Manage Profile (View and Update Password/Email)
    private static void manageProfile() {
        User user = userDAO.getById(currentUser .getId());
        if (user == null) {
            System.out.println("User  profile not found.");
            return;
        }
        System.out.println("\n--- Current Profile ---");
        System.out.println("Username: " + user.getUsername());
        System.out.println("Email: " + user.getEmail());
        System.out.println("Role: " + user.getRole());
        System.out.println("Number of Uploads: " + user.getNumUploads());

        System.out.print("\nChange Password? (y/n): ");
        String changePass = scanner.nextLine().trim().toLowerCase();
        if (changePass.startsWith("y")) {
            System.out.print("New Password: ");
            String newPassword = scanner.nextLine().trim();
            if (!newPassword.isEmpty()) {
                user.setPassword(newPassword);
                if (userDAO.update(user)) {
                    System.out.println("Password updated successfully.");
                    currentUser .setPassword(newPassword);  // Update session
                } else {
                    System.out.println("Failed to update password.");
                }
            } else {
                System.out.println("Password cannot be empty.");
            }
        }

        System.out.print("Change Email? (y/n): ");
        String changeEmail = scanner.nextLine().trim().toLowerCase();
        if (changeEmail.startsWith("y")) {
            System.out.print("New Email: ");
            String newEmail = scanner.nextLine().trim();
            if (!newEmail.isEmpty()) {
                user.setEmail(newEmail);
                if (userDAO.update(user)) {
                    System.out.println("Email updated successfully.");
                    currentUser .setEmail(newEmail);  // Update session
                } else {
                    System.out.println("Failed to update email.");
                }
            } else {
                System.out.println("Email cannot be empty.");
            }
        }
    }

    // Upload Document (Title + Multi-line HTML Content)
    private static void uploadDocument() {
        System.out.print("\nTitle: ");
        String title = scanner.nextLine().trim();
        if (title.isEmpty()) {
            System.out.println("Title cannot be empty.");
            return;
        }
        System.out.println("Enter HTML Content (multi-line; end with 'END' on a new line):");
        StringBuilder content = new StringBuilder();
        while (true) {
            String line = scanner.nextLine().trim();
            if ("END".equalsIgnoreCase(line)) {
                break;
            }
            content.append(line).append("\n");
        }
        String htmlContent = content.toString().trim();
        if (htmlContent.isEmpty()) {
            System.out.println("Content cannot be empty.");
            return;
        }
        Document doc = new Document(0, title, htmlContent, currentUser .getId(), null);
        if (docDAO.create(doc)) {
            System.out.println("Document uploaded successfully! ID: " + doc.getId());
        } else {
            System.out.println("Upload failed. Please try again.");
        }
    }

    // View All Documents (List and Select to View)
    private static void viewDocuments() {
        List<Document> docs = docDAO.getAll();
        if (docs.isEmpty()) {
            System.out.println("\nNo documents available.");
            return;
        }
        System.out.println("\n--- All Documents (" + docs.size() + ") ---");
        for (Document doc : docs) {
            System.out.println("ID: " + doc.getId() + " | Title: " + doc.getTitle() +
                               " | Uploaded: " + doc.getUploadDate() + " | By User ID: " + doc.getUploaderId());
        }
        System.out.print("\nEnter document ID to view full content (or 0 to cancel): ");
        int docId = getIntInput(0, Integer.MAX_VALUE);
        if (docId > 0) {
            viewDocument(docId);
        }
    }

    // View Specific Document (Simulate Download/Render)
    private static void viewDocument(int docId) {
        Document doc = docDAO.getById(docId);
        if (doc == null) {
            System.out.println("Document not found (ID: " + docId + ").");
            return;
        }
        System.out.println("\n--- Document Details ---");
        System.out.println("ID: " + doc.getId());
        System.out.println("Title: " + doc.getTitle());
        System.out.println("Uploaded: " + doc.getUploadDate());
        System.out.println("Uploader ID: " + doc.getUploaderId());
        System.out.println("\n--- Content (HTML - Simulated Render) ---");
        System.out.println(doc.getContent());  // In a web app, this would be parsed and rendered as HTML
        System.out.println("--- End of Document ---");
    }

    // Show Personal Reports (Upload Count and List)
    private static void showReports() {
        System.out.println("\n--- Personal Reports ---");
        System.out.println("Total Uploads: " + currentUser .getNumUploads());
        List<Document> myDocs = docDAO.getByUser (currentUser .getId());
        if (myDocs.isEmpty()) {
            System.out.println("No documents uploaded yet.");
        } else {
            System.out.println("Your Documents (" + myDocs.size() + "):");
            for (Document doc : myDocs) {
                System.out.println("ID: " + doc.getId() + " | Title: " + doc.getTitle() + " | Date: " + doc.getUploadDate());
            }
        }
    }

    // View All Users ( Only)
    private static void viewAllUsers() {
        List<User> users = userDAO.getAll();
        if (users.isEmpty()) {
            System.out.println("\nNo users found.");
            return;
        }
        System.out.println("\n--- All Users (" + users.size() + ") ---");
        for (User  user : users) {
            System.out.println("ID: " + user.getId() + " | Username: " + user.getUsername() +
                               " | Email: " + user.getEmail() + " | Role: " + user.getRole() +
                               " | Uploads: " + user.getNumUploads());
        }
    }

    // Delete Document (Admin Only)
    private static void deleteDocument() {
        List<Document> docs = docDAO.getAll();
        if (docs.isEmpty()) {
            System.out.println("\nNo documents to delete.");
            return;
        }
        System.out.println("\n--- Available Documents ---");
        for (Document doc : docs) {
            System.out.println("ID: " + doc.getId() + " | Title: " + doc.getTitle() + " | By: " + doc.getUploaderId());
        }
        System.out.print("Enter document ID to delete (or 0 to cancel): ");
        int docId = getIntInput(0, Integer.MAX_VALUE);
        if (docId > 0) {
            if (docDAO.delete(docId)) {
                System.out.println("Document deleted successfully.");
            } else {
                System.out.println("Failed to delete document (may not exist).");
            }
        }
    }

    // Logout (Clear Session)
    private static void logout() {
        currentUser  = null;
        System.out.println("Logged out successfully. Returning to guest menu.");
    }

    // Helper: Get validated integer input (basic)
    private static int getIntInput() {
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid input. Enter a number: ");
            scanner.next();  // Discard invalid input
        }
        int input = scanner.nextInt();
        scanner.nextLine();  // Consume trailing newline
        return input;
    }

    // Helper: Get validated integer input in a range
    private static int getIntInput(int min, int max) {
        int input;
        do {
            input = getIntInput();
            if (input < min || input > max) {
                System.out.print("Please enter a number between " + min + " and " + max + ": ");
            }
        } while (input < min || input > max);
        return input;
    }

    // Helper: Get non-empty string input (not used directly, but available)
    private static String getStringInput(String prompt) {
        String input;
        do {
            System.out.print(prompt);
            input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Input cannot be empty.");
            }
        } while (input.isEmpty());
        return input;
    }
}
