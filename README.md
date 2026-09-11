# Rent-A-Car Management System

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://openjdk.org/)
[![JavaFX](https://img.shields.io/badge/JavaFX-17-orange.svg)](https://openjfx.io/)
[![Platform](https://img.shields.io/badge/Platform-macOS%20%7C%20Windows%20%7C%20Linux%20%7C%20Android-green.svg)](#)

A Free and Open Source (FOSS), cross-platform, responsive fleet and rental management system built with JavaFX, embedded SQLite, and Gluon Mobile.

---

## Key Features

- **Adaptive Responsive UI**: Automatically transitions between desktop sidebar navigation (≥ 768px) and a mobile top bar with a slide-out drawer (< 768px).
- **Zero-Setup Embedded Database**: Automatically initializes local SQLite database schema on first launch (`~/.rent-a-car/rent-a-car.db` on Desktop, sandboxed private directory on Android).
- **Fleet, Customer & Driver Management**: Complete CRUD operations for vehicles, customers, and drivers.
- **Rental Billing & Real-time Balance Tracking**: Searchable customer/car dropdowns, payment tracking, overdue notices, and profit/loss calculations.
- **Cross-Platform & FOSS Ready**: Package as a universal standalone executable JAR or native Android APK.

---

## Quick Start & Running

### 1. Run Development Build (Desktop)
```bash
mvn clean javafx:run
```

### 2. Run Standalone Executable JAR (Any OS)
You can run the pre-built standalone JAR directly on macOS, Windows, or Linux with Java 17+ installed:
```bash
java -jar dist/rent-a-car.jar
```

---

## Multi-Platform Packaging & Release

### Automated Local Build
Build the universal standalone JAR and release archives in one step:
```bash
bash build-release.sh
```
This produces:
- `dist/rent-a-car-1.0.0-standalone.jar` (Self-contained runnable fat JAR)
- `dist/rent-a-car-1.0.0-universal.zip` (Distribution archive)
- `dist/rent-a-car-1.0.0-universal.tar.gz`

---

## Android Build & Deployment

The application is configured with the modern **GluonFX Maven Plugin** (`gluonfx-maven-plugin`) and Gluon Attach services for native Ahead-Of-Time (AOT) mobile compilation.

### Prerequisites:
1. **GraalVM** with Native Image enabled (Java 17 or 21).
2. **Android SDK** (API Level 21+) and **Android NDK** (version 25b+).
3. Set environment variables:
   ```bash
   export ANDROID_SDK_ROOT=/path/to/android-sdk
   export ANDROID_NDK_HOME=/path/to/android-ndk
   export GRAALVM_HOME=/path/to/graalvm
   ```

### 1. Compile & Package Android APK:
> **Note**: Use the bundled `./mvnw` wrapper, which ensures the required Maven 3.8.8 runtime expected by GluonFX.
```bash
./mvnw clean gluonfx:build -Pandroid
./mvnw gluonfx:package -Pandroid
```
The generated `.apk` will be output in `target/gluonfx/aarch64-android/gvm/Rent-A-Car.apk`.

### 2. Install & Run on Connected Android Device / Emulator:
```bash
# Connect device via USB with USB Debugging enabled (verify with `adb devices`), then run:
./mvnw gluonfx:run -Pandroid
```
Or manually install the APK via `adb`:
```bash
adb install target/gluonfx/aarch64-android/gvm/Rent-A-Car.apk
```

---

## Storage & Database Details

- **Desktop Storage**: `~/.rent-a-car/rent-a-car.db`
- **Android Storage**: Sandboxed private storage (`/data/data/com.alieon.rentacar/files/rent-a-car.db`) resolved automatically via Gluon `StorageService`.
- **Custom Storage Path Override**: Set `RENTACAR_DB_PATH` or `RENTACAR_DB_DIR` environment variables.

---

## Contributing & FOSS License

Contributions are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for contribution guidelines.

This project is licensed under the [MIT License](LICENSE).


