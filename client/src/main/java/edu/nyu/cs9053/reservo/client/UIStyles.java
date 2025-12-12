package edu.nyu.cs9053.reservo.client;

/**
 * Centralized UI styling constants for NYU-themed violet color scheme
 */
public class UIStyles {
    // NYU Brand Colors
    public static final String NYU_VIOLET = "#57068C";
    public static final String NYU_VIOLET_DARK = "#3D0459";
    public static final String NYU_VIOLET_LIGHT = "#7A0DB8";
    public static final String NYU_VIOLET_PALE = "#E8D5F2";
    
    // Accent Colors
    public static final String ACCENT_GOLD = "#FFD700";
    public static final String ACCENT_BLUE = "#4A90E2";
    
    // Status Colors
    public static final String SUCCESS_GREEN = "#4CAF50";
    public static final String ERROR_RED = "#F44336";
    public static final String WARNING_ORANGE = "#FF9800";
    public static final String INFO_BLUE = "#2196F3";
    
    // Neutral Colors
    public static final String BG_LIGHT = "#F5F5F5";
    public static final String BG_WHITE = "#FFFFFF";
    public static final String TEXT_DARK = "#212121";
    public static final String TEXT_LIGHT = "#757575";
    public static final String BORDER_LIGHT = "#E0E0E0";
    
    // Button Styles
    public static String primaryButton() {
        return String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 5; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(87,6,140,0.3), 5, 0, 0, 2);",
            NYU_VIOLET
        );
    }
    
    public static String primaryButtonHover() {
        return String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 5; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(87,6,140,0.5), 8, 0, 0, 3);",
            NYU_VIOLET_LIGHT
        );
    }
    
    public static String secondaryButton() {
        return String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: %s; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 5; " +
            "-fx-border-color: %s; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 5;",
            BG_WHITE, NYU_VIOLET, NYU_VIOLET
        );
    }
    
    public static String successButton() {
        return String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 5;",
            SUCCESS_GREEN
        );
    }
    
    public static String dangerButton() {
        return String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 5;",
            ERROR_RED
        );
    }
    
    // Title Styles
    public static String pageTitle() {
        return String.format(
            "-fx-font-size: 28px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: %s;",
            NYU_VIOLET_DARK
        );
    }
    
    public static String sectionTitle() {
        return String.format(
            "-fx-font-size: 20px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: %s;",
            NYU_VIOLET
        );
    }
    
    // Card/Container Styles
    public static String cardStyle() {
        return String.format(
            "-fx-background-color: %s; " +
            "-fx-background-radius: 10; " +
            "-fx-border-color: %s; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 10; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2); " +
            "-fx-padding: 20;",
            BG_WHITE, BORDER_LIGHT
        );
    }
    
    public static String sectionCard() {
        return String.format(
            "-fx-background-color: %s; " +
            "-fx-background-radius: 8; " +
            "-fx-border-color: %s; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 8; " +
            "-fx-padding: 15;",
            BG_WHITE, NYU_VIOLET_PALE
        );
    }
    
    // Status Labels
    public static String statusSuccess() {
        return String.format("-fx-text-fill: %s; -fx-font-weight: bold;", SUCCESS_GREEN);
    }
    
    public static String statusError() {
        return String.format("-fx-text-fill: %s; -fx-font-weight: bold;", ERROR_RED);
    }
    
    public static String statusWarning() {
        return String.format("-fx-text-fill: %s; -fx-font-weight: bold;", WARNING_ORANGE);
    }
    
    public static String statusInfo() {
        return String.format("-fx-text-fill: %s; -fx-font-weight: bold;", INFO_BLUE);
    }
    
    // Input Field Styles
    public static String inputField() {
        return String.format(
            "-fx-background-color: %s; " +
            "-fx-border-color: %s; " +
            "-fx-border-width: 1.5; " +
            "-fx-border-radius: 5; " +
            "-fx-background-radius: 5; " +
            "-fx-padding: 8 12; " +
            "-fx-font-size: 14px;",
            BG_WHITE, BORDER_LIGHT
        );
    }
    
    public static String inputFieldFocused() {
        return String.format(
            "-fx-background-color: %s; " +
            "-fx-border-color: %s; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 5; " +
            "-fx-background-radius: 5; " +
            "-fx-padding: 8 12; " +
            "-fx-font-size: 14px;",
            BG_WHITE, NYU_VIOLET
        );
    }
}

