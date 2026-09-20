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
        scroll.setFitToHeight(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setPannable(false);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent; -fx-border-width: 0; -fx-padding: 0;");
        makeSmooth(scroll);
        return scroll;
    }

    public static void makeSmooth(ScrollPane scroll) {
        scroll.setPannable(false);
        scroll.addEventFilter(javafx.scene.input.ScrollEvent.SCROLL, event -> {
            double deltaY = event.getDeltaY();
            if (deltaY != 0) {
                Node content = scroll.getContent();
                if (content != null) {
                    javafx.geometry.Bounds viewport = scroll.getViewportBounds();
                    double contentHeight = content.getBoundsInLocal().getHeight();
                    double viewportHeight = viewport != null ? viewport.getHeight() : scroll.getHeight();
                    double scrollable = contentHeight - viewportHeight;
                    if (scrollable > 0) {
                        double multiplier = event.isDirect() ? 1.0 : 2.5;
                        double step = -(deltaY * multiplier) / scrollable;
                        double newV = Math.max(0.0, Math.min(1.0, scroll.getVvalue() + step));
                        scroll.setVvalue(newV);
                        event.consume();
                    }
                }
            }

            double deltaX = event.getDeltaX();
            if (deltaX != 0) {
                Node content = scroll.getContent();
                if (content != null) {
                    javafx.geometry.Bounds viewport = scroll.getViewportBounds();
                    double contentWidth = content.getBoundsInLocal().getWidth();
                    double viewportWidth = viewport != null ? viewport.getWidth() : scroll.getWidth();
                    double scrollableX = contentWidth - viewportWidth;
                    if (scrollableX > 0) {
                        double multiplierX = event.isDirect() ? 1.0 : 2.5;
                        double stepX = -(deltaX * multiplierX) / scrollableX;
                        double newH = Math.max(0.0, Math.min(1.0, scroll.getHvalue() + stepX));
                        scroll.setHvalue(newH);
                        event.consume();
                    }
                }
            }
        });
    }

    public static void animateDialogEntrance(Node node) {
        node.setOpacity(0.0);
        node.setScaleX(0.96);
        node.setScaleY(0.96);

        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(160), node);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.setInterpolator(javafx.animation.Interpolator.EASE_OUT);

        javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(160), node);
        st.setFromX(0.96);
        st.setFromY(0.96);
        st.setToX(1.0);
        st.setToY(1.0);
        st.setInterpolator(javafx.animation.Interpolator.EASE_OUT);

        new javafx.animation.ParallelTransition(ft, st).play();
    }

    public static void addHoverScale(Node node, double scale) {
        node.setOnMouseEntered(e -> {
            javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(130), node);
            st.setToX(scale);
            st.setToY(scale);
            st.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
            st.play();
        });
        node.setOnMouseExited(e -> {
            javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(130), node);
            st.setToX(1.0);
            st.setToY(1.0);
            st.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
            st.play();
        });
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
                animateDialogEntrance(pane);
            }
        });
        if (pane.getScene() != null) {
            pane.getScene().setFill(javafx.scene.paint.Color.web("#191924"));
            animateDialogEntrance(pane);
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
