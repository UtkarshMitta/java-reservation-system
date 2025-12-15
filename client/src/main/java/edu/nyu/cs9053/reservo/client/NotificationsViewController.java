package edu.nyu.cs9053.reservo.client;

import edu.nyu.cs9053.reservo.client.api.ApiClient;
import javafx.application.Platform;
import javafx.concurrent.Task;
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
        notificationsList.getItems().clear();
        notificationsList.getItems().add("Loading...");
        
        Task<List<Map<String, Object>>> loadTask = new Task<List<Map<String, Object>>>() {
            @Override
            protected List<Map<String, Object>> call() throws Exception {
                return apiClient.getNotifications();
            }
        };
        
        loadTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                try {
                    List<Map<String, Object>> notifications = loadTask.getValue();
                    notificationsList.getItems().clear();
                    
                    if (notifications == null || notifications.isEmpty()) {
                        notificationsList.getItems().add("No notifications");
                        return;
                    }

                    for (Map<String, Object> notif : notifications) {
                        Object typeObj = getValueCaseInsensitive(notif, "type");
                        Object messageObj = getValueCaseInsensitive(notif, "message");
                        Object readObj = getValueCaseInsensitive(notif, "read");
                        
                        String type = typeObj != null ? typeObj.toString() : "UNKNOWN";
                        String message = messageObj != null ? messageObj.toString() : "No message";
                        
                        // Handle read field - can be Boolean, boolean, or null
                        boolean isRead = false;
                        if (readObj != null) {
                            if (readObj instanceof Boolean) {
                                isRead = ((Boolean) readObj).booleanValue();
                            } else if (readObj instanceof String) {
                                isRead = Boolean.parseBoolean((String) readObj);
                            } else if (readObj instanceof Number) {
                                isRead = ((Number) readObj).intValue() != 0;
                            }
                        }
                        
                        String display = String.format("[%s] %s", type, message);
                        if (!isRead) {
                            display = "● " + display;
                        }
                        notificationsList.getItems().add(display);
                    }
                } catch (Exception ex) {
                    notificationsList.getItems().clear();
                    showError("Failed to load notifications: " + ex.getMessage());
                }
            });
        });
        
        loadTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                notificationsList.getItems().clear();
                showError("Failed to load notifications: " + loadTask.getException().getMessage());
            });
        });
        
        new Thread(loadTask).start();
    }

    private Object getValueCaseInsensitive(Map<String, Object> map, String key) {
        // H2 returns uppercase column names, so check both cases
        if (map.containsKey(key)) return map.get(key);
        if (map.containsKey(key.toUpperCase())) return map.get(key.toUpperCase());
        if (map.containsKey(key.toLowerCase())) return map.get(key.toLowerCase());
        // Also check with underscores converted
        String upperKey = key.toUpperCase().replace("_", "_");
        if (map.containsKey(upperKey)) return map.get(upperKey);
        return null;
    }

    public VBox getView() {
        return view;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }
}

