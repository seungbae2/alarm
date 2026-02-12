# Alarm

A reliable Android alarm application that works accurately on the lock screen and overcomes battery
optimizations and system restrictions to provide stable alarm functionality.

## Key Features

- **Precise Scheduling** — Exact alarm scheduling on Android 12+ with `SCHEDULE_EXACT_ALARM`
- **Lock Screen Support** — Displays and dismisses alarms over the lock screen via dedicated
  AlarmActivity
- **Battery Optimization Handling** — Reliable operation even in battery saver and Doze mode
- **Reboot Recovery** — Automatically restores all alarms after device reboot
- **Persistent Monitoring** — Background service continuously monitors alarm status
- **Modern UI** — Built entirely with Jetpack Compose

## Tech Stack

| Category     | Technology                                          |
|--------------|-----------------------------------------------------|
| Language     | Kotlin (100%)                                       |
| UI           | Jetpack Compose                                     |
| Architecture | MVVM, Clean Architecture (Domain-Data-Presentation) |
| Async        | Coroutines                                          |
| DI           | Hilt                                                |
| Database     | Room                                                |
| Navigation   | Navigation Compose                                  |
| Date/Time    | Kotlinx DateTime                                    |

## Architecture

Follows Google's recommended app architecture with three layers:

```
app/src/main/java/com/sb/alarm/
├── data/                    → Data layer (Room DB, repositories)
├── domain/
│   ├── model/               → Alarm domain models
│   ├── repository/          → Repository interfaces
│   └── usecase/             → Business logic (schedule, cancel, restore)
├── presentation/
│   ├── alarm/               → Alarm trigger UI
│   ├── schedule/            → Alarm scheduling UI
│   ├── service/             → AlarmService, PersistentAlarmService
│   └── receiver/            → AlarmReceiver, BootReceiver
├── shared/                  → Shared utilities
└── di/                      → Hilt modules
```

## System Challenges & Solutions

### Battery Optimization

Android aggressively kills background processes. The app uses `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`
and foreground services with persistent notifications to ensure alarm delivery.

### Lock Screen Compatibility

Uses `USE_FULL_SCREEN_INTENT` permission with `showOnLockScreen` and `turnScreenOn` flags on a
dedicated AlarmActivity to display alarms without unlocking.

### System Change Handling

BootReceiver detects device reboot, time zone changes, and system updates, then restores all
scheduled alarms from the Room database.

## How to Build

### Requirements

- Android Studio Meerkat+
- JDK 17
- Min SDK 21 / Target SDK 34

### Steps

```bash
git clone https://github.com/seungbae2/alarm.git
cd alarm
```

Open in Android Studio and sync Gradle. Run on emulator or physical device.

### Required Permissions

The app will request at runtime:

- Notification permission (Android 13+)
- Ignore battery optimizations
- Draw over other apps
- Exact alarm scheduling (Android 12+)
