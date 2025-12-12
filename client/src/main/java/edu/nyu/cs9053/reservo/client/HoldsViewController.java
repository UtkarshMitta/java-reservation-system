package edu.nyu.cs9053.reservo.client;

import edu.nyu.cs9053.reservo.client.api.ApiClient;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class HoldsViewController {
    private ApiClient apiClient;
    private VBox view;
    private ListView<String> holdsList;
    private List<Map<String, Object>> holdsData;

    public HoldsViewController(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.view = new VBox(10);
        this.view.setPadding(new Insets(10));
        initializeView();
        loadHolds();
    }

    private void initializeView() {
        view.setStyle(String.format("-fx-background-color: %s; -fx-padding: 20;", UIStyles.BG_LIGHT));
        
        Label title = new Label("⏳ My Active Holds");
        title.setStyle(UIStyles.sectionTitle());

        Button refreshButton = new Button("🔄 Refresh");
        refreshButton.setStyle(UIStyles.secondaryButton());
        refreshButton.setOnAction(e -> loadHolds());

        Button cancelButton = new Button("✗ Cancel Selected Hold");
        cancelButton.setStyle(UIStyles.dangerButton());
        cancelButton.setDisable(true);
        cancelButton.setOnAction(e -> cancelSelectedHold());

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        buttonBox.setPadding(new Insets(0, 0, 15, 0));
        buttonBox.getChildren().addAll(refreshButton, cancelButton);

        holdsList = new ListView<>();
        holdsList.setPrefHeight(600);
        holdsList.setStyle(String.format(
            "-fx-background-color: %s; " +
            "-fx-background-radius: 8; " +
            "-fx-border-color: %s; " +
            "-fx-border-radius: 8;",
            UIStyles.BG_WHITE, UIStyles.BORDER_LIGHT
        ));
        holdsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            cancelButton.setDisable(newVal == null);
        });

        view.getChildren().addAll(title, buttonBox, holdsList);
    }

    private void loadHolds() {
        Task<List<Map<String, Object>>> loadTask = new Task<List<Map<String, Object>>>() {
            @Override
            protected List<Map<String, Object>> call() throws Exception {
                return apiClient.getMyHolds();
            }
        };

        loadTask.setOnSucceeded(event -> {
            try {
                holdsData = loadTask.getValue();
                holdsList.getItems().clear();

                if (holdsData == null || holdsData.isEmpty()) {
                    holdsList.getItems().add("No active holds");
                    return;
                }

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

                for (Map<String, Object> hold : holdsData) {
                    Long holdId = getValueAsLong(hold, "id");
                    Long timeSlotId = getValueAsLong(hold, "time_slot_id");
                    Integer qty = getValueAsInt(hold, "qty");
                    
                    Object expiresAtObj = getCaseInsensitive(hold, "expires_at");
                    String expiresAtStr = "Unknown";
                    if (expiresAtObj != null) {
                        try {
                            if (expiresAtObj instanceof java.sql.Timestamp) {
                                LocalDateTime expiresAt = ((java.sql.Timestamp) expiresAtObj).toLocalDateTime();
                                expiresAtStr = expiresAt.format(formatter);
                            } else if (expiresAtObj instanceof String) {
                                expiresAtStr = (String) expiresAtObj;
                            }
                        } catch (Exception e) {
                            expiresAtStr = expiresAtObj.toString();
                        }
                    }

                    String display = String.format("Hold #%d | Slot: %d | Qty: %d | Expires: %s",
                            holdId, timeSlotId, qty, expiresAtStr);
                    holdsList.getItems().add(display);
                }
            } catch (Exception ex) {
                showError("Failed to load holds: " + ex.getMessage());
            }
        });

        loadTask.setOnFailed(event -> {
            showError("Failed to load holds: " + loadTask.getException().getMessage());
        });

        new Thread(loadTask).start();
    }

    private void cancelSelectedHold() {
        int selectedIndex = holdsList.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0 || holdsData == null || selectedIndex >= holdsData.size()) {
            return;
        }

        Map<String, Object> selectedHold = holdsData.get(selectedIndex);
        Long holdId = getValueAsLong(selectedHold, "id");

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Cancel Hold");
        confirmDialog.setHeaderText("Cancel Hold");
        confirmDialog.setContentText("Are you sure you want to cancel this hold?");
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Task<Void> cancelTask = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        apiClient.cancelHold(holdId);
                        return null;
                    }
                };

                cancelTask.setOnSucceeded(event -> {
                    Platform.runLater(() -> {
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Success");
                        successAlert.setHeaderText("Hold Cancelled");
                        successAlert.setContentText("The hold has been cancelled successfully.");
                        successAlert.showAndWait();
                        loadHolds(); // Refresh the list
                    });
                });

                cancelTask.setOnFailed(event -> {
                    Platform.runLater(() -> {
                        String errorMsg = cancelTask.getException().getMessage();
                        showError("Failed to cancel hold: " + errorMsg);
                    });
                });

                new Thread(cancelTask).start();
            }
        });
    }

    private Object getCaseInsensitive(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value != null) return value;
        value = map.get(key.toUpperCase());
        if (value != null) return value;
        return map.get(key.toLowerCase());
    }

    private Long getValueAsLong(Map<String, Object> map, String key) {
        Object value = getCaseInsensitive(map, key);
        if (value == null) return 0L;
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(value.toString());
    }

    private Integer getValueAsInt(Map<String, Object> map, String key) {
        Object value = getCaseInsensitive(map, key);
        if (value == null) return 0;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(value.toString());
    }

    public VBox getView() {
        return view;
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, message);
            alert.showAndWait();
        });
    }
}

