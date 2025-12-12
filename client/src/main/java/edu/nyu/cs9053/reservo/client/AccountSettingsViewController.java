package edu.nyu.cs9053.reservo.client;

import edu.nyu.cs9053.reservo.client.api.ApiClient;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.Map;

public class AccountSettingsViewController {
    private ApiClient apiClient;
    private Runnable onLogout;
    private Label currentEmailValue;

    public AccountSettingsViewController(ApiClient apiClient, Runnable onLogout) {
        this.apiClient = apiClient;
        this.onLogout = onLogout;
    }

    public VBox getView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle(String.format("-fx-background-color: %s;", UIStyles.BG_LIGHT));

        Label title = new Label("⚙️ Account Settings");
        title.setStyle(UIStyles.pageTitle());

        // Email Section
        VBox emailSection = createEmailSection();
        
        // Password Section
        VBox passwordSection = createPasswordSection();

        // Logout Button
        Button logoutButton = new Button("🚪 Logout");
        logoutButton.setStyle(UIStyles.dangerButton());
        logoutButton.setPrefWidth(200);
        logoutButton.setOnAction(e -> {
            if (onLogout != null) {
                onLogout.run();
            }
        });

        root.getChildren().addAll(title, emailSection, passwordSection, logoutButton);

        // Load current profile
        loadProfile();

        return root;
    }

    private VBox createEmailSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(20));
        section.setStyle(UIStyles.sectionCard());

        Label sectionTitle = new Label("📧 Email Address");
        sectionTitle.setStyle(UIStyles.sectionTitle());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER_LEFT);

        Label currentEmailLabel = new Label("Current Email:");
        currentEmailValue = new Label("Loading...");

        TextField newEmailField = new TextField();
        newEmailField.setPromptText("newemail@nyu.edu");
        newEmailField.setPrefWidth(300);
        newEmailField.setStyle(UIStyles.inputField());

        Button updateEmailButton = new Button("✓ Update Email");
        updateEmailButton.setStyle(UIStyles.primaryButton());

        Label emailStatusLabel = new Label();
        emailStatusLabel.setWrapText(true);
        emailStatusLabel.setPrefWidth(400);

        grid.add(currentEmailLabel, 0, 0);
        grid.add(currentEmailValue, 1, 0);
        grid.add(new Label("New Email:"), 0, 1);
        grid.add(newEmailField, 1, 1);
        grid.add(updateEmailButton, 1, 2);
        grid.add(emailStatusLabel, 0, 3, 2, 1);

        updateEmailButton.setOnAction(e -> {
            String newEmail = newEmailField.getText().trim();
            
            if (newEmail.isEmpty()) {
                emailStatusLabel.setText("Email is required");
                emailStatusLabel.setStyle(UIStyles.statusError());
                return;
            }

            if (!newEmail.toLowerCase().endsWith("@nyu.edu")) {
                emailStatusLabel.setText("Email must be from @nyu.edu domain");
                emailStatusLabel.setStyle(UIStyles.statusError());
                return;
            }

            updateEmailButton.setDisable(true);
            emailStatusLabel.setText("Updating email...");
            emailStatusLabel.setStyle(UIStyles.statusInfo());

            Task<Void> updateTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    apiClient.updateEmail(newEmail);
                    return null;
                }
            };

            updateTask.setOnSucceeded(event -> {
                emailStatusLabel.setText("Email updated successfully!");
                emailStatusLabel.setStyle(UIStyles.statusSuccess());
                newEmailField.clear();
                updateEmailButton.setDisable(false);
                loadProfile(); // Reload profile to show new email
            });

            updateTask.setOnFailed(event -> {
                String errorMsg = updateTask.getException().getMessage();
                if (errorMsg != null && errorMsg.contains("error")) {
                    try {
                        com.google.gson.JsonObject errorJson = com.google.gson.JsonParser.parseString(errorMsg).getAsJsonObject();
                        if (errorJson.has("error")) {
                            errorMsg = errorJson.get("error").getAsString();
                        }
                    } catch (Exception ignored) {}
                }
                emailStatusLabel.setText("Failed to update email: " + errorMsg);
                emailStatusLabel.setStyle(UIStyles.statusError());
                updateEmailButton.setDisable(false);
            });

            new Thread(updateTask).start();
        });

        section.getChildren().addAll(sectionTitle, grid);
        return section;
    }

    private VBox createPasswordSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(20));
        section.setStyle(UIStyles.sectionCard());

        Label sectionTitle = new Label("🔒 Change Password");
        sectionTitle.setStyle(UIStyles.sectionTitle());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER_LEFT);

        PasswordField oldPasswordField = new PasswordField();
        oldPasswordField.setPromptText("Current password");
        oldPasswordField.setPrefWidth(300);
        oldPasswordField.setStyle(UIStyles.inputField());

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New password (min 3 characters)");
        newPasswordField.setPrefWidth(300);
        newPasswordField.setStyle(UIStyles.inputField());

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm new password");
        confirmPasswordField.setPrefWidth(300);
        confirmPasswordField.setStyle(UIStyles.inputField());

        Button changePasswordButton = new Button("✓ Change Password");
        changePasswordButton.setStyle(UIStyles.primaryButton());

        Label passwordStatusLabel = new Label();
        passwordStatusLabel.setWrapText(true);
        passwordStatusLabel.setPrefWidth(400);

        grid.add(new Label("Current Password:"), 0, 0);
        grid.add(oldPasswordField, 1, 0);
        grid.add(new Label("New Password:"), 0, 1);
        grid.add(newPasswordField, 1, 1);
        grid.add(new Label("Confirm Password:"), 0, 2);
        grid.add(confirmPasswordField, 1, 2);
        grid.add(changePasswordButton, 1, 3);
        grid.add(passwordStatusLabel, 0, 4, 2, 1);

        changePasswordButton.setOnAction(e -> {
            String oldPassword = oldPasswordField.getText();
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (oldPassword.isEmpty()) {
                passwordStatusLabel.setText("Current password is required");
                passwordStatusLabel.setStyle(UIStyles.statusError());
                return;
            }

            if (newPassword.isEmpty()) {
                passwordStatusLabel.setText("New password is required");
                passwordStatusLabel.setStyle(UIStyles.statusError());
                return;
            }

            if (newPassword.length() < 3) {
                passwordStatusLabel.setText("New password must be at least 3 characters");
                passwordStatusLabel.setStyle(UIStyles.statusError());
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                passwordStatusLabel.setText("New passwords do not match");
                passwordStatusLabel.setStyle(UIStyles.statusError());
                return;
            }

            changePasswordButton.setDisable(true);
            passwordStatusLabel.setText("Changing password...");
            passwordStatusLabel.setStyle(UIStyles.statusInfo());

            Task<Void> changeTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    apiClient.changePassword(oldPassword, newPassword);
                    return null;
                }
            };

            changeTask.setOnSucceeded(event -> {
                passwordStatusLabel.setText("Password changed successfully!");
                passwordStatusLabel.setStyle(UIStyles.statusSuccess());
                oldPasswordField.clear();
                newPasswordField.clear();
                confirmPasswordField.clear();
                changePasswordButton.setDisable(false);
            });

            changeTask.setOnFailed(event -> {
                String errorMsg = changeTask.getException().getMessage();
                if (errorMsg != null && errorMsg.contains("error")) {
                    try {
                        com.google.gson.JsonObject errorJson = com.google.gson.JsonParser.parseString(errorMsg).getAsJsonObject();
                        if (errorJson.has("error")) {
                            errorMsg = errorJson.get("error").getAsString();
                        }
                    } catch (Exception ignored) {}
                }
                passwordStatusLabel.setText("Failed to change password: " + errorMsg);
                passwordStatusLabel.setStyle(UIStyles.statusError());
                changePasswordButton.setDisable(false);
            });

            new Thread(changeTask).start();
        });

        section.getChildren().addAll(sectionTitle, grid);
        return section;
    }

    private void loadProfile() {
        Task<Map<String, Object>> profileTask = new Task<Map<String, Object>>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                return apiClient.getProfile();
            }
        };

        profileTask.setOnSucceeded(event -> {
            try {
                Map<String, Object> profile = profileTask.getValue();
                if (profile != null && profile.containsKey("email")) {
                    String email = (String) profile.get("email");
                    if (email == null || email.isEmpty()) {
                        email = "Not set";
                    }
                    final String finalEmail = email;
                    Platform.runLater(() -> {
                        if (currentEmailValue != null) {
                            currentEmailValue.setText(finalEmail);
                        }
                    });
                }
            } catch (Exception ex) {
                System.err.println("Failed to load profile: " + ex.getMessage());
                Platform.runLater(() -> {
                    if (currentEmailValue != null) {
                        currentEmailValue.setText("Error loading email");
                    }
                });
            }
        });

        profileTask.setOnFailed(event -> {
            System.err.println("Failed to load profile: " + profileTask.getException().getMessage());
            Platform.runLater(() -> {
                if (currentEmailValue != null) {
                    currentEmailValue.setText("Error loading email");
                }
            });
        });

        new Thread(profileTask).start();
    }
}

