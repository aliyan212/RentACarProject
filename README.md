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

## Contributing & FOSS License

Contributions are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for contribution guidelines.

This project is licensed under the [MIT License](LICENSE).


