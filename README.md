# Mene Monitor ⚡

[![Android CI](https://github.com/ajeth23/mene-monitor/actions/workflows/ci.yml/badge.svg)](https://github.com/ajeth23/mene-monitor/actions/workflows/ci.yml)
[![CodeQL Security Scan](https://github.com/ajeth23/mene-monitor/actions/workflows/codeql.yml/badge.svg)](https://github.com/ajeth23/mene-monitor/actions/workflows/codeql.yml)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0%2B-purple.svg)](https://kotlinlang.org)

> Modern, real-time Android system monitoring and hardware diagnostics application built with **Kotlin** and **Jetpack Compose**.

<p align="left">
  <a href="https://play.google.com/store/apps/details?id=app.mene.monitor">
    <img alt="Get it on Google Play" height="50" src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" />
  </a>
</p>

---

## 📱 About The Project

**Mene Monitor** provides developer-grade, real-time hardware telemetry and diagnostic insights directly on Android devices. Built from the ground up using **Clean Architecture** and **Unidirectional Data Flow (UDF)**, it reads low-level Android system APIs and Linux `/sys/devices` kernel nodes to deliver accurate per-core CPU usage, GPU metrics, memory distribution, battery health, storage partition wear, live network speeds, and sensor data.

---

## 🛠️ Tech Stack & Architecture

- **Language:** 100% Kotlin
- **UI Framework:** Jetpack Compose (Material 3)
- **Architecture:** Clean Architecture (`data` ➔ `domain` ➔ `presentation`)
- **State & Concurrency:** Kotlin Coroutines, `StateFlow`, `SharedFlow`, `Flow`
- **Data Layer:** Reactive System DataSources reading Linux `sysfs` & Android System Services
- **Dependency Injection:** Lightweight Manual AppContainer (Service Locator / DI pattern)
- **Testing:** 
  - **Unit Tests:** JUnit 4 & Kotlinx Coroutines Test
  - **Component & Integration:** Robolectric
  - **Visual Regression:** Roborazzi Screenshot Testing

```
app/src/main/java/app/mene/monitor/
├── core/            # DI Container, Dispatchers, Extensions & Result wrappers
├── data/            # System Data Sources (CPU, GPU, Battery, Sensors, etc.) & Repository
├── domain/          # Entities/Models, Repository interfaces, & Reactive UseCases
└── presentation/    # Jetpack Compose Screens, ViewModels, Custom Gauges & Navigation
```

---

## ✨ Features

- **📊 Home Dashboard:** At-a-glance telemetry cards for CPU usage, GPU, Memory, Storage, Battery state, and Network activity.
- **🧪 Hardware Diagnostics Suite:** Interactive & automated test suite including Display Dead Pixel color sweep, Touchscreen Multi-Touch matrix test, Haptics/Vibration motor test, Flashlight/Torch toggle, and 440 Hz Speaker frequency test.
- **⚡ CPU & Core Monitor:** Live per-core frequency scaling and load percentage visualizer.
- **🎮 GPU Diagnostics:** Renderer, vendor, frequency bounds, and real-time GPU load analysis.
- **💾 Memory & Storage:** Breakdown of RAM allocation (Available, Active, Cached) and UFS/eMMC storage partition health.
- **🔋 Battery & Power:** Charging rate (mA), voltage (mV), health condition, temperature, and battery technology.
- **📡 Network Connectivity:** Live Wi-Fi & cellular upload/download speed throughput gauges and IP specifications.
- **🌡️ Sensors & Thermal Zones:** Accelerometer, gyroscope, light sensors, and device thermal throttling states.
- **⚙️ Modern Material 3 Adaptive Theme:** Full Material You dynamic color support (Android 12+ wallpaper color extraction) with automatic System Light / Dark mode adaptation and custom Theme Mode selector (System, Dark Glass, Light Clean, Material You).

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** Ladybug (2024.2.1) or newer
- **JDK 17**
- Android device or emulator running **Android 7.0 (API 24)** or higher

### Building Locally

1. **Clone the repository:**
   ```bash
   git clone https://github.com/ajeth23/mene-monitor.git
   cd mene-monitor
   ```

2. **Open in Android Studio:**
   Select **Open** and choose the `mene-monitor` project directory.

3. **Run tests:**
   ```bash
   ./gradlew testDebugUnitTest
   ```

4. **Build Debug APK:**
   ```bash
   ./gradlew assembleDebug
   ```

---

## 📊 Hardware Data Availability & System Limitations

| Hardware Metric | Source Mechanism | Non-Rooted Device Availability | Notes |
| :--- | :--- | :--- | :--- |
| **CPU Usage (Per-Core & Overall)** | Linux `/proc/stat` delta counters | ✅ Available | Calculated via real user/system/idle CPU time deltas. |
| **RAM Allocation** | Android `ActivityManager.MemoryInfo` | ✅ Available | Direct Android OS System API. |
| **Storage Capacity & Partition Usage** | Android `StatFs` API | ✅ Available | Direct Android OS System API. |
| **Battery Level, Health & Voltage** | Android `BatteryManager` Broadcast | ✅ Available | Direct Android OS System API. |
| **Sensors (Accelerometer, Gyro, Light, etc.)** | Android `SensorManager` API | ✅ Available | Direct Android OS System API. |
| **Network Throughput (Rx / Tx Speed)** | Linux `/proc/net/dev` & `TrafficStats` | ✅ Available | Calculated via kernel network interface byte deltas. |
| **CPU / GPU / Storage Thermal Temps** | Linux Kernel `sysfs` (`/sys/class/thermal/`) | ⚠️ OEM Dependent | Returns `N/A` if restricted by SELinux on non-rooted devices. |
| **GPU Clock Speed & Load** | Vendor Kernel `sysfs` (`/sys/class/kgsl/`) | ⚠️ OEM Dependent | Returns `N/A` if restricted by SELinux on non-rooted devices. |

---

## 🔒 Privacy & Security

Mene Monitor operates on a **100% On-Device Privacy** model. No internet permission (`android.permission.INTERNET`) is requested by the application; all hardware, system, and kernel telemetry measurements are processed strictly on-device. No telemetry, analytics, or user data is ever transmitted to external servers.

---

## 📄 License & Trademarks

Copyright 2026 **Mene Software Development Service**.

Licensed under the **Apache License, Version 2.0** (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at:

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

> [!NOTE]
> **Trademark Notice:** The MENE name, logo, icons, and associated branding are proprietary assets of Mene Software Development Service. While the source code is freely available under the Apache 2.0 license, rights to MENE trademarks, brand assets, and service names are strictly reserved and not granted under the software license.
