package edu.nyu.cs9053.reservo.client;

import edu.nyu.cs9053.reservo.client.api.ApiClient;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;

public class NotificationsViewController {
    private ApiClient apiClient;
    private VBox view;
    private ListView<String> notificationsList;

    public NotificationsViewController(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.view = new VBox(10);
        this.view.setPadding(new Insets(10));
        initializeView();
        loadNotifications();
    }

    private void initializeView() {
        view.setStyle(String.format("-fx-background-color: %s; -fx-padding: 20;", UIStyles.BG_LIGHT));
        
        Label title = new Label("🔔 Notifications");
        title.setStyle(UIStyles.sectionTitle());

        Button refreshButton = new Button("🔄 Refresh");
        refreshButton.setStyle(UIStyles.secondaryButton());
        refreshButton.setOnAction(e -> loadNotifications());

        notificationsList = new ListView<>();
        notificationsList.setPrefHeight(600);
        notificationsList.setStyle(String.format(
            "-fx-background-color: %s; " +
            "-fx-background-radius: 8; " +
            "-fx-border-color: %s; " +
            "-fx-border-radius: 8;",
            UIStyles.BG_WHITE, UIStyles.BORDER_LIGHT
        ));

        view.getChildren().addAll(title, refreshButton, notificationsList);
    }

    private void loadNotifications() {
        try {
            List<Map<String, Object>> notifications = apiClient.getNotifications();
            notificationsList.getItems().clear();

            for (Map<String, Object> notif : notifications) {
                String type = (String) notif.get("type");
                String message = (String) notif.get("message");
                Boolean read = (Boolean) notif.get("read");
                String display = String.format("[%s] %s", type, message);
                if (!read) {
                    display = "● " + display;
                }
                notificationsList.getItems().add(display);
            }
        } catch (Exception e) {
            showError("Failed to load notifications: " + e.getMessage());
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

