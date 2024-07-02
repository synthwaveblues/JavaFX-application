package com.example.jpo;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.io.IOException;

public class StartController {
    @FXML
    protected void openRegisterWindow() throws IOException {
        // Load register-view.fxml and open a new stage for registration
        Parent root = FXMLLoader.load(getClass().getResource("register-view.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Register");
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    protected void openLoginWindow() throws IOException {
        // Load main-view.fxml and open a new stage for login
        Parent root = FXMLLoader.load(getClass().getResource("main-view.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Main");
        stage.setScene(new Scene(root));
        stage.show();
    }
}
