# JavaFX-application
The program is a JavaFX application that allows users to register, log in, display currency exchange rates, and weather data. Users can register by entering a login and password, which are then hashed and saved to a file. Upon logging in, users can fetch currency exchange rates from the NBP API and display weather data for Poznań from the WeatherAPI. The application updates weather data regularly every 5 minutes. Currency exchange rates are saved to text files, and their contents can be browsed by the user.

# In-app functionality
**Registration**: The user enters a login and password, which are hashed and saved in the users.txt file.
**Login**: The user enters a login and password, which are checked against the stored data in the file.
**Fetching currency exchange rates**: The user enters a currency symbol and date, and the application fetches the exchange rate from the NBP API and saves it to a file.
**Displaying currency exchange rates**: The user can display saved exchange rates for a given symbol.
Weather update: The application regularly fetches and displays current weather data for Poznań.

The application uses the HttpClient library for making HTTP requests, Gson for parsing JSON responses, and JavaFX for creating the user interface.

# Primary files
    • HelloApplication.java: Main application entry point
    • MainController.java: Controller for the main application logic
    • RegisterController.java: Controller for the user registration functionality
    • StartController.java: Controller for handling the initial UI actions

**Opened and created in [IntelliJ Idea 2024.1] (https://www.jetbrains.com/idea/download/?section=windows)**
