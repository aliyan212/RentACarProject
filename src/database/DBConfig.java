package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DBConfig {
    private static final String DB_FILE_NAME = "rent-a-car.db";
    private static String dbPath;
    private static final String URL = "jdbc:sqlite:" + getDbPath();
    private static volatile boolean initialized;

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);
        try (Statement pragma = conn.createStatement()) {
            pragma.execute("PRAGMA foreign_keys = ON");
        }
        ensureSchema(conn);
        return conn;
    }

    private static void ensureSchema(Connection conn) throws SQLException {
        if (initialized) {
            return;
        }

        synchronized (DBConfig.class) {
            if (initialized) {
                return;
            }

            try (Statement st = conn.createStatement()) {
                st.execute("""
                        CREATE TABLE IF NOT EXISTS Customers (
                            customer_cnic INTEGER PRIMARY KEY,
                            customer_name TEXT NOT NULL,
                            address TEXT,
                            phone_number INTEGER,
                            license_number TEXT
                        )
                        """);

                st.execute("""
                        CREATE TABLE IF NOT EXISTS Drivers (
                            driver_cnic INTEGER PRIMARY KEY,
                            driver_name TEXT NOT NULL,
                            license_number INTEGER,
                            phone_number INTEGER,
                            status TEXT
                        )
                        """);

                st.execute("""
                        CREATE TABLE IF NOT EXISTS Vehicles (
                            car_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                            model TEXT NOT NULL,
                            purchase_Price TEXT,
                            ownership_Percentage REAL
                        )
                        """);

                st.execute("""
                        CREATE TABLE IF NOT EXISTS Sales (
                            sale_id INTEGER PRIMARY KEY AUTOINCREMENT,
                            customer_cnic INTEGER NOT NULL,
                            car_id INTEGER NOT NULL,
                            driver_cnic INTEGER,
                            start_date TEXT,
                            end_date TEXT,
                            rental_type TEXT,
                            Total INTEGER NOT NULL,
                            FOREIGN KEY(customer_cnic) REFERENCES Customers(customer_cnic) ON DELETE RESTRICT,
                            FOREIGN KEY(car_id) REFERENCES Vehicles(car_ID) ON DELETE RESTRICT,
                            FOREIGN KEY(driver_cnic) REFERENCES Drivers(driver_cnic) ON DELETE SET NULL
                        )
                        """);

                st.execute("""
                        CREATE TABLE IF NOT EXISTS Payment (
                            payment_id INTEGER PRIMARY KEY AUTOINCREMENT,
                            sale_id INTEGER NOT NULL,
                            money_paid REAL NOT NULL,
                            payment_date TEXT,
                            FOREIGN KEY(sale_id) REFERENCES Sales(sale_id) ON DELETE CASCADE
                        )
                        """);

                st.execute("""
                        CREATE TABLE IF NOT EXISTS Expenses (
                            expense_id INTEGER PRIMARY KEY AUTOINCREMENT,
                            type TEXT,
                            payer TEXT,
                            sale_id INTEGER,
                            vehicle_id INTEGER,
                            amount REAL NOT NULL,
                            date TEXT,
                            FOREIGN KEY(sale_id) REFERENCES Sales(sale_id) ON DELETE SET NULL,
                            FOREIGN KEY(vehicle_id) REFERENCES Vehicles(car_ID) ON DELETE SET NULL
                        )
                        """);

                st.execute("CREATE INDEX IF NOT EXISTS idx_sales_car_id ON Sales(car_id)");
                st.execute("CREATE INDEX IF NOT EXISTS idx_sales_driver_cnic ON Sales(driver_cnic)");
                st.execute("CREATE INDEX IF NOT EXISTS idx_payment_sale_id ON Payment(sale_id)");
                st.execute("CREATE INDEX IF NOT EXISTS idx_expenses_sale_id ON Expenses(sale_id)");
                st.execute("CREATE INDEX IF NOT EXISTS idx_expenses_vehicle_id ON Expenses(vehicle_id)");
            }

            initialized = true;
        }
    }

    private static String resolveDbPath() {
        // Check for Gluon Mobile / Android Attach Storage Service
        try {
            Class<?> storageServiceClass = Class.forName("com.gluonhq.attach.storage.StorageService");
            Object serviceOpt = storageServiceClass.getMethod("create").invoke(null);
            if (serviceOpt instanceof java.util.Optional<?> opt && opt.isPresent()) {
                Object service = opt.get();
                Object storageOpt = service.getClass().getMethod("getPrivateStorage").invoke(service);
                if (storageOpt instanceof java.util.Optional<?> optDir && optDir.isPresent()) {
                    java.io.File dir = (java.io.File) optDir.get();
                    return new java.io.File(dir, DB_FILE_NAME).getAbsolutePath();
                }
            }
        } catch (Throwable ignored) {
            // Not running in Gluon Attach runtime with StorageService, continue with
            // standard path resolution
        }

        // Allow explicit override via environment variable or system property
        String customPath = System.getenv("RENTACAR_DB_PATH");
        if (customPath != null && !customPath.isBlank()) {
            return customPath;
        }

        String customDir = System.getenv("RENTACAR_DB_DIR");
        if (customDir == null || customDir.isBlank()) {
            customDir = System.getProperty("app.data.dir");
        }

        Path appDataDir;
        if (customDir != null && !customDir.isBlank()) {
            appDataDir = Paths.get(customDir);
        } else {
            String userHome = System.getProperty("user.home");
            if (userHome != null && !userHome.isBlank() && !userHome.equals("/")) {
                appDataDir = Paths.get(userHome, ".rent-a-car");
            } else {
                // Fallback for mobile/sandboxed environments where user.home might be root or
                // null
                String userDir = System.getProperty("user.dir", ".");
                appDataDir = Paths.get(userDir, ".rent-a-car");
            }
        }

        try {
            Files.createDirectories(appDataDir);
        } catch (Exception e) {
            // If creating directory fails, fallback to current directory
            appDataDir = Paths.get(".");
        }
        return appDataDir.resolve(DB_FILE_NAME).toAbsolutePath().normalize().toString();
    }

    public static String getDbPath() {
        if (dbPath == null) {
            dbPath = resolveDbPath();
        }
        return dbPath;
    }

    public static void backupDatabase(java.io.File targetFile) throws java.io.IOException {
        Path src = Paths.get(getDbPath());
        if (!Files.exists(src)) {
            throw new java.io.FileNotFoundException("Database file does not exist at " + src);
        }
        if (targetFile.getParentFile() != null) {
            targetFile.getParentFile().mkdirs();
        }
        Files.copy(src, targetFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    public static void restoreDatabase(java.io.File sourceFile) throws java.io.IOException, java.sql.SQLException {
        if (!sourceFile.exists()) {
            throw new java.io.FileNotFoundException("Selected backup file does not exist");
        }
        Path dest = Paths.get(getDbPath());
        if (dest.getParent() != null) {
            Files.createDirectories(dest.getParent());
        }
        Files.copy(sourceFile.toPath(), dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        // Verify database connection and schema
        initialized = false;
        try (Connection conn = getConnection()) {
            ensureSchema(conn);
        }
    }
}
