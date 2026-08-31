# Contributing to Rent-A-Car Management System

Thank you for your interest in contributing to the **Rent-A-Car Management System**! This is a Free and Open Source Software (FOSS) project, and we welcome contributions from everyone.

---

## Code of Conduct

Please be respectful, collaborative, and constructive when interacting with fellow contributors and maintainers.

---

## Getting Started

### 1. Fork & Clone
1. Fork the repository on GitHub.
2. Clone your fork locally:
   ```bash
   git clone https://github.com/your-username/RentACarProject.git
   cd RentACarProject
   ```

### 2. Development Prerequisites
- **JDK 17 or JDK 21**
- **Apache Maven 3.8+**
- (Optional for Android builds) **Android SDK / NDK** and **GraalVM**

### 3. Build & Run
Run the application on desktop:
```bash
mvn clean compile javafx:run
```

Package the standalone executable:
```bash
mvn clean package
```

---

## Contribution Workflow

1. **Create a Feature Branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```
2. **Make Clean, Modular Changes**:
   - Follow standard Java naming conventions and clean code architecture.
   - Ensure responsive UI considerations are preserved for mobile screens.
3. **Verify Build**:
   ```bash
   mvn clean compile
   ```
4. **Submit a Pull Request**:
   - Push your branch to GitHub and open a Pull Request with a clear description of your changes.

---

## License

By contributing to Rent-A-Car, you agree that your contributions will be licensed under the project's [MIT License](LICENSE).
