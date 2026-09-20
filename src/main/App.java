package main;

import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import views.*;

import java.io.InputStream;
import java.util.function.Supplier;

public class App extends Application {
    private static final double MOBILE_BREAKPOINT = 768.0;

    private StackPane rootPane;
    private BorderPane mainLayout;
    private StackPane contentArea;

    private VBox desktopSidebar;
    private HBox mobileTopBar;
    private Label mobileTitleLabel;
    private Label mobileScreenBadge;

    private Region drawerBackdrop;
    private VBox mobileDrawer;
    private boolean isDrawerOpen = false;
    private boolean isMobileMode = false;

    private ToggleGroup desktopNavGroup;
    private ToggleGroup drawerNavGroup;

    private Stage primaryStage;
    private String currentScreenName = "Dashboard";

    private Image loadAppIcon(int size) {
        try {
            String path = size <= 32 ? "/resources/icon-32.png" : (size <= 64 ? "/resources/icon-64.png" : "/resources/icon.png");
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                is = getClass().getResourceAsStream(path.replace("/resources", ""));
            }
            if (is != null) {
                return new Image(is);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private Image loadBrandLogo(int size) {
        try {
            String path = "/resources/icon-mark-" + (size <= 32 ? "32" : (size <= 64 ? "64" : "128")) + ".png";
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                is = getClass().getResourceAsStream(path.replace("/resources", ""));
            }
            if (is != null) {
                return new Image(is);
            }
        } catch (Exception ignored) {
        }
        return loadAppIcon(size);
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        rootPane = new StackPane();
        mainLayout = new BorderPane();
        contentArea = new StackPane();
        mainLayout.setCenter(contentArea);

        desktopNavGroup = new ToggleGroup();
        drawerNavGroup = new ToggleGroup();

        // ── 1. Desktop Sidebar ────────────────────────────────────────────────
        desktopSidebar = createSidebar(desktopNavGroup, false);

        // ── 2. Mobile Top Bar ─────────────────────────────────────────────────
        mobileTopBar = createMobileTopBar();

        // ── 3. Mobile Navigation Drawer & Backdrop ────────────────────────────
        createMobileDrawer();

        rootPane.getChildren().addAll(mainLayout, drawerBackdrop, mobileDrawer);
        StackPane.setAlignment(mobileDrawer, Pos.CENTER_LEFT);

        Scene scene = new Scene(rootPane, 1100, 720);

        var css = getClass().getResource("/resources/style.css");
        if (css == null)
            css = getClass().getResource("/style.css");
        if (css != null)
            scene.getStylesheets().add(css.toExternalForm());

        // ── 4. Responsive Breakpoint Listener ─────────────────────────────────
        scene.widthProperty().addListener((obs, oldW, newW) -> {
            applyResponsiveLayout(newW.doubleValue());
        });

        // ── 5. Start on Dashboard ─────────────────────────────────────────────
        navigateTo("Dashboard", DashboardView::getView);

        stage.setTitle("Roam | Fleet & Rental Management");
        try {
            Image appIcon512 = loadAppIcon(512);
            if (appIcon512 != null) stage.getIcons().add(appIcon512);
            Image appIcon32 = loadAppIcon(32);
            if (appIcon32 != null) stage.getIcons().add(appIcon32);
        } catch (Exception ignored) {
        }

        stage.setMinWidth(360);
        stage.setMinHeight(520);
        stage.setScene(scene);
        stage.show();

        // Apply initial layout based on starting width
        applyResponsiveLayout(scene.getWidth());
    }

    private void applyResponsiveLayout(double width) {
        boolean mobile = width < MOBILE_BREAKPOINT;
        if (mobile == isMobileMode && mainLayout.getLeft() != null == !mobile) {
            return;
        }
        isMobileMode = mobile;

        if (isMobileMode) {
            mainLayout.setLeft(null);
            mainLayout.setTop(mobileTopBar);
        } else {
            closeDrawer(false);
            mainLayout.setTop(null);
            mainLayout.setLeft(desktopSidebar);
        }
    }

    private HBox createMobileTopBar() {
        HBox topBar = new HBox(10);
        topBar.getStyleClass().add("mobile-top-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);

        Button hamburgerBtn = new Button("☰");
        hamburgerBtn.getStyleClass().add("hamburger-btn");
        hamburgerBtn.setOnAction(e -> toggleDrawer());

        Image logoImg = loadBrandLogo(64);
        ImageView logoView = null;
        if (logoImg != null) {
            logoView = new ImageView(logoImg);
            logoView.setFitWidth(24);
            logoView.setFitHeight(24);
            logoView.setPreserveRatio(true);
            logoView.getStyleClass().add("brand-logo-img");
        }

        mobileTitleLabel = new Label("Roam");
        mobileTitleLabel.getStyleClass().add("mobile-title");

        mobileScreenBadge = new Label("Dashboard");
        mobileScreenBadge.getStyleClass().add("mobile-screen-badge");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        if (logoView != null) {
            topBar.getChildren().addAll(hamburgerBtn, logoView, mobileTitleLabel, mobileScreenBadge, spacer);
        } else {
            topBar.getChildren().addAll(hamburgerBtn, mobileTitleLabel, mobileScreenBadge, spacer);
        }
        return topBar;
    }

    private void createMobileDrawer() {
        drawerBackdrop = new Region();
        drawerBackdrop.getStyleClass().add("drawer-backdrop");
        drawerBackdrop.setVisible(false);
        drawerBackdrop.setOpacity(0.0);
        drawerBackdrop.setPickOnBounds(true);
        drawerBackdrop.prefWidthProperty().bind(rootPane.widthProperty());
        drawerBackdrop.prefHeightProperty().bind(rootPane.heightProperty());
        drawerBackdrop.setOnMousePressed(e -> {
            closeDrawer(true);
            e.consume();
        });
        drawerBackdrop.setOnMouseClicked(e -> {
            closeDrawer(true);
            e.consume();
        });

        mobileDrawer = createSidebar(drawerNavGroup, true);
        mobileDrawer.getStyleClass().add("nav-drawer");
        mobileDrawer.setTranslateX(-300);
        mobileDrawer.setVisible(false);
        mobileDrawer.setOnMousePressed(javafx.event.Event::consume);
        mobileDrawer.setOnMouseClicked(javafx.event.Event::consume);
    }

    private VBox createSidebar(ToggleGroup navGroup, boolean isDrawer) {
        VBox sidebar = new VBox(6);
        sidebar.getStyleClass().add(isDrawer ? "nav-drawer" : "sidebar");

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getStyleClass().add("sidebar-header-box");

        Image logoImg = loadBrandLogo(64);
        ImageView logoView = null;
        if (logoImg != null) {
            logoView = new ImageView(logoImg);
            logoView.setFitWidth(28);
            logoView.setFitHeight(28);
            logoView.setPreserveRatio(true);
            logoView.getStyleClass().add("sidebar-brand-logo");
        }

        VBox titleBox = new VBox(1);
        Label appTitle = new Label("Roam");
        appTitle.getStyleClass().add("sidebar-title");
        Label subtitle = new Label("FLEET & RENTALS");
        subtitle.getStyleClass().add("sidebar-subtitle");
        titleBox.getChildren().addAll(appTitle, subtitle);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        if (logoView != null) {
            headerBox.getChildren().add(logoView);
        }
        headerBox.getChildren().add(titleBox);

        if (isDrawer) {
            Button closeBtn = new Button("✕");
            closeBtn.getStyleClass().add("drawer-close-btn");
            closeBtn.setOnAction(e -> closeDrawer(true));
            headerBox.getChildren().add(closeBtn);
        }

        ToggleButton btnDash = navBtn("Dashboard", navGroup);
        ToggleButton btnVehicles = navBtn("Fleet", navGroup);
        ToggleButton btnCustomers = navBtn("Customers", navGroup);
        ToggleButton btnDrivers = navBtn("Drivers", navGroup);
        ToggleButton btnSales = navBtn("Sales", navGroup);
        ToggleButton btnPayments = navBtn("Payments", navGroup);
        ToggleButton btnExpenses = navBtn("Expenses", navGroup);

        btnDash.setOnAction(e -> navigateTo("Dashboard", DashboardView::getView));
        btnVehicles.setOnAction(e -> navigateTo("Fleet", VehicleView::getView));
        btnCustomers.setOnAction(e -> navigateTo("Customers", CustomerView::getView));
        btnDrivers.setOnAction(e -> navigateTo("Drivers", DriverView::getView));
        btnSales.setOnAction(e -> navigateTo("Sales", SalesView::getView));
        btnPayments.setOnAction(e -> navigateTo("Payments", PaymentView::getView));
        btnExpenses.setOnAction(e -> navigateTo("Expenses", ExpenseView::getView));

        Button dbBtn = new Button("⚙ DB Tools");
        dbBtn.getStyleClass().add("nav-button");
        dbBtn.setMaxWidth(Double.MAX_VALUE);
        dbBtn.setOnAction(e -> showDatabaseToolsDialog(primaryStage));

        Region navSpacer = new Region();
        VBox.setVgrow(navSpacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(
                headerBox,
                new Separator(),
                btnDash,
                new Separator(),
                btnVehicles, btnCustomers, btnDrivers,
                new Separator(),
                btnSales, btnPayments, btnExpenses,
                navSpacer,
                new Separator(),
                dbBtn);

        return sidebar;
    }

    private void showDatabaseToolsDialog(Stage owner) {
        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Database Backup & Restore");
        dlg.setHeaderText(null);
        ViewHelper.styleDialog(dlg.getDialogPane());

        VBox box = new VBox(14);
        box.setPadding(new Insets(12));

        Label info = new Label("Current Database Location:\n" + database.DBConfig.getDbPath());
        info.getStyleClass().add("muted");
        info.setWrapText(true);

        Button backupBtn = new Button("💾 Backup / Export Database");
        backupBtn.getStyleClass().add("btn-primary");
        backupBtn.setMaxWidth(Double.MAX_VALUE);

        Button restoreBtn = new Button("📥 Restore Database from File");
        restoreBtn.getStyleClass().add("btn-secondary");
        restoreBtn.setMaxWidth(Double.MAX_VALUE);

        backupBtn.setOnAction(e -> {
            javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
            fc.setTitle("Save Database Backup");
            fc.setInitialFileName("rent-a-car-backup.db");
            fc.getExtensionFilters()
                    .add(new javafx.stage.FileChooser.ExtensionFilter("SQLite Database (*.db)", "*.db"));
            java.io.File dest = fc.showSaveDialog(owner);
            if (dest != null) {
                try {
                    database.DBConfig.backupDatabase(dest);
                    ViewHelper.showInfo("Database successfully backed up to:\n" + dest.getAbsolutePath());
                } catch (Exception ex) {
                    ViewHelper.showError("Backup failed", ex);
                }
            }
        });

        restoreBtn.setOnAction(e -> {
            javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
            fc.setTitle("Select Database Backup to Restore");
            fc.getExtensionFilters()
                    .add(new javafx.stage.FileChooser.ExtensionFilter("SQLite Database (*.db)", "*.db"));
            java.io.File src = fc.showOpenDialog(owner);
            if (src != null) {
                try {
                    database.DBConfig.restoreDatabase(src);
                    ViewHelper.showInfo("Database successfully restored! Reloading current screen...");
                    navigateTo(currentScreenName, () -> switch (currentScreenName) {
                        case "Fleet" -> VehicleView.getView();
                        case "Customers" -> CustomerView.getView();
                        case "Drivers" -> DriverView.getView();
                        case "Sales" -> SalesView.getView();
                        case "Payments" -> PaymentView.getView();
                        case "Expenses" -> ExpenseView.getView();
                        default -> DashboardView.getView();
                    });
                    dlg.close();
                } catch (Exception ex) {
                    ViewHelper.showError("Restore failed", ex);
                }
            }
        });

        box.getChildren().addAll(info, new Separator(), backupBtn, restoreBtn);
        dlg.getDialogPane().setContent(box);
        dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dlg.showAndWait();
    }

    private void navigateTo(String screenName, Supplier<Node> viewSupplier) {
        currentScreenName = screenName;
        if (mobileScreenBadge != null) {
            mobileScreenBadge.setText(screenName);
        }

        Node view = viewSupplier.get();
        view.setOpacity(0.0);
        view.setTranslateY(10);
        contentArea.getChildren().setAll(view);

        FadeTransition ft = new FadeTransition(Duration.millis(200), view);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.setInterpolator(Interpolator.EASE_OUT);

        TranslateTransition tt = new TranslateTransition(Duration.millis(200), view);
        tt.setFromY(10);
        tt.setToY(0);
        tt.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(ft, tt).play();

        // Sync toggle button selections
        selectNavButton(desktopNavGroup, screenName);
        selectNavButton(drawerNavGroup, screenName);

        if (isDrawerOpen) {
            closeDrawer(true);
        }
    }

    private void selectNavButton(ToggleGroup group, String name) {
        for (var toggle : group.getToggles()) {
            if (toggle instanceof ToggleButton btn && btn.getText().equalsIgnoreCase(name)) {
                btn.setSelected(true);
                break;
            }
        }
    }

    private void toggleDrawer() {
        if (isDrawerOpen) {
            closeDrawer(true);
        } else {
            openDrawer();
        }
    }

    private void openDrawer() {
        if (isDrawerOpen)
            return;
        isDrawerOpen = true;
        drawerBackdrop.setVisible(true);
        drawerBackdrop.setOpacity(1.0);
        mobileDrawer.setVisible(true);

        TranslateTransition tt = new TranslateTransition(Duration.millis(120), mobileDrawer);
        tt.setFromX(-300);
        tt.setToX(0);
        tt.play();
    }

    private void closeDrawer(boolean animated) {
        if (!isDrawerOpen && !mobileDrawer.isVisible()) {
            return;
        }
        isDrawerOpen = false;
        drawerBackdrop.setVisible(false);
        drawerBackdrop.setOpacity(0.0);

        if (animated) {
            TranslateTransition tt = new TranslateTransition(Duration.millis(100), mobileDrawer);
            tt.setFromX(mobileDrawer.getTranslateX());
            tt.setToX(-300);
            tt.setOnFinished(e -> mobileDrawer.setVisible(false));
            tt.play();
        } else {
            mobileDrawer.setTranslateX(-300);
            mobileDrawer.setVisible(false);
        }
    }

    private ToggleButton navBtn(String text, ToggleGroup group) {
        ToggleButton btn = new ToggleButton(text);
        btn.getStyleClass().add("nav-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setToggleGroup(group);
        return btn;
    }

    public static void main(String[] args) {
        launch(args);
    }
}