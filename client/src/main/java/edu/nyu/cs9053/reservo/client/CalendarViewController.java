package edu.nyu.cs9053.reservo.client;

import edu.nyu.cs9053.reservo.client.api.ApiClient;
import edu.nyu.cs9053.reservo.client.websocket.WebSocketClientWrapper;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CalendarViewController {
    private ApiClient apiClient;
    private WebSocketClientWrapper wsClient;
    private VBox view;
    private ComboBox<Map<String, Object>> resourceCombo;
    private GridPane calendarGrid;
    private List<Map<String, Object>> resources;

    public CalendarViewController(ApiClient apiClient, WebSocketClientWrapper wsClient) {
        this.apiClient = apiClient;
        this.wsClient = wsClient;
        this.view = new VBox(10);
        this.view.setPadding(new Insets(10));
        initializeView();
        loadResources();
    }

    private void initializeView() {
        view.setStyle(String.format("-fx-background-color: %s; -fx-padding: 20;", UIStyles.BG_LIGHT));
        
        Label title = new Label("📅 Resource Calendar");
        title.setStyle(UIStyles.sectionTitle());

        resourceCombo = new ComboBox<>();
        resourceCombo.setPromptText("Select Resource");
        resourceCombo.setPrefWidth(300);
        resourceCombo.setStyle(UIStyles.inputField());
        resourceCombo.setOnAction(e -> loadAvailability());

        Button refreshButton = new Button("🔄 Refresh");
        refreshButton.setStyle(UIStyles.secondaryButton());
        refreshButton.setOnAction(e -> loadAvailability());

        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 0, 15, 0));
        Label resourceLabel = new Label("Resource:");
        resourceLabel.setStyle(String.format("-fx-font-size: 14px; -fx-text-fill: %s;", UIStyles.TEXT_DARK));
        topBar.getChildren().addAll(resourceLabel, resourceCombo, refreshButton);

        calendarGrid = new GridPane();
        calendarGrid.setHgap(5);
        calendarGrid.setVgap(5);
        calendarGrid.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(calendarGrid);
        scrollPane.setFitToWidth(true);

        view.getChildren().addAll(title, topBar, scrollPane);
    }

    private void loadResources() {
        Task<List<Map<String, Object>>> task = new Task<List<Map<String, Object>>>() {
            @Override
            protected List<Map<String, Object>> call() throws Exception {
                return apiClient.getResources();
            }
        };
        
        task.setOnSucceeded(e -> {
            try {
                resources = task.getValue();
                Platform.runLater(() -> {
                    resourceCombo.getItems().clear();
                    resourceCombo.getItems().addAll(resources);
                    resourceCombo.setCellFactory(param -> new ListCell<Map<String, Object>>() {
                        @Override
                        protected void updateItem(Map<String, Object> item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                            } else {
                                setText((String) item.get("name"));
                            }
                        }
                    });
                    resourceCombo.setButtonCell(new ListCell<Map<String, Object>>() {
                        @Override
                        protected void updateItem(Map<String, Object> item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                            } else {
                                setText((String) item.get("name"));
                            }
                        }
                    });
                    if (!resources.isEmpty()) {
                        resourceCombo.getSelectionModel().select(0);
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> showError("Failed to load resources: " + ex.getMessage()));
            }
        });
        
        task.setOnFailed(e -> {
            Platform.runLater(() -> showError("Failed to load resources: " + task.getException().getMessage()));
        });
        
        new Thread(task).start();
    }

    private void loadAvailability() {
        Map<String, Object> selected = resourceCombo.getValue();
        if (selected == null) {
            return;
        }

        Long resourceId = ((Number) selected.get("id")).longValue();
        LocalDate today = LocalDate.now();
        String from = today.atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String to = today.plusDays(7).atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        Task<Map<String, Object>> task = new Task<Map<String, Object>>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                return apiClient.getAvailability(resourceId, from, to);
            }
        };
        
        task.setOnSucceeded(e -> {
            try {
                Map<String, Object> availability = task.getValue();
                Platform.runLater(() -> displayAvailability(availability, selected));
            } catch (Exception ex) {
                Platform.runLater(() -> showError("Failed to load availability: " + ex.getMessage()));
            }
        });
        
        task.setOnFailed(e -> {
            Platform.runLater(() -> showError("Failed to load availability: " + task.getException().getMessage()));
        });
        
        new Thread(task).start();
    }

    @SuppressWarnings("unchecked")
    private void displayAvailability(Map<String, Object> availability, Map<String, Object> resource) {
        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        List<Map<String, Object>> slots = (List<Map<String, Object>>) availability.get("slots");
        if (slots == null || slots.isEmpty()) {
            Label noSlots = new Label("No slots available");
            calendarGrid.add(noSlots, 0, 0);
            return;
        }

        // Create header
        Label header = new Label("Time Slots");
        header.setStyle("-fx-font-weight: bold;");
        calendarGrid.add(header, 0, 0);

        int row = 1;
        for (Map<String, Object> slot : slots) {
            String startStr = (String) slot.get("startTs");
            String endStr = (String) slot.get("endTs");
            Integer capacityRemaining = ((Number) slot.get("capacityRemaining")).intValue();
            Integer totalCapacity = ((Number) slot.get("totalCapacity")).intValue();
            Long slotId = ((Number) slot.get("id")).longValue();

            LocalDateTime start = LocalDateTime.parse(startStr.replace(" ", "T"));
            String timeLabel = start.format(DateTimeFormatter.ofPattern("MM/dd HH:mm")) + 
                             " - " + LocalDateTime.parse(endStr.replace(" ", "T"))
                             .format(DateTimeFormatter.ofPattern("HH:mm"));

            final Long finalSlotId = slotId;
            final Integer finalCapacityRemaining = capacityRemaining;
            final Map<String, Object> finalResource = resource;

            Button slotButton = new Button(timeLabel + "\n" + 
                    capacityRemaining + "/" + totalCapacity + " available");
            slotButton.setPrefWidth(150);
            slotButton.setPrefHeight(60);

            if (capacityRemaining > 0) {
                slotButton.setStyle(String.format(
                    "-fx-background-color: %s; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 12px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-background-radius: 8; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2);",
                    UIStyles.SUCCESS_GREEN
                ));
                slotButton.setOnAction(e -> showBookingDialog(finalSlotId, finalCapacityRemaining, finalResource));
            } else {
                slotButton.setStyle(String.format(
                    "-fx-background-color: %s; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 12px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-background-radius: 8; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2);",
                    UIStyles.ERROR_RED
                ));
                slotButton.setOnAction(e -> showWaitlistDialog(finalSlotId));
            }

            calendarGrid.add(slotButton, 0, row++);
        }
    }

    private void showBookingDialog(Long timeSlotId, Integer maxQty, Map<String, Object> resource) {
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle("Book Slot");
        dialog.setHeaderText("Reserve: " + resource.get("name"));

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle(UIStyles.cardStyle());

        Label qtyLabel = new Label("Quantity:");
        qtyLabel.setStyle(String.format("-fx-font-size: 14px; -fx-text-fill: %s;", UIStyles.TEXT_DARK));
        Spinner<Integer> qtySpinner = new Spinner<>(1, maxQty, 1);
        qtySpinner.setPrefWidth(200);
        
        Label countdownLabel = new Label("⏱️ 60 seconds to confirm");
        countdownLabel.setStyle(String.format("-fx-font-size: 14px; -fx-text-fill: %s; -fx-font-weight: bold;", UIStyles.NYU_VIOLET));
        Label statusLabel = new Label();
        statusLabel.setWrapText(true);

        Button confirmButton = new Button("✓ Confirm Reservation");
        confirmButton.setStyle(UIStyles.primaryButton());
        confirmButton.setPrefWidth(200);
        
        Button cancelButton = new Button("✗ Cancel");
        cancelButton.setStyle(UIStyles.secondaryButton());
        cancelButton.setPrefWidth(200);

        String requestId = UUID.randomUUID().toString();
        final Long[] holdIdRef = new Long[1];
        final boolean[] holdPlaced = new boolean[1];
        holdIdRef[0] = null;
        holdPlaced[0] = false;

        // Disable confirm button while placing hold, but keep cancel enabled
        confirmButton.setDisable(true);
        statusLabel.setText("Placing hold...");
        statusLabel.setStyle(UIStyles.statusInfo());

        // Place hold in background thread
        javafx.concurrent.Task<Map<String, Object>> placeHoldTask = new javafx.concurrent.Task<Map<String, Object>>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                return apiClient.placeHold(timeSlotId, qtySpinner.getValue(), requestId);
            }
        };

        placeHoldTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                try {
                    Map<String, Object> hold = placeHoldTask.getValue();
                    holdIdRef[0] = ((Number) hold.get("id")).longValue();
                    holdPlaced[0] = true;
                    String expiresAt = (String) hold.get("expiresAt");
                    confirmButton.setDisable(false);
                    statusLabel.setText("");
                    // Start countdown timer
                    startCountdown(expiresAt, countdownLabel, confirmButton);
                } catch (Exception e) {
                    if (dialog.isShowing()) {
                        statusLabel.setText("Failed to place hold: " + e.getMessage());
                        statusLabel.setStyle(UIStyles.statusError());
                        confirmButton.setDisable(false);
                    }
                }
            });
        });

        placeHoldTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                // Only show error if dialog is still open
                if (dialog.isShowing()) {
                    statusLabel.setText("Failed to place hold: " + placeHoldTask.getException().getMessage());
                    statusLabel.setStyle(UIStyles.statusError());
                    confirmButton.setDisable(false);
                }
            });
        });

        new Thread(placeHoldTask).start();

        confirmButton.setOnAction(e -> {
            if (holdIdRef[0] == null) {
                statusLabel.setText("Please wait for hold to be placed...");
                statusLabel.setStyle(UIStyles.statusWarning());
                return;
            }
            
            confirmButton.setDisable(true);
            statusLabel.setText("Confirming...");
            statusLabel.setStyle(UIStyles.statusInfo());
            
            javafx.concurrent.Task<Map<String, Object>> confirmTask = new javafx.concurrent.Task<Map<String, Object>>() {
                @Override
                protected Map<String, Object> call() throws Exception {
                    apiClient.confirmHold(holdIdRef[0]);
                    return Map.of("success", true);
                }
            };
            
            confirmTask.setOnSucceeded(event -> {
                Platform.runLater(() -> {
                    dialog.setResult(Map.of("success", true));
                    dialog.close();
                    loadAvailability();
                });
            });
            
            confirmTask.setOnFailed(event -> {
                Platform.runLater(() -> {
                    statusLabel.setText("Confirmation failed: " + confirmTask.getException().getMessage());
                    statusLabel.setStyle(UIStyles.statusError());
                    confirmButton.setDisable(false);
                });
            });
            
            new Thread(confirmTask).start();
        });

        cancelButton.setOnAction(e -> {
            // If a hold was placed, cancel it before closing
            if (holdPlaced[0] && holdIdRef[0] != null) {
                // Cancel the hold in background
                javafx.concurrent.Task<Void> cancelHoldTask = new javafx.concurrent.Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        apiClient.cancelHold(holdIdRef[0]);
                        return null;
                    }
                };
                
                cancelHoldTask.setOnSucceeded(event -> {
                    Platform.runLater(() -> {
                        dialog.close();
                        loadAvailability(); // Refresh to show updated capacity
                    });
                });
                
                cancelHoldTask.setOnFailed(event -> {
                    // Even if cancel fails, close the dialog
                    // The hold will expire naturally
                    Platform.runLater(() -> {
                        dialog.close();
                        loadAvailability();
                    });
                });
                
                new Thread(cancelHoldTask).start();
            } else {
                // No hold placed yet, just close
                dialog.close();
            }
        });

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(confirmButton, cancelButton);
        
        content.getChildren().addAll(
                qtyLabel, qtySpinner,
                countdownLabel, statusLabel,
                buttonBox
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.CLOSE) {
                // Handle close button (X) or ESC key
                if (holdPlaced[0] && holdIdRef[0] != null) {
                    // Cancel the hold if it was placed
                    javafx.concurrent.Task<Void> cancelHoldTask = new javafx.concurrent.Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            apiClient.cancelHold(holdIdRef[0]);
                            return null;
                        }
                    };
                    
                    cancelHoldTask.setOnSucceeded(event -> {
                        Platform.runLater(() -> loadAvailability());
                    });
                    
                    cancelHoldTask.setOnFailed(event -> {
                        Platform.runLater(() -> loadAvailability());
                    });
                    
                    new Thread(cancelHoldTask).start();
                }
                return null;
            }
            return dialog.getResult();
        });
        
        // Show dialog non-modally so it doesn't block
        dialog.show();
    }

    private void startCountdown(String expiresAt, Label label, Button confirmButton) {
        // Simplified countdown - in production, parse expiresAt and calculate remaining time
        new Thread(() -> {
            for (int i = 60; i >= 0; i--) {
                final int seconds = i;
                Platform.runLater(() -> {
                    label.setText(seconds + " seconds remaining");
                    if (seconds == 0) {
                        confirmButton.setDisable(true);
                        label.setText("⏰ Hold expired");
                        label.setStyle(UIStyles.statusError());
                    }
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }

    private void showWaitlistDialog(Long timeSlotId) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Join Waitlist");
        dialog.setHeaderText("This slot is full. Join the waitlist?");

        Button joinButton = new Button("Join Waitlist");
        Label statusLabel = new Label();
        joinButton.setOnAction(e -> {
            joinButton.setDisable(true);
            statusLabel.setText("Joining waitlist...");
            statusLabel.setStyle("-fx-text-fill: blue;");
            
            javafx.concurrent.Task<Void> joinTask = new javafx.concurrent.Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    apiClient.joinWaitlist(timeSlotId);
                    return null;
                }
            };
            
            joinTask.setOnSucceeded(event -> {
                Platform.runLater(() -> {
                    dialog.close();
                    showInfo("Added to waitlist");
                    loadAvailability();
                });
            });
            
            joinTask.setOnFailed(event -> {
                Platform.runLater(() -> {
                    statusLabel.setText("Failed to join waitlist: " + joinTask.getException().getMessage());
                    statusLabel.setStyle("-fx-text-fill: red;");
                    joinButton.setDisable(false);
                });
            });
            
            new Thread(joinTask).start();
        });

        VBox content = new VBox(10, new Label("You will be notified when a slot becomes available."), joinButton, statusLabel);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.show();
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

