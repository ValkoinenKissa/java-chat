package me.abollo.javachat;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ChatApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ChatApplication.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 780, 430);
        stage.setTitle("Java Chat");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
}
