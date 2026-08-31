package main;

import javafx.scene.control.TextField;

public class InputValidator {
    public static boolean isNotEmpty(TextField field, String fieldName) {
        String text = field.getText();
        if (text == null || text.trim().isEmpty()) {
            field.setStyle("-fx-border-color: #ff5252; -fx-border-width: 2;");
            field.setPromptText("Required: " + fieldName);
            return false;
        }
        field.setStyle("");
        return true;
    }

    public static boolean isNumeric(TextField field, String fieldName) {
        String text = field.getText();
        try {
            Long.parseLong(text.trim());
            field.setStyle("");
            return true;
        } catch (NumberFormatException e) {
            field.setStyle("-fx-border-color: #ff5252; -fx-border-width: 2;");
            field.setPromptText("Numeric: " + fieldName);
            return false;
        }
    }

    public static boolean isPositiveNumber(TextField field, String fieldName) {
        String text = field.getText();
        try {
            double val = Double.parseDouble(text.trim());
            if (val < 0) {
                field.setStyle("-fx-border-color: #ff5252; -fx-border-width: 2;");
                field.setPromptText("Positive: " + fieldName);
                return false;
            }
            field.setStyle("");
            return true;
        } catch (NumberFormatException e) {
            field.setStyle("-fx-border-color: #ff5252; -fx-border-width: 2;");
            field.setPromptText("Number: " + fieldName);
            return false;
        }
    }
}