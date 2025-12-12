package edu.nyu.cs9053.reservo.client;

import edu.nyu.cs9053.reservo.client.api.ApiClient;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;

public class WaitlistViewController {
    private ApiClient apiClient;
    private VBox view;
    private ListView<String> waitlistView;

    public WaitlistViewController(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.view = new VBox(10);
        this.view.setPadding(new Insets(10));
        initializeView();
        loadWaitlist();
    }

    private void initializeView() {
        view.setStyle(String.format("-fx-background-color: %s; -fx-padding: 20;", UIStyles.BG_LIGHT));
        
        Label title = new Label("📋 My Waitlist");
        title.setStyle(UIStyles.sectionTitle());

        Button refreshButton = new Button("🔄 Refresh");
        refreshButton.setStyle(UIStyles.secondaryButton());
        refreshButton.setOnAction(e -> loadWaitlist());

        waitlistView = new ListView<>();
        waitlistView.setPrefHeight(600);
        waitlistView.setStyle(String.format(
            "-fx-background-color: %s; " +
            "-fx-background-radius: 8; " +
            "-fx-border-color: %s; " +
            "-fx-border-radius: 8;",
            UIStyles.BG_WHITE, UIStyles.BORDER_LIGHT
        ));

        view.getChildren().addAll(title, refreshButton, waitlistView);
    }

    private void loadWaitlist() {
        try {
            List<Map<String, Object>> waitlist = apiClient.getMyWaitlist();
            waitlistView.getItems().clear();

            for (Map<String, Object> entry : waitlist) {
                String resourceName = (String) entry.get("resource_name");
                Number position = (Number) entry.get("position");
                String display = String.format("%s - Position: %d", resourceName, position.intValue() + 1);
                waitlistView.getItems().add(display);
            }
        } catch (Exception e) {
            showError("Failed to load waitlist: " + e.getMessage());
        }
    }

    public VBox getView() {
        return view;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }
}

