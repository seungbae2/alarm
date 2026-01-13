# Alarm App

A reliable Android alarm application. It works accurately even on the
lock screen and overcomes battery optimizations and system restrictions
to provide stable alarm functionality.

## ✨ Key Features

- **Precise Alarms**: Supports exact alarm scheduling on Android 12+
- **Lock Screen Support**: Displays and dismisses alarms over the lock
  screen
- **Battery Optimization Handling**: Works reliably even in battery
  saver mode
- **Reboot Recovery**: Automatically restores alarms after device
  reboot
- **Persistent Monitoring**: Continuously monitors alarm status in the
  background
- **Modern UI**: Built with Jetpack Compose

## 🛠 Tech Stack

### Core Technologies

- **Kotlin**: 100% Kotlin
- **Jetpack Compose**: Declarative UI
- **Coroutines**: Async and concurrency

### Architecture & Patterns

- **Google App Architecture**: Domain--Data--Presentation layers
- **MVVM Pattern**
- **Repository Pattern**

### Libraries

- **Hilt**: Dependency Injection
- **Room**: Local database
- **Navigation Compose**
- **Kotlinx DateTime**

## 📱 System Requirements

- **Min SDK**: Android 5.0 (API 21)
- **Target SDK**: Android 14 (API 34)
- **Compile SDK**: Android 14 (API 34)

## 🏗 Project Structure

    app/src/main/java/com/sb/alarm/
    ├── MainActivity.kt
    ├── AlarmApplication.kt
    ├── AppNavGraph.kt
    ├── data/
    ├── domain/
    │   ├── model/
    │   ├── repository/
    │   └── usecase/
    ├── presentation/
    │   ├── alarm/
    │   ├── schedule/
    │   ├── service/
    │   └── receiver/
    ├── shared/
    └── di/

## 🚀 Installation & Run

### Required Permissions

The app will request:

- **Notification permission**
- **Ignore battery optimizations**
- **Draw over other apps**
- **Exact alarm scheduling**

## 🔧 Main Components

### Services

- **AlarmService**
- **PersistentAlarmService**

### Receivers

- **AlarmReceiver**
- **BootReceiver**

### Activities

- **MainActivity**
- **AlarmActivity**

## 📝 Development Notes

### Battery Optimization

- Uses `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`
- Foreground services for critical alarms
- Persistent monitoring service

### Lock Screen Compatibility

- Uses `USE_FULL_SCREEN_INTENT`
- Dedicated AlarmActivity
- `showOnLockScreen`, `turnScreenOn` flags

### System Change Handling

- Detects reboot, time changes, updates
- Restores alarms via BootReceiver
- Auto-adjusts for timezone changes
