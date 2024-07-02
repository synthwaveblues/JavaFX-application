package com.example.jpo;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainController {

    @FXML private VBox loginBox; // Login VBox container
    @FXML private TextField loginField; // TextField for login
    @FXML private PasswordField passwordField; // PasswordField for password input
    @FXML private Label messageLabel; // Label for displaying messages
    @FXML private VBox mainBox; // Main application VBox container
    @FXML private TextField symbolField; // TextField for currency symbol input
    @FXML private DatePicker datePicker; // DatePicker for selecting date
    @FXML private Label resultLabel; // Label for displaying general results
    @FXML private Label resultLabelCurrencies; // Label for displaying currency-related results
    @FXML private Label resultLabelWeather; // Label for displaying weather-related results
    @FXML private Label weatherDateTimeLabel; // Label for weather data timestamp
    @FXML private Label weatherTemperatureLabel; // Label for weather temperature
    @FXML private Label weatherConditionLabel; // Label for weather condition

    private static final String USERS_FILE = "src/main/resources/com/example/jpo/users.txt"; // File path for storing user credentials
    private static final String CURRENCIES_DIR = "src/main/resources/com/example/jpo/currencies/"; // Directory path for storing currency data
    private static final String CONFIG_FILE = "src/main/resources/com/example/jpo/config.txt"; // Configuration file path

    private String apiKey; // API key for external services

    @FXML
    private void initialize() {
        loadApiKey(); // Initialize and load API key from config file
    }

    // Method to load API key from configuration file
    private void loadApiKey() {
        Path configPath = Paths.get(CONFIG_FILE);
        try {
            List<String> lines = Files.readAllLines(configPath);
            for (String line : lines) {
                if (line.startsWith("API_KEY=")) {
                    apiKey = line.substring(8).trim(); // Extract and store API key
                    break;
                }
            }
        } catch (IOException e) {
            resultLabelWeather.setText("Failed to load API key."); // Display error message if API key loading fails
        }
    }

    @FXML
    protected void login() throws IOException, NoSuchAlgorithmException {
        String username = loginField.getText(); // Get username from input field
        String password = passwordField.getText(); // Get password from input field

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Username or password cannot be empty."); // Display error if username or password is empty
            return;
        }

        Path usersFilePath = Paths.get(USERS_FILE); // Path to user credentials file
        if (!Files.exists(usersFilePath)) {
            messageLabel.setText("No users registered."); // Display error if no users are registered
            return;
        }

        List<String> users = Files.readAllLines(usersFilePath); // Read all registered users
        String hashedPassword = hashPassword(password); // Hash entered password
        boolean authenticated = false; // Flag to track authentication status

        // Iterate through registered users to verify credentials
        for (String user : users) {
            if (user.equals(username + ":" + hashedPassword)) {
                authenticated = true; // Set authenticated to true if credentials match
                break;
            }
        }

        if (authenticated) {
            messageLabel.setText("Login successful."); // Display success message on successful login
            loginBox.setVisible(false); // Hide login box
            mainBox.setVisible(true); // Show main application box
            fetchWeatherData(); // Fetch weather data on successful login
            startWeatherUpdateTask(); // Start periodic weather update task
        } else {
            messageLabel.setText("Invalid username or password."); // Display error on invalid credentials
        }
    }

    // Method to hash password using SHA-256 algorithm
    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256"); // Create instance of SHA-256 hash function
        byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8)); // Generate hash from password bytes
        StringBuilder hexString = new StringBuilder(); // StringBuilder to store hexadecimal hash

        // Convert bytes to hexadecimal format
        for (byte b : hash) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString(); // Return hashed password as hexadecimal string
    }

    @FXML
    protected void showData() {
        String symbol = symbolField.getText(); // Get currency symbol from input field
        Path filePath = Paths.get(CURRENCIES_DIR + symbol + ".txt"); // Path to currency data file

        if (Files.exists(filePath)) {
            try {
                List<String> lines = Files.readAllLines(filePath); // Read all lines from currency data file
                StringBuilder content = new StringBuilder(); // StringBuilder to store file content

                // Append each line to content StringBuilder
                for (String line : lines) {
                    content.append(line).append("\n");
                }

                // Display currency data in a new stage
                Stage stage = new Stage();
                VBox vbox = new VBox(new Label(content.toString()));
                Scene scene = new Scene(vbox, 300, 200);
                stage.setScene(scene);
                stage.setTitle("Data for " + symbol);
                stage.show();
            } catch (IOException e) {
                resultLabelCurrencies.setText("Error reading the file."); // Display error message on file read error
                e.printStackTrace(); // Print stack trace for debugging
            }
        } else {
            resultLabelCurrencies.setText("Data for the given symbol does not exist."); // Display message if data file does not exist
        }
    }

    @FXML
    protected void fetchCurrencyRate() {
        String symbol = symbolField.getText(); // Get currency symbol from input field
        LocalDate date = datePicker.getValue(); // Get selected date from date picker

        if (symbol.isEmpty() || date == null) {
            resultLabelCurrencies.setText("Symbol and date cannot be empty."); // Display error if symbol or date is empty
            return;
        }

        String formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // Format selected date

        try {
            Path filePath = Paths.get(CURRENCIES_DIR + symbol + ".txt"); // Path to currency data file
            if (Files.exists(filePath)) {
                List<String> lines = Files.readAllLines(filePath); // Read all lines from currency data file

                // Check if data for selected date already exists in the file
                for (String line : lines) {
                    if (line.contains("Date: " + formattedDate)) {
                        resultLabelCurrencies.setText("Data for this date is already saved."); // Display message if data exists
                        return;
                    }
                }
            }

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                // Construct API URL for fetching currency rates
                String apiUrl = "http://api.nbp.pl/api/exchangerates/rates/A/" + symbol + "/" + formattedDate + "/?format=json";
                HttpGet request = new HttpGet(apiUrl); // Create HTTP GET request
                HttpResponse response = httpClient.execute(request); // Execute HTTP request

                int responseCode = response.getStatusLine().getStatusCode(); // Get HTTP response code
                if (responseCode == 200) {
                    HttpEntity entity = response.getEntity(); // Get response entity
                    String responseBody = EntityUtils.toString(entity, StandardCharsets.UTF_8); // Read response body

                    // Parse JSON response
                    JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                    JsonArray rates = jsonResponse.getAsJsonArray("rates");

                    if (!rates.isEmpty()) {
                        JsonObject rate = rates.get(0).getAsJsonObject();
                        String effectiveDate = rate.get("effectiveDate").getAsString();
                        double mid = rate.get("mid").getAsDouble();

                        // Format data to save
                        String data = "Date: " + effectiveDate + "\n" +
                                "Symbol: " + symbol + "\n" +
                                "Rate: " + mid + " PLN" + "\n";

                        saveData(symbol, data); // Save fetched data
                        resultLabelCurrencies.setText("Data fetched and saved successfully."); // Display success message
                    } else {
                        resultLabelCurrencies.setText("No data available for the given date."); // Display message if no data available
                    }
                } else {
                    resultLabelCurrencies.setText("Failed to fetch data. Response code: " + responseCode); // Display error message on failed request
                }
            } catch (ConnectTimeoutException | SocketTimeoutException e) {
                resultLabelCurrencies.setText("Connection timed out. Please check your internet connection and try again."); // Display timeout error
                e.printStackTrace(); // Print stack trace for debugging
            } catch (IOException e) {
                resultLabelCurrencies.setText("Error fetching data.\nCheck your internet connection."); // Display general error message
                e.printStackTrace(); // Print stack trace for debugging
            }
        } catch (IOException e) {
            resultLabelCurrencies.setText("Error checking existing data."); // Display error message on data check error
            e.printStackTrace(); // Print stack trace for debugging
        }
    }

    // Method to save currency data to file
    protected void saveData(String symbol, String data) throws IOException {
        Path dirPath = Paths.get(CURRENCIES_DIR); // Path to currencies directory
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath); // Create currencies directory if it doesn't exist
        }

        Path filePath = dirPath.resolve(symbol + ".txt"); // Path to currency data file
        if (!Files.exists(filePath)) {
            Files.createFile(filePath); // Create currency data file if it doesn't exist
        }
        Files.write(filePath, (data + "\n").getBytes(), StandardOpenOption.APPEND); // Write data to file
    }

    // Method to start periodic weather update task
    private void startWeatherUpdateTask() {
        Timer timer = new Timer(true); // Create new timer
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                fetchWeatherData(); // Schedule periodic task to fetch weather data
            }
        }, 0, 300000); // Update weather every 5 minutes (300,000 milliseconds)
    }

    // Method to fetch weather data from API
    private void fetchWeatherData() {
        Platform.runLater(() -> {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                // Construct API URL for fetching weather data
                String apiUrl = String.format("http://api.weatherapi.com/v1/current.json?key=%s&q=Poznan", apiKey);
                HttpGet request = new HttpGet(apiUrl); // Create HTTP GET request
                HttpResponse response = httpClient.execute(request); // Execute HTTP request

                int responseCode = response.getStatusLine().getStatusCode(); // Get HTTP response code
                if (responseCode == 200) {
                    HttpEntity entity = response.getEntity(); // Get response entity
                    String responseBody = EntityUtils.toString(entity, StandardCharsets.UTF_8); // Read response body

                    // Parse JSON response
                    JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                    JsonObject current = jsonResponse.getAsJsonObject("current");
                    String lastUpdated = current.get("last_updated").getAsString();
                    double tempCelsius = current.get("temp_c").getAsDouble();
                    String condition = current.getAsJsonObject("condition").get("text").getAsString();

                    // Update UI elements with weather data
                    weatherDateTimeLabel.setText("Last Updated: " + lastUpdated);
                    weatherTemperatureLabel.setText("Temperature: " + tempCelsius + " °C");
                    weatherConditionLabel.setText("Condition: " + condition);
                    resultLabelWeather.setText(""); // Clear weather result label
                } else {
                    resultLabelWeather.setText("Failed to fetch weather data. Response code: " + responseCode); // Display error message on failed request
                }
            } catch (ConnectTimeoutException | SocketTimeoutException e) {
                resultLabelWeather.setText("Connection timed out. Please check your internet connection and try again."); // Display timeout error
                e.printStackTrace(); // Print stack trace for debugging
            } catch (IOException e) {
                resultLabelWeather.setText("Error fetching weather data.\nCheck your internet connection."); // Display general error message
                e.printStackTrace(); // Print stack trace for debugging
            }
        });
    }
}
