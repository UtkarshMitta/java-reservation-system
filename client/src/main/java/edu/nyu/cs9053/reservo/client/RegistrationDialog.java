package edu.nyu.cs9053.reservo.client;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class RegistrationDialog {
    private String email;
    private String username;
    private String password;
    private boolean registered = false;

    public boolean showAndWait(Stage parentStage) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parentStage);
        dialog.setTitle("New User? Register");

        VBox root = new VBox(20);
        root.setStyle(String.format(
            "-fx-padding: 30; " +
            "-fx-background-color: %s; " +
            "-fx-background-radius: 10;",
            UIStyles.BG_WHITE
        ));
        root.setPrefWidth(450);

        Label titleLabel = new Label("✨ Create New NYU Account");
        titleLabel.setStyle(UIStyles.sectionTitle());
        titleLabel.setAlignment(Pos.CENTER);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10));

        Label emailLabel = new Label("Email (@nyu.edu):");
        emailLabel.setStyle(String.format("-fx-font-size: 13px; -fx-text-fill: %s;", UIStyles.TEXT_DARK));
        TextField emailField = new TextField();
        emailField.setPromptText("username@nyu.edu");
        emailField.setPrefWidth(280);
        emailField.setStyle(UIStyles.inputField());
        grid.add(emailLabel, 0, 0);
        grid.add(emailField, 1, 0);

        Label usernameLabel = new Label("Username:");
        usernameLabel.setStyle(String.format("-fx-font-size: 13px; -fx-text-fill: %s;", UIStyles.TEXT_DARK));
        TextField usernameField = new TextField();
        usernameField.setPromptText("Choose a username");
        usernameField.setPrefWidth(280);
        usernameField.setStyle(UIStyles.inputField());
        grid.add(usernameLabel, 0, 1);
        grid.add(usernameField, 1, 1);

        Label passwordLabel = new Label("Password:");
        passwordLabel.setStyle(String.format("-fx-font-size: 13px; -fx-text-fill: %s;", UIStyles.TEXT_DARK));
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("At least 3 characters");
        passwordField.setPrefWidth(280);
        passwordField.setStyle(UIStyles.inputField());
        grid.add(passwordLabel, 0, 2);
        grid.add(passwordField, 1, 2);

        Label confirmPasswordLabel = new Label("Confirm Password:");
        confirmPasswordLabel.setStyle(String.format("-fx-font-size: 13px; -fx-text-fill: %s;", UIStyles.TEXT_DARK));
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Re-enter password");
        confirmPasswordField.setPrefWidth(280);
        confirmPasswordField.setStyle(UIStyles.inputField());
        grid.add(confirmPasswordLabel, 0, 3);
        grid.add(confirmPasswordField, 1, 3);

        Label statusLabel = new Label();
        statusLabel.setWrapText(true);
        statusLabel.setPrefWidth(400);
        statusLabel.setAlignment(Pos.CENTER);
        GridPane.setColumnSpan(statusLabel, 2);
        grid.add(statusLabel, 0, 4);

        Button registerButton = new Button("✓ Register");
        registerButton.setStyle(UIStyles.successButton());
        registerButton.setPrefWidth(140);

        Button cancelButton = new Button("✗ Cancel");
        cancelButton.setStyle(UIStyles.secondaryButton());
        cancelButton.setPrefWidth(140);

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(registerButton, cancelButton);
        GridPane.setColumnSpan(buttonBox, 2);
        grid.add(buttonBox, 0, 5);
        
        root.getChildren().addAll(titleLabel, grid);

        registerButton.setOnAction(e -> {
            String emailText = emailField.getText().trim();
            String usernameText = usernameField.getText().trim();
            String passwordText = passwordField.getText();
            String confirmPasswordText = confirmPasswordField.getText();

            // Validation
            if (emailText.isEmpty()) {
                statusLabel.setText("Email is required");
                statusLabel.setStyle(UIStyles.statusError());
                return;
            }

            if (!emailText.toLowerCase().endsWith("@nyu.edu")) {
                statusLabel.setText("Email must be from @nyu.edu domain");
                statusLabel.setStyle(UIStyles.statusError());
                return;
            }

            if (usernameText.isEmpty()) {
                statusLabel.setText("Username is required");
                statusLabel.setStyle(UIStyles.statusError());
                return;
            }

            if (passwordText.isEmpty()) {
                statusLabel.setText("Password is required");
                statusLabel.setStyle(UIStyles.statusError());
                return;
            }

            if (passwordText.length() < 3) {
                statusLabel.setText("Password must be at least 3 characters");
                statusLabel.setStyle(UIStyles.statusError());
                return;
            }

            if (!passwordText.equals(confirmPasswordText)) {
                statusLabel.setText("Passwords do not match");
                statusLabel.setStyle(UIStyles.statusError());
                return;
            }

            // Store values
            this.email = emailText;
            this.username = usernameText;
            this.password = passwordText;
            this.registered = true;
            dialog.close();
        });

        cancelButton.setOnAction(e -> {
            this.registered = false;
            dialog.close();
        });

        Scene scene = new Scene(root, 480, 420);
        scene.setFill(javafx.scene.paint.Color.valueOf(UIStyles.NYU_VIOLET_PALE));
        dialog.setScene(scene);
        dialog.setResizable(false);
        dialog.showAndWait();

        return registered;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}

