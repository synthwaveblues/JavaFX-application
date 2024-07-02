package com.example.jpo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RegisterController {

    @FXML private TextField usernameField; // TextField for username input
    @FXML private PasswordField passwordField; // PasswordField for password input
    @FXML private Label messageLabel; // Label for displaying messages

    private static final String USERS_FILE = "src/main/resources/com/example/jpo/users.txt"; // File path for storing user credentials

    @FXML
    protected void registerUser() throws IOException, NoSuchAlgorithmException {
        String username = usernameField.getText(); // Get username from input field
        String password = passwordField.getText(); // Get password from input field

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Username and password cannot be empty."); // Display error if username or password is empty
            return;
        }

        if (isUsernameTaken(username)) {
            messageLabel.setText("Username is already taken."); // Display error if username is already taken
            return;
        }

        String hashedPassword = hashPassword(password); // Hash password using SHA-256 algorithm

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE, true))) {
            writer.write(username + ":" + hashedPassword); // Write username and hashed password to file
            writer.newLine(); // Move to the next line
        }

        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close(); // Close registration window after successful registration
    }

    // Method to check if username is already taken
    private boolean isUsernameTaken(String username) throws IOException {
        if (!Files.exists(Paths.get(USERS_FILE))) {
            return false; // Return false if user file does not exist
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String existingUsername = line.split(":")[0]; // Get username from each line
                if (existingUsername.equals(username)) {
                    return true; // Return true if username already exists
                }
            }
        }

        return false; // Return false if username does not exist
    }

    // Method to hash password using SHA-256 algorithm
    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256"); // Create instance of SHA-256 hash function
        byte[] hash = md.digest(password.getBytes()); // Generate hash from password bytes
        StringBuilder hexString = new StringBuilder(); // StringBuilder to store hexadecimal hash

        // Convert bytes to hexadecimal format
        for (byte b : hash) {
            hexString.append(String.format("%02x", b));
        }

        return hexString.toString(); // Return hashed password as hexadecimal string
    }
}
