# Android App

The Android Studio project lives in this folder. It is **generated with the Android Studio
wizard** (not hand-written) — follow the steps below exactly so the whole team gets the
identical project.

> Full design: `../documentation/architecture.md` (packages §3–§4, MVVM §5, screens §7,
> classes §8). DB schema: `../documentation/database-schema.md`.

## 1. Generate the project (one team member does this, then commits)

Android Studio → **New Project → Empty Activity** (Compose) with:

| Field | Value |
|---|---|
| Name | `SmartHome` |
| Package name | `com.smarthome.monitor` |
| Save location | this `android-app/` folder |
| Language | Kotlin |
| Build configuration language | Kotlin DSL (`build.gradle.kts`) |
| Minimum SDK | API 24 (Android 7.0) |

Then:

1. Download `google-services.json` from the Firebase console (Project settings → Your apps
   → Android app `com.smarthome.monitor`) and place it in `app/`. **Commit it** (private
   coursework repo — see `.gitignore` note).
2. Apply the Google Services plugin and add dependencies (below).
3. Verify the generated app runs, then commit as `chore: android project skeleton`.

## 2. Dependencies to add

- Firebase BoM → `firebase-database`, `firebase-auth`, `firebase-messaging`
- Compose BOM, Material 3, `navigation-compose`
- Hilt (`hilt-android`, `hilt-navigation-compose`, ksp compiler)
- `lifecycle-viewmodel-compose`, `lifecycle-runtime-compose`
- `kotlinx-coroutines` (comes with Firebase/Lifecycle, explicit is fine)
- Coil (`coil-compose`) — camera snapshots
- DataStore Preferences — settings

## 3. Target package structure

Create these packages under `com.smarthome.monitor` during Week 1–2
(responsibilities in `../documentation/architecture.md` §4):

```
com.smarthome.monitor/
├── SmartHomeApp.kt              # @HiltAndroidApp
├── MainActivity.kt              # single activity + NavHost
├── di/                          # AppModule (Hilt)
├── core/
│   ├── ui/theme/                # generated M3 theme
│   ├── util/                    # Constants (HOME_ID, DB paths), DateUtils
│   └── notification/            # SmartHomeMessagingService (FCM)
├── data/
│   ├── model/                   # Device, Floor, Schedule, UsageRecord, User
│   ├── remote/                  # Firebase*DataSource — only Firebase-importing package
│   └── repository/              # repository implementations
├── domain/
│   └── repository/              # repository interfaces
└── ui/
    ├── navigation/              # Routes.kt, NavGraph.kt
    ├── components/              # DeviceCard, FloorGrid, StatusBadge, …
    ├── splash/  home/  floor/  device/  schedule/  reports/  camera/  settings/
    └── (each feature = Screen.kt + ViewModel.kt)
```

## 4. Run

Open **this folder** in Android Studio → let Gradle sync → run on an emulator
(API 30+ recommended) or a physical device. Sign-in is anonymous and automatic; no login
screen exists by design.

## 5. Conventions

- Screens observe `viewModel.uiState` via `collectAsStateWithLifecycle()`; nothing else.
- All Firebase writes follow the write contract in `../documentation/database-schema.md` §6.
- `HOME_ID = "home_001"` lives in `core/util/Constants.kt` — nowhere else.
