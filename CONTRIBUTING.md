# Contributing to Mene Monitor ⚡

Thank you for your interest in contributing to **Mene Monitor**! We welcome bug reports, feature suggestions, documentation updates, and code contributions from the community.

---

## 🚀 How to Get Started

1. **Fork & Clone the Repository:**
   ```bash
   git clone https://github.com/mene-monitor/mene-monitor.git
   cd mene-monitor
   ```

2. **Open in Android Studio:**
   - Use **Android Studio Ladybug (2024.2.1)** or newer.
   - Required JDK version: **JDK 17**.

3. **Verify Local Build & Tests:**
   ```bash
   ./gradlew testDebugUnitTest
   ./gradlew assembleDebug
   ```

---

## 🛠️ Code Conventions & Architecture

- **Architecture:** Follow Clean Architecture principles (`data` ➔ `domain` ➔ `presentation`).
- **UI:** Built exclusively with **Jetpack Compose** and Material 3 design components.
- **State Management:** Unidirectional Data Flow using Kotlin `Coroutines` and `StateFlow`.
- **Formatting:** Keep Kotlin code clean and maintain existing naming conventions.

---

## 📥 Submitting Pull Requests

1. Create a feature branch for your work:
   ```bash
   git checkout -b feature/my-new-feature
   ```
2. Commit your changes with clear, descriptive commit messages.
3. Ensure all unit tests pass before opening a Pull Request:
   ```bash
   ./gradlew testDebugUnitTest
   ```
4. Push your branch and open a Pull Request against `master`.

---

## 📄 License & Intellectual Property

By contributing to Mene Monitor, you agree that your contributions will be licensed under the project's [Apache 2.0 License](LICENSE).
