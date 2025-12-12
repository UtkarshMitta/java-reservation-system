package edu.nyu.cs9053.reservo.client;

import edu.nyu.cs9053.reservo.client.api.ApiClient;
import edu.nyu.cs9053.reservo.client.websocket.WebSocketClientWrapper;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Map;

public class MainController {
    private ApiClient apiClient;
    private WebSocketClientWrapper wsClient;
    private String currentUser;
    private Boolean isAdmin;
    private Stage primaryStage;

    public void start(Stage stage) {
        this.primaryStage = stage;
        this.apiClient = new ApiClient();
        this.wsClient = new WebSocketClientWrapper();

        showLoginScreen();
    }

    private void showLoginScreen() {
        VBox root = new VBox(20);
        root.setStyle(String.format(
            "-fx-padding: 40; " +
            "-fx-alignment: center; " +
            "-fx-background-color: linear-gradient(to bottom, %s, %s);",
            UIStyles.NYU_VIOLET_PALE, UIStyles.BG_WHITE
        ));
        root.setPrefWidth(450);
        root.setPrefHeight(500);

        // NYU Logo/Title Section
        VBox titleSection = new VBox(5);
        titleSection.setAlignment(Pos.CENTER);
        
        Label title = new Label("Reservo");
        title.setStyle(UIStyles.pageTitle());
        
        Label subtitle = new Label("NYU Resource Reservation System");
        subtitle.setStyle(String.format("-fx-font-size: 14px; -fx-text-fill: %s;", UIStyles.TEXT_LIGHT));
        
        titleSection.getChildren().addAll(title, subtitle);

        // Form Container
        VBox formContainer = new VBox(15);
        formContainer.setStyle(UIStyles.cardStyle());
        formContainer.setPrefWidth(350);
        formContainer.setAlignment(Pos.CENTER);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefWidth(280);
        usernameField.setStyle(UIStyles.inputField());

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefWidth(280);
        passwordField.setStyle(UIStyles.inputField());

        Button loginButton = new Button("Login");
        loginButton.setPrefWidth(280);
        loginButton.setStyle(UIStyles.primaryButton());
        loginButton.setOnMouseEntered(e -> loginButton.setStyle(UIStyles.primaryButtonHover()));
        loginButton.setOnMouseExited(e -> loginButton.setStyle(UIStyles.primaryButton()));
        
        Button registerButton = new Button("New User? Register");
        registerButton.setPrefWidth(280);
        registerButton.setStyle(UIStyles.successButton());

        Label statusLabel = new Label();
        statusLabel.setWrapText(true);
        statusLabel.setPrefWidth(280);
        statusLabel.setAlignment(Pos.CENTER);

            loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            
            if (username.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Please enter username and password");
                statusLabel.setStyle(UIStyles.statusError());
                return;
            }
            
            loginButton.setDisable(true);
            statusLabel.setText("Logging in...");
            statusLabel.setStyle(UIStyles.statusInfo());
            
            Task<Map<String, Object>> loginTask = new Task<Map<String, Object>>() {
                @Override
                protected Map<String, Object> call() throws Exception {
                    return apiClient.login(username, password);
                }
            };
            
            loginTask.setOnSucceeded(event -> {
                try {
                    Map<String, Object> response = loginTask.getValue();
                    handleLoginSuccess(response);
                } catch (Exception ex) {
                    statusLabel.setText("Login failed: " + ex.getMessage());
                    statusLabel.setStyle(UIStyles.statusError());
                    loginButton.setDisable(false);
                }
            });
            
            loginTask.setOnFailed(event -> {
                statusLabel.setText("Login failed: " + loginTask.getException().getMessage());
                statusLabel.setStyle(UIStyles.statusError());
                loginButton.setDisable(false);
            });
            
            new Thread(loginTask).start();
        });

        registerButton.setOnAction(e -> {
            RegistrationDialog dialog = new RegistrationDialog();
            boolean registered = dialog.showAndWait(primaryStage);
            
            if (registered) {
                String email = dialog.getEmail();
                String username = dialog.getUsername();
                String password = dialog.getPassword();
                
                registerButton.setDisable(true);
                loginButton.setDisable(true);
                statusLabel.setText("Registering new account...");
                statusLabel.setStyle(UIStyles.statusInfo());
                
                Task<Map<String, Object>> registerTask = new Task<Map<String, Object>>() {
                    @Override
                    protected Map<String, Object> call() throws Exception {
                        return apiClient.register(username, password, email);
                    }
                };
                
                registerTask.setOnSucceeded(event -> {
                    try {
                        Map<String, Object> response = registerTask.getValue();
                    if (response != null && response.containsKey("token")) {
                        statusLabel.setText("Registration successful! Logging you in...");
                        statusLabel.setStyle(UIStyles.statusSuccess());
                            // Clear fields
                            usernameField.clear();
                            passwordField.clear();
                            handleLoginSuccess(response);
                    } else {
                        statusLabel.setText("Registration failed: Invalid response from server");
                        statusLabel.setStyle(UIStyles.statusError());
                        registerButton.setDisable(false);
                        loginButton.setDisable(false);
                    }
                } catch (Exception ex) {
                    String errorMsg = ex.getMessage();
                    if (errorMsg != null && errorMsg.contains("error")) {
                        // Try to parse error from response
                        try {
                            com.google.gson.JsonObject errorJson = com.google.gson.JsonParser.parseString(errorMsg).getAsJsonObject();
                            if (errorJson.has("error")) {
                                errorMsg = errorJson.get("error").getAsString();
                            }
                        } catch (Exception ignored) {}
                    }
                    statusLabel.setText("Registration failed: " + errorMsg);
                    statusLabel.setStyle(UIStyles.statusError());
                    registerButton.setDisable(false);
                    loginButton.setDisable(false);
                }
            });
            
            registerTask.setOnFailed(event -> {
                Throwable exception = registerTask.getException();
                String errorMsg = "Unknown error";
                if (exception != null) {
                    errorMsg = exception.getMessage();
                    // Try to extract error message from IOException
                    if (errorMsg != null && errorMsg.contains("Registration failed:")) {
                        errorMsg = errorMsg.substring(errorMsg.indexOf("Registration failed:") + "Registration failed:".length()).trim();
                        // Try to parse JSON error
                        try {
                            com.google.gson.JsonObject errorJson = com.google.gson.JsonParser.parseString(errorMsg).getAsJsonObject();
                            if (errorJson.has("error")) {
                                errorMsg = errorJson.get("error").getAsString();
                            }
                        } catch (Exception ignored) {}
                    }
                }
                statusLabel.setText("Registration failed: " + errorMsg);
                statusLabel.setStyle(UIStyles.statusError());
                registerButton.setDisable(false);
                loginButton.setDisable(false);
            });
                
                new Thread(registerTask).start();
            }
        });

        formContainer.getChildren().addAll(
            new Label("Sign In"), usernameField, passwordField, 
            loginButton, registerButton, statusLabel
        );
        
        root.getChildren().addAll(titleSection, formContainer);

        Scene scene = new Scene(root, 450, 500);
        primaryStage.setTitle("Reservo - NYU Resource Reservation");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void handleLoginSuccess(Map<String, Object> response) {
        String token = (String) response.get("token");
        apiClient.setAuthToken(token);
        currentUser = (String) response.get("username");
        isAdmin = (Boolean) response.get("isAdmin");

        // Connect WebSocket
        wsClient.connect("http://localhost:8081");
        wsClient.setEventHandler(this::handleWebSocketEvent);

        showMainScreen();
    }

    private void handleWebSocketEvent(com.google.gson.JsonObject event) {
        Platform.runLater(() -> {
            // Handle real-time events
            String type = event.get("type").getAsString();
            System.out.println("Received event: " + type);
            // Refresh views as needed
        });
    }

    private void showMainScreen() {
        TabPane tabPane = new TabPane();
        tabPane.setStyle(String.format(
            "-fx-background-color: %s; " +
            "-fx-tab-min-width: 120px; " +
            "-fx-tab-min-height: 35px;",
            UIStyles.BG_LIGHT
        ));
        
        // Style tabs with NYU violet
        String tabStyle = String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: %s; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: bold;",
            UIStyles.BG_WHITE, UIStyles.TEXT_DARK
        );
        
        String selectedTabStyle = String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: bold;",
            UIStyles.NYU_VIOLET
        );

        Tab calendarTab = new Tab("📅 Calendar", new CalendarViewController(apiClient, wsClient).getView());
        calendarTab.setClosable(false);
        calendarTab.setStyle(tabStyle);

        Tab reservationsTab = new Tab("✅ Reservations", 
                new ReservationsViewController(apiClient).getView());
        reservationsTab.setClosable(false);
        reservationsTab.setStyle(tabStyle);

        Tab holdsTab = new Tab("⏳ Holds", 
                new HoldsViewController(apiClient).getView());
        holdsTab.setClosable(false);
        holdsTab.setStyle(tabStyle);

        Tab waitlistTab = new Tab("📋 Waitlist", 
                new WaitlistViewController(apiClient).getView());
        waitlistTab.setClosable(false);
        waitlistTab.setStyle(tabStyle);

        Tab notificationsTab = new Tab("🔔 Notifications", 
                new NotificationsViewController(apiClient).getView());
        notificationsTab.setClosable(false);
        notificationsTab.setStyle(tabStyle);

        Tab accountSettingsTab = new Tab("⚙️ Settings", 
                new AccountSettingsViewController(apiClient, this::handleLogout).getView());
        accountSettingsTab.setClosable(false);
        accountSettingsTab.setStyle(tabStyle);

        tabPane.getTabs().addAll(calendarTab, reservationsTab, holdsTab, waitlistTab, notificationsTab, accountSettingsTab);

        if (isAdmin != null && isAdmin) {
            Tab adminTab = new Tab("👑 Admin", new AdminViewController(apiClient).getView());
            adminTab.setClosable(false);
            adminTab.setStyle(tabStyle);
            tabPane.getTabs().add(adminTab);
        }

        Scene scene = new Scene(tabPane, 1200, 800);
        primaryStage.setTitle("Reservo - " + currentUser + " | NYU Resource Reservation");
        primaryStage.setScene(scene);
    }

    private void handleLogout() {
        // Clear authentication
        apiClient.setAuthToken(null);
        wsClient.disconnect();
        
        // Show login screen again
        showLoginScreen();
    }
}

