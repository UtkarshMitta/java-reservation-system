package edu.nyu.cs9053.reservo.client;

import edu.nyu.cs9053.reservo.client.api.ApiClient;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Map;

public class AdminViewController {
    private ApiClient apiClient;
    private VBox view;
    private TabPane tabPane;
    private VBox resourcesContainer;

    public AdminViewController(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.view = new VBox(10);
        this.view.setPadding(new Insets(10));
        initializeView();
    }

    private void initializeView() {
        view.setStyle(String.format("-fx-background-color: %s; -fx-padding: 20;", UIStyles.BG_LIGHT));
        
        Label title = new Label("👑 Admin Console");
        title.setStyle(UIStyles.pageTitle());

        tabPane = new TabPane();
        tabPane.setStyle(String.format(
            "-fx-background-color: %s; " +
            "-fx-tab-min-width: 140px;",
            UIStyles.BG_WHITE
        ));
        
        Tab createResourceTab = createResourceTab();
        Tab simulateContentionTab = createSimulateContentionTab();
        Tab isolationModeTab = createIsolationModeTab();
        Tab resourcesListTab = createResourcesListTab();

        tabPane.getTabs().addAll(createResourceTab, simulateContentionTab, isolationModeTab, resourcesListTab);

        view.getChildren().addAll(title, tabPane);
    }

    private Tab createResourceTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle(String.format("-fx-background-color: %s;", UIStyles.BG_LIGHT));

        Label sectionTitle = new Label("➕ Create New Resource");
        sectionTitle.setStyle(UIStyles.sectionTitle());

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setAlignment(Pos.CENTER_LEFT);

        TextField nameField = new TextField();
        nameField.setPromptText("e.g., Study Room C");
        nameField.setPrefWidth(350);
        nameField.setStyle(UIStyles.inputField());

        TextField capacityField = new TextField();
        capacityField.setPromptText("e.g., 10");
        capacityField.setPrefWidth(350);
        capacityField.setStyle(UIStyles.inputField());

        TextField slotDurationField = new TextField();
        slotDurationField.setPromptText("e.g., 60 (minutes)");
        slotDurationField.setPrefWidth(350);
        slotDurationField.setStyle(UIStyles.inputField());

        TextField bookingHorizonField = new TextField();
        bookingHorizonField.setPromptText("e.g., 30 (days)");
        bookingHorizonField.setPrefWidth(350);
        bookingHorizonField.setStyle(UIStyles.inputField());

        TextField maxHoursField = new TextField();
        maxHoursField.setPromptText("e.g., 4 (hours per day)");
        maxHoursField.setPrefWidth(350);
        maxHoursField.setStyle(UIStyles.inputField());

        TextArea rulesField = new TextArea();
        rulesField.setPromptText("JSON rules (optional)");
        rulesField.setPrefWidth(350);
        rulesField.setPrefRowCount(3);
        rulesField.setStyle(UIStyles.inputField());

        Label statusLabel = new Label();
        statusLabel.setWrapText(true);
        statusLabel.setPrefWidth(400);

        Button createButton = new Button("✓ Create Resource");
        createButton.setStyle(UIStyles.successButton());
        createButton.setPrefWidth(180);

        form.add(new Label("Name:"), 0, 0);
        form.add(nameField, 1, 0);
        form.add(new Label("Capacity:"), 0, 1);
        form.add(capacityField, 1, 1);
        form.add(new Label("Slot Duration (minutes):"), 0, 2);
        form.add(slotDurationField, 1, 2);
        form.add(new Label("Booking Horizon (days):"), 0, 3);
        form.add(bookingHorizonField, 1, 3);
        form.add(new Label("Max Hours Per Day:"), 0, 4);
        form.add(maxHoursField, 1, 4);
        form.add(new Label("Rules JSON (optional):"), 0, 5);
        form.add(rulesField, 1, 5);
        form.add(createButton, 1, 6);
        form.add(statusLabel, 0, 7, 2, 1);

        createButton.setOnAction(e -> {
            try {
                String name = nameField.getText().trim();
                Integer capacity = Integer.parseInt(capacityField.getText().trim());
                Integer slotDuration = Integer.parseInt(slotDurationField.getText().trim());
                Integer bookingHorizon = Integer.parseInt(bookingHorizonField.getText().trim());
                String maxHoursStr = maxHoursField.getText().trim();
                Integer maxHours = maxHoursStr.isEmpty() ? null : Integer.parseInt(maxHoursStr);
                String rulesJson = rulesField.getText().trim();
                if (rulesJson.isEmpty()) {
                    rulesJson = "{}";
                }

                if (name.isEmpty()) {
                    statusLabel.setText("Name is required");
                    statusLabel.setStyle(UIStyles.statusError());
                    return;
                }

                createButton.setDisable(true);
                statusLabel.setText("Creating resource...");
                statusLabel.setStyle(UIStyles.statusInfo());

                Map<String, Object> resource = Map.of(
                        "name", name,
                        "capacity", capacity,
                        "slotDurationMinutes", slotDuration,
                        "bookingHorizonDays", bookingHorizon,
                        "maxHoursPerDay", maxHours != null ? maxHours : "",
                        "rulesJson", rulesJson
                );

                Task<Map<String, Object>> createTask = new Task<Map<String, Object>>() {
                    @Override
                    protected Map<String, Object> call() throws Exception {
                        return apiClient.createResource(resource);
                    }
                };

                createTask.setOnSucceeded(event -> {
                    statusLabel.setText("Resource created successfully!");
                    statusLabel.setStyle(UIStyles.statusSuccess());
                    nameField.clear();
                    capacityField.clear();
                    slotDurationField.clear();
                    bookingHorizonField.clear();
                    maxHoursField.clear();
                    rulesField.clear();
                    createButton.setDisable(false);
                    // Refresh resources list
                    refreshResourcesList();
                });

                createTask.setOnFailed(event -> {
                    String errorMsg = createTask.getException().getMessage();
                    statusLabel.setText("Failed to create resource: " + errorMsg);
                    statusLabel.setStyle(UIStyles.statusError());
                    createButton.setDisable(false);
                });

                new Thread(createTask).start();
            } catch (NumberFormatException ex) {
                statusLabel.setText("Please enter valid numbers for capacity, duration, and horizon");
                statusLabel.setStyle(UIStyles.statusError());
            }
        });

        content.getChildren().addAll(sectionTitle, form);
        Tab tab = new Tab("Create Resource", content);
        tab.setClosable(false);
        return tab;
    }

    private Tab createSimulateContentionTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle(String.format("-fx-background-color: %s;", UIStyles.BG_LIGHT));

        Label sectionTitle = new Label("⚡ Simulate Contention");
        sectionTitle.setStyle(UIStyles.sectionTitle());

        Label description = new Label("Simulate multiple users trying to book the same time slot simultaneously.");
        description.setWrapText(true);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setAlignment(Pos.CENTER_LEFT);

        TextField timeSlotIdField = new TextField();
        timeSlotIdField.setPromptText("Time Slot ID");
        timeSlotIdField.setPrefWidth(350);
        timeSlotIdField.setStyle(UIStyles.inputField());

        TextField numThreadsField = new TextField();
        numThreadsField.setPromptText("Number of concurrent threads");
        numThreadsField.setPrefWidth(350);
        numThreadsField.setStyle(UIStyles.inputField());

        TextField qtyField = new TextField();
        qtyField.setPromptText("Quantity per thread (default: 1)");
        qtyField.setPrefWidth(350);
        qtyField.setStyle(UIStyles.inputField());

        Label statusLabel = new Label();
        statusLabel.setWrapText(true);
        statusLabel.setPrefWidth(400);

        Button simulateButton = new Button("▶ Start Simulation");
        simulateButton.setStyle(String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 5;",
            UIStyles.WARNING_ORANGE
        ));
        simulateButton.setPrefWidth(180);

        form.add(new Label("Time Slot ID:"), 0, 0);
        form.add(timeSlotIdField, 1, 0);
        form.add(new Label("Number of Threads:"), 0, 1);
        form.add(numThreadsField, 1, 1);
        form.add(new Label("Quantity per Thread:"), 0, 2);
        form.add(qtyField, 1, 2);
        form.add(simulateButton, 1, 3);
        form.add(statusLabel, 0, 4, 2, 1);

        simulateButton.setOnAction(e -> {
            try {
                Long timeSlotId = Long.parseLong(timeSlotIdField.getText().trim());
                Integer numThreads = Integer.parseInt(numThreadsField.getText().trim());
                String qtyStr = qtyField.getText().trim();
                Integer qty = qtyStr.isEmpty() ? 1 : Integer.parseInt(qtyStr);

                if (numThreads <= 0) {
                    statusLabel.setText("Number of threads must be greater than 0");
                    statusLabel.setStyle(UIStyles.statusError());
                    return;
                }

                simulateButton.setDisable(true);
                statusLabel.setText("Starting simulation...");
                statusLabel.setStyle(UIStyles.statusInfo());

                Task<Map<String, Object>> simulateTask = new Task<Map<String, Object>>() {
                    @Override
                    protected Map<String, Object> call() throws Exception {
                        return apiClient.simulateContention(timeSlotId, numThreads, qty);
                    }
                };

                simulateTask.setOnSucceeded(event -> {
                    Map<String, Object> result = simulateTask.getValue();
                    String message = result != null && result.containsKey("message") 
                            ? (String) result.get("message") 
                            : "Simulation started";
                    statusLabel.setText(message);
                    statusLabel.setStyle(UIStyles.statusSuccess());
                    simulateButton.setDisable(false);
                });

                simulateTask.setOnFailed(event -> {
                    String errorMsg = simulateTask.getException().getMessage();
                    statusLabel.setText("Failed to start simulation: " + errorMsg);
                    statusLabel.setStyle(UIStyles.statusError());
                    simulateButton.setDisable(false);
                });

                new Thread(simulateTask).start();
            } catch (NumberFormatException ex) {
                statusLabel.setText("Please enter valid numbers");
                statusLabel.setStyle(UIStyles.statusError());
            }
        });

        content.getChildren().addAll(sectionTitle, description, form);
        Tab tab = new Tab("Simulate Contention", content);
        tab.setClosable(false);
        return tab;
    }

    private Tab createIsolationModeTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle(String.format("-fx-background-color: %s;", UIStyles.BG_LIGHT));

        Label sectionTitle = new Label("🔐 Transaction Isolation Mode");
        sectionTitle.setStyle(UIStyles.sectionTitle());

        Label description = new Label("Change the transaction isolation level for the database connection pool.");
        description.setWrapText(true);

        ComboBox<String> isolationCombo = new ComboBox<>();
        isolationCombo.getItems().addAll("READ_COMMITTED", "SERIALIZABLE");
        isolationCombo.setValue("READ_COMMITTED");
        isolationCombo.setPrefWidth(350);
        isolationCombo.setStyle(UIStyles.inputField());

        Label statusLabel = new Label();
        statusLabel.setWrapText(true);
        statusLabel.setPrefWidth(400);

        Button applyButton = new Button("✓ Apply Isolation Mode");
        applyButton.setStyle(UIStyles.primaryButton());
        applyButton.setPrefWidth(200);

        HBox hbox = new HBox(10, new Label("Isolation Level:"), isolationCombo, applyButton);
        hbox.setAlignment(Pos.CENTER_LEFT);

        applyButton.setOnAction(e -> {
            String level = isolationCombo.getValue();
            if (level == null) {
                statusLabel.setText("Please select an isolation level");
                statusLabel.setStyle(UIStyles.statusError());
                return;
            }

            applyButton.setDisable(true);
            statusLabel.setText("Applying isolation mode...");
            statusLabel.setStyle(UIStyles.statusInfo());

            Task<Map<String, Object>> applyTask = new Task<Map<String, Object>>() {
                @Override
                protected Map<String, Object> call() throws Exception {
                    return apiClient.setIsolationMode(level);
                }
            };

            applyTask.setOnSucceeded(event -> {
                Map<String, Object> result = applyTask.getValue();
                String message = result != null && result.containsKey("message") 
                        ? (String) result.get("message") 
                        : "Isolation mode changed";
                String note = result != null && result.containsKey("note") 
                        ? "\nNote: " + result.get("note") 
                        : "";
                statusLabel.setText(message + note);
                statusLabel.setStyle(UIStyles.statusSuccess());
                applyButton.setDisable(false);
            });

            applyTask.setOnFailed(event -> {
                String errorMsg = applyTask.getException().getMessage();
                statusLabel.setText("Failed to change isolation mode: " + errorMsg);
                statusLabel.setStyle(UIStyles.statusError());
                applyButton.setDisable(false);
            });

            new Thread(applyTask).start();
        });

        content.getChildren().addAll(sectionTitle, description, hbox, statusLabel);
        Tab tab = new Tab("Isolation Mode", content);
        tab.setClosable(false);
        return tab;
    }

    private Tab createResourcesListTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle(String.format("-fx-background-color: %s;", UIStyles.BG_LIGHT));

        Label sectionTitle = new Label("📚 All Resources");
        sectionTitle.setStyle(UIStyles.sectionTitle());

        Button refreshButton = new Button("🔄 Refresh");
        refreshButton.setStyle(UIStyles.secondaryButton());
        refreshButton.setOnAction(e -> refreshResourcesList());

        resourcesContainer = new VBox(10);
        resourcesContainer.setStyle(String.format(
            "-fx-background-color: %s; " +
            "-fx-background-radius: 8; " +
            "-fx-border-color: %s; " +
            "-fx-border-radius: 8; " +
            "-fx-padding: 15;",
            UIStyles.BG_WHITE, UIStyles.BORDER_LIGHT
        ));

        ScrollPane scrollPane = new ScrollPane(resourcesContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 0, 15, 0));
        topBar.getChildren().addAll(sectionTitle, refreshButton);

        content.getChildren().addAll(topBar, scrollPane);

        // Initial load
        refreshResourcesList();

        Tab tab = new Tab("Resources List", content);
        tab.setClosable(false);
        return tab;
    }

    private void refreshResourcesList() {
        Task<List<Map<String, Object>>> loadTask = new Task<List<Map<String, Object>>>() {
            @Override
            protected List<Map<String, Object>> call() throws Exception {
                return apiClient.getResources();
            }
        };

        loadTask.setOnSucceeded(event -> {
            try {
                List<Map<String, Object>> resources = loadTask.getValue();
                Platform.runLater(() -> {
                    if (resourcesContainer != null) {
                        resourcesContainer.getChildren().clear();
                        if (resources == null || resources.isEmpty()) {
                            Label noResources = new Label("No resources found");
                            noResources.setStyle(String.format("-fx-font-size: 14px; -fx-text-fill: %s;", UIStyles.TEXT_LIGHT));
                            resourcesContainer.getChildren().add(noResources);
                        } else {
                            for (Map<String, Object> resource : resources) {
                                Long id = ((Number) resource.get("id")).longValue();
                                String name = (String) resource.get("name");
                                Integer capacity = ((Number) resource.get("capacity")).intValue();
                                Integer slotDuration = ((Number) resource.get("slotDurationMinutes")).intValue();
                                Integer bookingHorizon = ((Number) resource.get("bookingHorizonDays")).intValue();
                                
                                // Create resource card with expandable time slots
                                VBox resourceCard = createResourceCardWithTimeSlots(id, name, capacity, slotDuration, bookingHorizon);
                                resourcesContainer.getChildren().add(resourceCard);
                            }
                        }
                    }
                });
            } catch (Exception ex) {
                System.err.println("Failed to refresh resources: " + ex.getMessage());
            }
        });

        loadTask.setOnFailed(event -> {
            System.err.println("Failed to load resources: " + loadTask.getException().getMessage());
        });

        new Thread(loadTask).start();
    }

    private VBox createResourceCardWithTimeSlots(Long resourceId, String resourceName, 
                                                  Integer capacity, Integer slotDuration, Integer bookingHorizon) {
        VBox resourceCard = new VBox(10);
        resourceCard.setStyle(UIStyles.sectionCard());
        resourceCard.setPadding(new Insets(15));
        
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label nameLabel = new Label(resourceName);
        nameLabel.setStyle(String.format(
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: %s;",
            UIStyles.NYU_VIOLET
        ));
        
        Button showTimeSlotsButton = new Button("📅 View Time Slots");
        showTimeSlotsButton.setStyle(UIStyles.secondaryButton());
        showTimeSlotsButton.setPrefWidth(150);
        
        Button deleteResourceButton = new Button("🗑️ Delete Category");
        deleteResourceButton.setStyle(UIStyles.dangerButton());
        deleteResourceButton.setPrefWidth(150);
        deleteResourceButton.setOnAction(e -> deleteResource(resourceId, resourceName));
        
        HBox.setHgrow(nameLabel, javafx.scene.layout.Priority.ALWAYS);
        headerBox.getChildren().addAll(nameLabel, showTimeSlotsButton, deleteResourceButton);
        
        Label detailsLabel = new Label(String.format(
            "ID: %d | Capacity: %d | Slot Duration: %d min | Booking Horizon: %d days",
            resourceId, capacity, slotDuration, bookingHorizon
        ));
        detailsLabel.setStyle(String.format("-fx-font-size: 12px; -fx-text-fill: %s;", UIStyles.TEXT_LIGHT));
        
        VBox timeSlotsContainer = new VBox(5);
        timeSlotsContainer.setVisible(false);
        timeSlotsContainer.setManaged(false);
        
        showTimeSlotsButton.setOnAction(e -> {
            if (timeSlotsContainer.isVisible()) {
                timeSlotsContainer.setVisible(false);
                timeSlotsContainer.setManaged(false);
                showTimeSlotsButton.setText("📅 View Time Slots");
            } else {
                loadTimeSlotsForResource(resourceId, resourceName, timeSlotsContainer);
                timeSlotsContainer.setVisible(true);
                timeSlotsContainer.setManaged(true);
                showTimeSlotsButton.setText("🔼 Hide Time Slots");
            }
        });
        
        resourceCard.getChildren().addAll(headerBox, detailsLabel, timeSlotsContainer);
        return resourceCard;
    }

    private void loadTimeSlotsForResource(Long resourceId, String resourceName, VBox container) {
        container.getChildren().clear();
        container.getChildren().add(new Label("Loading time slots..."));
        
        Task<List<Map<String, Object>>> loadTask = new Task<List<Map<String, Object>>>() {
            @Override
            protected List<Map<String, Object>> call() throws Exception {
                return apiClient.getTimeSlotsForResource(resourceId);
            }
        };
        
        loadTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                try {
                    List<Map<String, Object>> timeSlots = loadTask.getValue();
                    container.getChildren().clear();
                    
                    if (timeSlots == null || timeSlots.isEmpty()) {
                        Label noSlots = new Label("No time slots found");
                        noSlots.setStyle(String.format("-fx-font-size: 12px; -fx-text-fill: %s;", UIStyles.TEXT_LIGHT));
                        container.getChildren().add(noSlots);
                        return;
                    }
                    
                    Label slotsTitle = new Label("Time Slots:");
                    slotsTitle.setStyle(String.format("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: %s;", UIStyles.TEXT_DARK));
                    container.getChildren().add(slotsTitle);
                    
                    for (Map<String, Object> slot : timeSlots) {
                        Long slotId = ((Number) slot.get("id")).longValue();
                        String startTs = (String) slot.get("startTs");
                        String endTs = (String) slot.get("endTs");
                        Integer capacityRemaining = ((Number) slot.get("capacityRemaining")).intValue();
                        
                        HBox slotRow = new HBox(10);
                        slotRow.setAlignment(Pos.CENTER_LEFT);
                        slotRow.setPadding(new Insets(5));
                        slotRow.setStyle(String.format(
                            "-fx-background-color: %s; " +
                            "-fx-background-radius: 5; " +
                            "-fx-padding: 8;",
                            UIStyles.BG_LIGHT
                        ));
                        
                        Label slotInfo = new Label(String.format(
                            "ID: %d | %s - %s | Capacity: %d",
                            slotId, startTs, endTs, capacityRemaining
                        ));
                        slotInfo.setStyle(String.format("-fx-font-size: 12px; -fx-text-fill: %s;", UIStyles.TEXT_DARK));
                        
                        Button deleteSlotButton = new Button("🗑️ Delete");
                        deleteSlotButton.setStyle(UIStyles.dangerButton());
                        deleteSlotButton.setPrefWidth(100);
                        deleteSlotButton.setOnAction(e -> deleteTimeSlot(slotId, resourceName, startTs, endTs));
                        
                        HBox.setHgrow(slotInfo, javafx.scene.layout.Priority.ALWAYS);
                        slotRow.getChildren().addAll(slotInfo, deleteSlotButton);
                        container.getChildren().add(slotRow);
                    }
                } catch (Exception ex) {
                    container.getChildren().clear();
                    Label errorLabel = new Label("Failed to load time slots: " + ex.getMessage());
                    errorLabel.setStyle(UIStyles.statusError());
                    container.getChildren().add(errorLabel);
                }
            });
        });
        
        loadTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                container.getChildren().clear();
                Label errorLabel = new Label("Failed to load time slots: " + loadTask.getException().getMessage());
                errorLabel.setStyle(UIStyles.statusError());
                container.getChildren().add(errorLabel);
            });
        });
        
        new Thread(loadTask).start();
    }

    private void deleteTimeSlot(Long timeSlotId, String resourceName, String startTs, String endTs) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Time Slot");
        confirmDialog.setHeaderText("Delete Time Slot Instance");
        confirmDialog.setContentText(String.format(
            "Are you sure you want to delete this time slot?\n\n" +
            "Resource: %s\n" +
            "Time: %s - %s\n\n" +
            "⚠️ WARNING: This will delete:\n" +
            "• This specific time slot instance\n" +
            "• All reservations for this time slot\n" +
            "• All holds for this time slot\n" +
            "• All waitlist entries for this time slot\n\n" +
            "The resource category will remain intact.\n" +
            "Affected users will be notified.",
            resourceName, startTs, endTs
        ));
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Task<Void> deleteTask = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        apiClient.deleteTimeSlot(timeSlotId);
                        return null;
                    }
                };

                deleteTask.setOnSucceeded(event -> {
                    Platform.runLater(() -> {
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Success");
                        successAlert.setHeaderText("Time Slot Deleted");
                        successAlert.setContentText(String.format(
                            "Time slot for '%s' (%s - %s) has been deleted successfully.\n" +
                            "All related reservations, holds, and waitlist entries have been removed.\n" +
                            "Affected users have been notified.\n\n" +
                            "The resource category remains intact.",
                            resourceName, startTs, endTs
                        ));
                        successAlert.showAndWait();
                        refreshResourcesList(); // Refresh to update time slots
                    });
                });

                deleteTask.setOnFailed(event -> {
                    Platform.runLater(() -> {
                        String errorMsg = deleteTask.getException().getMessage();
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Error");
                        errorAlert.setHeaderText("Failed to Delete Time Slot");
                        errorAlert.setContentText("Failed to delete time slot: " + errorMsg);
                        errorAlert.showAndWait();
                    });
                });

                new Thread(deleteTask).start();
            }
        });
    }

    private void deleteResource(Long resourceId, String resourceName) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Resource Category");
        confirmDialog.setHeaderText("Delete Entire Resource Category");
        confirmDialog.setContentText(String.format(
            "Are you sure you want to delete the entire resource category '%s'?\n\n" +
            "⚠️ WARNING: This will delete:\n" +
            "• The entire resource category\n" +
            "• ALL time slots for this resource\n" +
            "• ALL reservations for this resource\n" +
            "• ALL holds for this resource\n" +
            "• ALL waitlist entries for this resource\n\n" +
            "This action cannot be undone.\n" +
            "Affected users will be notified.",
            resourceName
        ));
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Task<Void> deleteTask = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        apiClient.deleteResource(resourceId);
                        return null;
                    }
                };

                deleteTask.setOnSucceeded(event -> {
                    Platform.runLater(() -> {
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Success");
                        successAlert.setHeaderText("Resource Category Deleted");
                        successAlert.setContentText(String.format(
                            "Resource category '%s' has been deleted successfully.\n" +
                            "All related time slots, reservations, holds, and waitlist entries have been removed.\n" +
                            "Affected users have been notified.",
                            resourceName
                        ));
                        successAlert.showAndWait();
                        refreshResourcesList(); // Refresh the list
                    });
                });

                deleteTask.setOnFailed(event -> {
                    Platform.runLater(() -> {
                        String errorMsg = deleteTask.getException().getMessage();
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Error");
                        errorAlert.setHeaderText("Failed to Delete Resource Category");
                        errorAlert.setContentText("Failed to delete resource category: " + errorMsg);
                        errorAlert.showAndWait();
                    });
                });

                new Thread(deleteTask).start();
            }
        });
    }

    public VBox getView() {
        return view;
    }
}
