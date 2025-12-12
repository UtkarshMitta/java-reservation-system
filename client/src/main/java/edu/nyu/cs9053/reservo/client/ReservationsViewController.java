package edu.nyu.cs9053.reservo.client;

import edu.nyu.cs9053.reservo.client.api.ApiClient;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ReservationsViewController {
    private ApiClient apiClient;
    private VBox view;
    private ListView<String> reservationsList;

    public ReservationsViewController(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.view = new VBox(10);
        this.view.setPadding(new Insets(10));
        initializeView();
        loadReservations();
    }

    private void initializeView() {
        view.setStyle(String.format("-fx-background-color: %s; -fx-padding: 20;", UIStyles.BG_LIGHT));
        
        Label title = new Label("✅ My Reservations");
        title.setStyle(UIStyles.sectionTitle());

        Button refreshButton = new Button("🔄 Refresh");
        refreshButton.setStyle(UIStyles.secondaryButton());
        refreshButton.setOnAction(e -> loadReservations());

        reservationsList = new ListView<>();
        reservationsList.setPrefHeight(600);
        reservationsList.setStyle(String.format(
            "-fx-background-color: %s; " +
            "-fx-background-radius: 8; " +
            "-fx-border-color: %s; " +
            "-fx-border-radius: 8;",
            UIStyles.BG_WHITE, UIStyles.BORDER_LIGHT
        ));

        view.getChildren().addAll(title, refreshButton, reservationsList);
    }

    private void loadReservations() {
        reservationsList.getItems().clear();
        reservationsList.getItems().add("Loading...");
        
        Task<List<Map<String, Object>>> task = new Task<List<Map<String, Object>>>() {
            @Override
            protected List<Map<String, Object>> call() throws Exception {
                return apiClient.getMyReservations();
            }
        };
        
        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                try {
                    List<Map<String, Object>> reservations = task.getValue();
                    reservationsList.getItems().clear();
                    
                    if (reservations == null || reservations.isEmpty()) {
                        reservationsList.getItems().add("No reservations found");
                        return;
                    }

                    for (Map<String, Object> res : reservations) {
                        // Safely extract values with null checks (case-insensitive for H2 uppercase)
                        Object idObj = getValueCaseInsensitive(res, "id");
                        Object qtyObj = getValueCaseInsensitive(res, "qty");
                        
                        if (idObj == null || qtyObj == null) {
                            System.err.println("Warning: Reservation missing required fields: " + res);
                            continue;
                        }
                        
                        Object resourceNameObj = getValueCaseInsensitive(res, "resource_name");
                        Object startTsObj = getValueCaseInsensitive(res, "start_ts");
                        Object endTsObj = getValueCaseInsensitive(res, "end_ts");
                        
                        Long reservationId = ((Number) idObj).longValue();
                        Integer qty = ((Number) qtyObj).intValue();
                        
                        String startTs = startTsObj != null ? startTsObj.toString() : "N/A";
                        String endTs = endTsObj != null ? endTsObj.toString() : "N/A";
                        String resourceNameSafe = resourceNameObj != null ? resourceNameObj.toString() : "Unknown Resource";

                        String display = String.format("%s: %s - %s (Qty: %d)",
                                resourceNameSafe, formatDateTime(startTs), formatDateTime(endTs), qty);

                        MenuItem cancelItem = new MenuItem("Cancel");
                        cancelItem.setOnAction(evt -> cancelReservation(reservationId));

                        ContextMenu contextMenu = new ContextMenu(cancelItem);
                        ListCell<String> cell = new ListCell<String>() {
                            @Override
                            protected void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty || item == null) {
                                    setText(null);
                                    setContextMenu(null);
                                } else {
                                    setText(item);
                                    setContextMenu(contextMenu);
                                }
                            }
                        };

                        reservationsList.getItems().add(display);
                    }
                } catch (Exception ex) {
                    Platform.runLater(() -> showError("Failed to load reservations: " + ex.getMessage()));
                }
            });
        });
        
        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                reservationsList.getItems().clear();
                showError("Failed to load reservations: " + task.getException().getMessage());
            });
        });
        
        new Thread(task).start();
    }

    private void cancelReservation(Long reservationId) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Cancel this reservation?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    apiClient.cancelReservation(reservationId);
                    loadReservations();
                    showInfo("Reservation cancelled");
                } catch (Exception e) {
                    showError("Failed to cancel: " + e.getMessage());
                }
            }
        });
    }

    private Object getValueCaseInsensitive(Map<String, Object> map, String key) {
        // H2 returns uppercase column names, so check both cases
        if (map.containsKey(key)) return map.get(key);
        if (map.containsKey(key.toUpperCase())) return map.get(key.toUpperCase());
        if (map.containsKey(key.toLowerCase())) return map.get(key.toLowerCase());
        // Also check with underscores converted
        String upperUnderscore = key.toUpperCase().replace("_", "_");
        if (map.containsKey(upperUnderscore)) return map.get(upperUnderscore);
        return null;
    }

    private String formatDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.equals("N/A")) {
            return "N/A";
        }
        try {
            // Handle both timestamp and string formats
            String normalized = dateTimeStr.replace(" ", "T");
            if (normalized.contains(".")) {
                normalized = normalized.substring(0, normalized.indexOf("."));
            }
            LocalDateTime dt = LocalDateTime.parse(normalized);
            return dt.format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm"));
        } catch (Exception e) {
            return dateTimeStr;
        }
    }

    public VBox getView() {
        return view;
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, message);
            alert.show();
        });
    }

    private void showInfo(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
            alert.show();
        });
    }
}

