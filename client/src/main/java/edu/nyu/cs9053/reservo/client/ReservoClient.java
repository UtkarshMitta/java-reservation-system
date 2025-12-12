package edu.nyu.cs9053.reservo.client;

import javafx.application.Application;
import javafx.stage.Stage;

public class ReservoClient extends Application {

    @Override
    public void start(Stage primaryStage) {
        MainController controller = new MainController();
        controller.start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

