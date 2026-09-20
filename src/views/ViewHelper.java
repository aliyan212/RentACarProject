package views;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.sql.SQLException;

public class ViewHelper {

    public static TextField field(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.getStyleClass().add("form-field");
        tf.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(tf, Priority.ALWAYS);
        return tf;
    }

    public static Node form(Object... pairs) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(12, 16, 12, 16));
        grid.setMaxWidth(Double.MAX_VALUE);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(90);
        col1.setPrefWidth(110);
        col1.setHalignment(HPos.LEFT);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        col2.setFillWidth(true);

        grid.getColumnConstraints().addAll(col1, col2);

        for (int i = 0; i < pairs.length; i += 2) {
            Label lbl = new Label(pairs[i] + ":");
            lbl.getStyleClass().add("form-label");
            Node input = (Node) pairs[i + 1];
            if (input instanceof Control c) {
                c.setMaxWidth(Double.MAX_VALUE);
            }
            grid.add(lbl, 0, i / 2);
            grid.add(input, 1, i / 2);
        }

        // Wrap inside a touch-pannable ScrollPane so inputs on mobile virtual keyboards
        // are never cut off
        ScrollPane scroll = createResponsiveScroll(grid);
        scroll.setMaxHeight(420);
        scroll.setPrefWidth(400);
        return scroll;
    }

    public static ScrollPane createResponsiveScroll(Node content) {
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setPannable(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent; -fx-border-width: 0; -fx-padding: 0;");
        return scroll;
    }

    public static VBox createHeader(String titleText, String subtitleText) {
        Label title = new Label(titleText);
        title.getStyleClass().add("content-title");

        VBox header = new VBox(4, title);
        if (subtitleText != null && !subtitleText.isBlank()) {
            Label sub = new Label(subtitleText);
            sub.getStyleClass().add("muted");
            header.getChildren().add(sub);
        }
        return header;
    }

    public static Node wrapInResponsivePage(String title, String subtitle, Node toolbar, Node mainCard) {
        VBox content = new VBox(16);
        content.getStyleClass().add("content");
        content.setPadding(new Insets(18));

        VBox header = createHeader(title, subtitle);
        content.getChildren().add(header);

        if (toolbar != null) {
            content.getChildren().add(toolbar);
        }
        if (mainCard != null) {
            VBox.setVgrow(mainCard, Priority.ALWAYS);
            content.getChildren().add(mainCard);
        }

        return createResponsiveScroll(content);
    }

    public static void styleDialog(DialogPane pane) {
        var css = ViewHelper.class.getResource("/resources/style.css");
        if (css == null)
            css = ViewHelper.class.getResource("/style.css");
        if (css != null && !pane.getStylesheets().contains(css.toExternalForm()))
            pane.getStylesheets().add(css.toExternalForm());
        pane.getStyleClass().add("dialog-pane");
        pane.setMinWidth(320);
        pane.setPrefWidth(440);
        pane.setMaxWidth(560);

        pane.sceneProperty().addListener((obs, oldS, newS) -> {
            if (newS != null) {
                newS.setFill(javafx.scene.paint.Color.web("#191924"));
                if (newS.getWindow() instanceof javafx.stage.Stage stage) {
                    try {
                        var is = ViewHelper.class.getResourceAsStream("/resources/icon-32.png");
                        if (is != null) stage.getIcons().add(new javafx.scene.image.Image(is));
                    } catch (Exception ignored) {}
                }
            }
        });
        if (pane.getScene() != null) {
            pane.getScene().setFill(javafx.scene.paint.Color.web("#191924"));
        }
    }

    public static void showError(String msg, SQLException e) {
        showError(msg, (Exception) e);
    }

    public static void showError(String msg, Exception e) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText(msg);
        a.setContentText(e != null ? e.getMessage() : "An unexpected error occurred.");
        styleDialog(a.getDialogPane());
        a.show();
    }

    public static void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Information");
        a.setHeaderText(null);
        a.setContentText(msg);
        styleDialog(a.getDialogPane());
        a.show();
    }

    public static void showWarning(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Warning");
        a.setHeaderText(null);
        a.setContentText(msg);
        styleDialog(a.getDialogPane());
        a.show();
    }

    public static boolean confirmDelete(String item) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Confirm Delete");
        a.setHeaderText("Delete " + item + "?");
        a.setContentText("This action cannot be undone.");
        styleDialog(a.getDialogPane());
        return a.showAndWait().filter(b -> b == ButtonType.OK).isPresent();
    }
}
