# Architecture — Smart Home Monitoring & Control System

**Course:** SCS 3311 · **Scope:** Android app + hardware simulator + Firebase backend
**Audience:** the 3 team members. This is the single source of truth for all design decisions.

> **Status:** Design only. No implementation code exists yet. Model sketches in §8 are
> design contracts (field lists), not final code.

---

## Table of Contents

1. High-Level System Architecture
2. Repository Structure
3. Android Package Structure
4. Folder-by-Folder Explanation
5. MVVM Architecture Design
6. Data Flow (UI → ViewModel → Repository → Firebase, and back)
7. Android Screens
8. Kotlin Class Responsibilities (Models / ViewModels / Repositories)
9. Firebase Realtime Database (summary — full schema in `database-schema.md`)
10. Cloud Functions Design
11. Hardware Simulator Architecture
12. Device Status Model (ON / OFF / ERROR / DISCONNECTED)
13. Practical Guardrails (what we deliberately do NOT build)
14. Technology & Library Choices

The development roadmap, team roles and Git workflow live in `development-plan.md`.

---

## 1. High-Level System Architecture

```
┌──────────────────────────────┐        ┌──────────────────────────────┐
│      ANDROID APP             │        │   HARDWARE SIMULATOR (Web)   │
│  Kotlin · Jetpack Compose    │        │  HTML + CSS + Vanilla JS     │
│  MVVM · Hilt · StateFlow     │        │  Firebase Web SDK (CDN)      │
└──────────────┬───────────────┘        └──────────────┬───────────────┘
               │  Firebase Android SDK                │  Firebase Web SDK
               │  (read/write + realtime listeners)   │  (read/write + listeners)
               ▼                                      ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         FIREBASE PROJECT                            │
│                                                                     │
│  ┌──────────────────────┐   ┌───────────────────────────────────┐   │
│  │  Realtime Database   │   │  Authentication (Anonymous)       │   │
│  │  (single source of   │   │  - identifies app & simulator     │   │
│  │   truth for state)   │   │  - enables security rules         │   │
│  └─────────┬────────────┘   └───────────────────────────────────┘   │
│            │ database triggers                                      │
│  ┌─────────▼───────────────────────────────────────────────────┐   │
│  │              Cloud Functions (Node.js 20)                   │   │
│  │  1. usageLogger     (RTDB onUpdate trigger)                 │   │
│  │  2. safetyMonitor   (scheduled, every 1 min)                │   │
│  │  3. scheduleRunner  (scheduled, every 1 min)                │   │
│  └─────────┬───────────────────────────────────────────────────┘   │
│            │ publishes to FCM topic "home_<homeId>"                 │
│  ┌─────────▼────────────┐                                          │
│  │  Cloud Messaging     │──────────► push notification → Android   │
│  └──────────────────────┘                                          │
└─────────────────────────────────────────────────────────────────────┘
```

**Central architectural decision:** Firebase Realtime Database is the *single source of
truth*. The Android app and the simulator never communicate directly. Both read from and
write to the same database nodes, and Firebase realtime listeners push every change to both
sides. Bidirectional synchronization is therefore a property of the platform, not code we
must build and debug.

---

## 2. Repository Structure

```
smart-home-monitoring-system/
│
├── android-app/                  # Android Studio project root (Gradle build lives here)
│   ├── app/                      # The single application module
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   ├── google-services.json      # Firebase config (committed — coursework, private repo)
│   └── README.md                 # Project generation steps + target package structure
│
├── hardware-simulator/           # Static web app — no build step
│   ├── index.html
│   ├── css/main.css
│   ├── js/
│   │   ├── config/firebase-config.js
│   │   ├── services/firebase-service.js
│   │   ├── devices/              # light.js, outlet.js, switch-panel.js, iron.js, camera.js
│   │   ├── ui/floor-view.js
│   │   └── app.js
│   ├── assets/                   # Icons, camera placeholder images
│   └── README.md
│
├── cloud-functions/
│   ├── firebase.json             # Functions config (+ RTDB rules hosting, emulator config)
│   └── functions/
│       ├── package.json
│       ├── index.js              # Exports all functions
│       └── src/                  # safety.js, scheduling.js, usage.js, notify.js
│
├── documentation/
│   ├── architecture.md           # This document
│   ├── database-schema.md        # Full RTDB JSON tree + path ownership + security rules
│   ├── development-plan.md       # 4-week roadmap, roles, Git workflow
│   └── demo-script.md            # Demo video plan (≤ 25 min)
│
├── .gitignore                    # Android + Node + Firebase + macOS
└── README.md
```

One repository, three independently runnable artifacts, one shared documentation folder.

---

## 3. Android Package Structure

Single `app` module (no multi-module — overkill for 4 weeks). Feature-based packaging:

```
com.smarthome.monitor/
│
├── SmartHomeApp.kt                    # @HiltAndroidApp Application class
├── MainActivity.kt                    # Single activity hosting the NavHost
│
├── di/
│   └── AppModule.kt                   # Hilt: provides Firebase singletons + repositories
│
├── core/
│   ├── ui/theme/                      # Color.kt, Theme.kt, Type.kt (Material 3)
│   ├── util/                          # DateUtils, Constants (DB paths, HOME_ID), extensions
│   └── notification/
│       └── SmartHomeMessagingService.kt   # FCM service: topic subscription + notifications
│
├── data/
│   ├── model/                         # Device, Floor, Schedule, UsageRecord, User, enums
│   ├── remote/                        # The ONLY package allowed to import Firebase Database
│   │   ├── FirebaseDeviceDataSource.kt
│   │   ├── FirebaseFloorDataSource.kt
│   │   ├── FirebaseScheduleDataSource.kt
│   │   └── FirebaseUsageDataSource.kt
│   └── repository/                    # Repository implementations (delegate to data sources)
│
├── domain/
│   └── repository/                    # Repository INTERFACES only (no classes, no logic)
│
└── ui/
    ├── navigation/
    │   ├── NavGraph.kt                # Single NavHost
    │   └── Routes.kt                  # All route strings in one place
    ├── components/                    # Shared: DeviceCard, FloorGrid, PowerToggle,
    │                                #         StatusBadge, LoadingBox, EmptyState
    ├── splash/                        # SplashScreen + SplashViewModel
    ├── home/                          # HomeScreen + HomeViewModel        (dashboard)
    ├── floor/                         # FloorScreen + FloorViewModel      (one floor's grid)
    ├── device/                        # DeviceControlSheet + DeviceControlViewModel
    ├── schedule/                      # ScheduleScreen + ScheduleViewModel
    ├── reports/                       # ReportsScreen + ReportsViewModel
    ├── camera/                        # CameraScreen + CameraViewModel
    └── settings/                      # SettingsScreen + SettingsViewModel
```

**Why feature-based `ui/` packaging:** each member owns whole feature folders → minimal
merge conflicts; every screen sits next to its ViewModel; the pattern is identical in every
folder so it is learned once and applied eight times.

---

## 4. Folder-by-Folder Explanation

### 4.1 Repository root

| Folder | Purpose | Suggested owner |
|---|---|---|
| `android-app/` | Complete Android Studio project. This folder is what you open in Android Studio. | Members A & B |
| `hardware-simulator/` | Standalone static web app acting as the "physical house". Open via any local static server. | Member C |
| `cloud-functions/` | Node.js backend automation, deployed with `firebase deploy --only functions`. | Member B |
| `documentation/` | Architecture, DB schema, plan, demo script — source material for the technical report. | All |

### 4.2 Android packages

| Package | Responsibility | Hard rule |
|---|---|---|
| `di` | Hilt modules: `FirebaseDatabase`, `FirebaseAuth`, repository bindings. | One module is enough. |
| `core/ui/theme` | Generated Material 3 theme. | Touched once, never again. |
| `core/util` | Time formatting ("2h 15m"), constants (`HOME_ID`, DB paths), small helpers. | No UI, no Firebase. |
| `core/notification` | `FirebaseMessagingService`: subscribes to home topic, posts system notifications. | Must declare channel on Android 8+. |
| `data/model` | Plain Kotlin data classes mirroring the RTDB JSON. No logic inside. | Must stay Firebase-deserializable (default values for every field). |
| `data/remote` | Builds RTDB references, attaches listeners, exposes `Flow`s and suspend write functions. | **Only** package importing `com.google.firebase.database`. |
| `data/repository` | Implements domain interfaces; maps snapshots → models. | No Android UI imports. |
| `domain/repository` | Interfaces only. | Keeps ViewModels testable with fakes. |
| `ui/navigation` | `Routes.kt` + one `NavHost`. | All navigation in one place. |
| `ui/components` | Composables reused by ≥ 2 screens. | If used once, keep it in the feature folder. |
| `ui/<feature>` | One screen composable + one ViewModel. | Composable is dumb; ViewModel owns all state/logic. |

---

## 5. MVVM Architecture Design

```
┌─────────────────────────────────────────────────────────────────┐
│ UI LAYER  (Composables)                                         │
│  - observe viewModel.uiState via collectAsStateWithLifecycle()  │
│  - send user events UP: viewModel.setPower(id, true)            │
│  - ZERO Firebase/business logic                                 │
└──────────────▲──────────────────────────────┬───────────────────┘
               │ StateFlow<UiState>            │ plain function calls
┌──────────────┴──────────────────────────────▼───────────────────┐
│ VIEWMODEL LAYER                                                 │
│  - exposes one immutable UiState data class per screen          │
│    (loading / data / error)                                     │
│  - exposes events as plain functions                            │
│  - viewModelScope for all coroutines                            │
│  - survives configuration change; holds no View references      │
└──────────────▲──────────────────────────────┬───────────────────┘
               │ Flow<Model>                   │ suspend fun calls
┌──────────────┴──────────────────────────────▼───────────────────┐
│ REPOSITORY LAYER  (interface in domain/, impl in data/)         │
│  - single source of truth for ViewModels                        │
│  - one repository per domain area (Device, Floor, Schedule…)    │
└──────────────▲──────────────────────────────┬───────────────────┘
               │ callbackFlow                  │ setValue / updateChildren
┌──────────────┴──────────────────────────────▼───────────────────┐
│ DATA SOURCE LAYER  (Firebase Realtime Database)                 │
└─────────────────────────────────────────────────────────────────┘
```

**Layer rules (enforced in code review):**

1. Composables never import `com.google.firebase.*`.
2. ViewModels never reference Composables and never hold a `Context` (except via Hilt-provided `Application` if truly needed).
3. Repositories never reference ViewModels or UI.
4. Data sources are the only Firebase-touching classes.
5. Unidirectional data flow: state flows **down**, events flow **up**. Always.

Example UI state (pattern repeated for every screen):

```kotlin
data class FloorUiState(
    val isLoading: Boolean = true,
    val floorName: String = "",
    val devices: List<Device> = emptyList(),
    val errorMessage: String? = null
)
```

---

## 6. Data Flow

### 6.1 Read path (automatic sync — used in BOTH directions)

```
Firebase RTDB node changes
      │  (ValueEventListener fires)
      ▼
DataSource: callbackFlow { ref.addValueEventListener(...) } → Flow<List<Device>>
      ▼
Repository: .map { snapshots → models }
      ▼
ViewModel: .stateIn(viewModelScope, WhileSubscribed(5000), initial)
      ▼
Composable: collectAsStateWithLifecycle() → recomposition
```

### 6.2 Write path (user action)

```
User taps toggle in Composable
      ▼
viewModel.setPower(deviceId, true)
      ▼
deviceRepository.setPower(deviceId, true)                (suspend fun)
      ▼
dataSource: db.getReference("homes/HOME_ID/devices/$id/state")
            .updateChildren(mapOf(
                "isOn" to true,
                "lastOnAt" to ServerValue.TIMESTAMP,
                "lastChangedBy" to "android"))
      ▼
Firebase propagates the change → the app's own listener AND the
simulator's listener both fire → both UIs update from the database.
```

**Key decision — no optimistic local updates.** The app never mutates its own state after a
write; it writes to Firebase and lets the listener push the confirmed value back down. RTDB
latency on normal Wi-Fi is ~100–300 ms (imperceptible), and this guarantees the app and
simulator can *never* disagree — which is exactly what the "no manual refresh" requirement
demands.

### 6.3 Worked example (the demo scenario)

1. User taps **Light ON** in Android → `updateChildren` on `.../state`.
2. Simulator's `onValue` listener fires → bulb icon turns on. *(forward sync)*
3. User clicks the bulb in the simulator → writes `isOn: false`.
4. Android's listener fires → `FloorUiState.devices` updates → card recomposes to OFF. *(reverse sync)*
5. If the device was an iron, the `usageLogger` Cloud Function sees the OFF transition and
   accumulates today's `activeMinutes` in `usage/`.

---

## 7. Android Screens

| # | Route | Screen | Contents |
|---|---|---|---|
| 1 | `splash` | Splash | Logo; ensures anonymous sign-in; auto-navigates to `home`. |
| 2 | `home` | Home Dashboard | Floor list (name, device count, "N ON"), active safety alerts (e.g. "Iron ON for 20 min"), entries to Reports/Settings. FAB → Add Floor. |
| 3 | `floor/{floorId}` | Floor View | Grid (from `gridColumns × gridRows`) with device cards at `position.x/y`, each showing name + status badge. Tap → control sheet. FAB → Add Device (dialog: name, type, room, grid cell). |
| 4 | `device/{deviceId}` | Device Control | **Modal bottom sheet**, content varies by `DeviceType`: toggle (outlet/light), N sub-toggles (switch panel), toggle + live "ON for X / limit Y min" + limit editor (iron), schedule shortcut (light), "Open camera" (camera). |
| 5 | `schedule/{deviceId}` | Schedule | ON time, OFF time, enabled toggle. Saved to `schedules/`. |
| 6 | `reports` | Reports | Date selector (default today) + per-device rows: active time, sessions, estimated Wh. Simple bar chart drawn with Compose Canvas — **no chart library**. |
| 7 | `camera/{deviceId}` | Camera | Mock snapshot image (Coil), mock stream URL text, connection status badge, refresh-snapshot button. |
| 8 | `settings` | Settings | Home name, notification toggle, default iron safety limit, about. |

**Login screen: deliberately omitted.** We use Firebase **Anonymous Authentication** — one
line of code, invisible to the user, but it satisfies security rules (`auth != null`) and
gives every install a stable `uid`. If the rubric demands a visible login, email/password
auth can be added later behind `AuthRepository` without touching any other layer. Be ready
to defend this trade-off in the viva.

---

## 8. Kotlin Class Responsibilities

### 8.1 Models (`data/model/`)

**Pragmatic decision: one flat `Device` class with a type enum — not a sealed class
hierarchy.** Firebase deserializes flat classes with default values trivially; sealed
hierarchies require custom mapping code. We lose a little type purity and save roughly a
week. Per-type rendering is done in the UI with `when (device.type)`.

```kotlin
enum class DeviceType { OUTLET, SWITCH_PANEL, IRON, LIGHT, CAMERA }

data class Device(
    val id: String = "",
    val name: String = "",
    val type: DeviceType = DeviceType.OUTLET,
    val floorId: String = "",
    val room: String = "",
    val position: GridPosition = GridPosition(),      // x, y on the floor grid
    val state: DeviceState = DeviceState(),
    val config: DeviceConfig = DeviceConfig()
)

data class GridPosition(val x: Int = 0, val y: Int = 0)

data class DeviceState(
    val isOn: Boolean = false,                        // OUTLET, IRON, LIGHT
    val switches: Map<String, Boolean> = emptyMap(),  // SWITCH_PANEL: "s1"→true … (variable count!)
    val online: Boolean = true,                       // written by simulator presence (§11)
    val error: Boolean = false,                       // written by simulator "fault" button (§11)
    val streamUrl: String = "",                       // CAMERA (mock URI stream)
    val snapshotUrl: String = "",                     // CAMERA (mock snapshot image)
    val lastOnAt: Long = 0L,                          // server timestamp when turned ON
    val lastChangedBy: String = ""                    // "android" | "simulator" | "function"
)

data class DeviceConfig(
    val maxActiveMinutes: Int = 30,                   // IRON: max_on_duration (safety rule)
    val wattage: Int = 0                              // energy estimate: Wh = min/60 × W
)

data class Floor(
    val id: String = "",
    val name: String = "",
    val order: Int = 0,
    val gridColumns: Int = 4,
    val gridRows: Int = 4
)

data class Schedule(
    val id: String = "",
    val deviceId: String = "",
    val onTime: String = "18:00",                     // "HH:mm" in home timezone
    val offTime: String = "06:00",
    val enabled: Boolean = true,
    val lastRunKey: String = ""                       // idempotency guard, see §10
)

data class UsageRecord(
    val date: String = "",                            // "yyyy-MM-dd"
    val activeMinutes: Long = 0,
    val sessions: Int = 0,
    val energyWh: Double = 0.0
)

data class User(val uid: String = "", val homeId: String = "")
```

Notes:
- `switches: Map<String, Boolean>` is what makes a *variable* number of gang switches (2, 3,
  5…) possible under one device entity — a spec requirement.
- `lastOnAt` is the linchpin: both the safety cutoff and usage tracking derive everything
  from it.

### 8.2 ViewModels

| ViewModel | State it owns | Events it handles |
|---|---|---|
| `SplashViewModel` | auth-check result | `ensureSignedIn()` → navigate |
| `HomeViewModel` | floors, per-floor ON counts, safety alerts (irons currently ON) | add / rename / delete floor |
| `FloorViewModel` | devices of one floor as `StateFlow` | add device, delete device, open control sheet |
| `DeviceControlViewModel` | single device as `StateFlow`; ticking elapsed-time timer for irons | `setPower`, `setPanelSwitch(key, on)`, `setMaxActiveMinutes`, open schedule |
| `ScheduleViewModel` | schedule of one device | edit times, toggle enabled, save |
| `ReportsViewModel` | selected date, `List<Pair<Device, UsageRecord>>`, totals | change date |
| `CameraViewModel` | single camera device | refresh snapshot (append cache-busting query param) |
| `SettingsViewModel` | home name, notification preference, default safety limit | rename home, toggle FCM topic subscription |

All eight follow the identical pattern: one `StateFlow<XUiState>` + plain event functions.

### 8.3 Repositories

Interfaces in `domain/repository/`, implementations in `data/repository/`, bound via Hilt.

| Interface | Key members |
|---|---|
| `AuthRepository` | `suspend fun ensureSignedIn(): User` (anonymous), `val currentUserId` |
| `FloorRepository` | `observeFloors(): Flow<List<Floor>>`, `addFloor(name)`, `renameFloor(id, name)`, `deleteFloor(id)` |
| `DeviceRepository` | `observeDevicesOnFloor(floorId)`, `observeDevice(deviceId)`, `observeAllDevices()`, `setPower(id, on)`, `setPanelSwitch(id, key, on)`, `updateConfig(id, config)`, `addDevice(device)`, `deleteDevice(id)` |
| `ScheduleRepository` | `observeScheduleFor(deviceId)`, `upsertSchedule(schedule)`, `setEnabled(id, enabled)` |
| `UsageRepository` | `observeUsageForDate(date): Flow<Map<deviceId, UsageRecord>>`, `observeUsage(deviceId, date)` |
| `SettingsRepository` | DataStore-backed: notification preference, default safety limit |

Simplification allowed if time pressure hits: drop the interfaces and let ViewModels depend
on concrete repositories. Keep the package boundaries either way.

---

## 9. Firebase Realtime Database (Summary)

Full annotated tree, path ownership table, and security rules: **`database-schema.md`**.
Condensed view:

```
homes/{homeId}/
├── meta/            name, timezone
├── floors/{floorId} name, order, gridColumns, gridRows
├── devices/{deviceId}
│   ├── name, type, floorId, room, position{x,y}
│   ├── state/       isOn, switches{}, online, error, streamUrl, snapshotUrl, lastOnAt, lastChangedBy
│   └── config/      maxActiveMinutes, wattage
├── schedules/{scheduleId}   deviceId, onTime, offTime, enabled, lastRunKey
├── usage/{deviceId}/{yyyy-MM-dd}   activeMinutes, sessions, energyWh, autoCutoffs
└── alerts/{alertId}          title, message, severity, deviceId, timestamp, isRead
users/{uid}/         homeId
```

Five decisions to be able to defend:

1. **Flat `devices` map with `floorId` field** — not nested under floors. Query with
   `orderByChild("floorId")`. Nesting would force reading whole floors to get one device.
2. **`state` and `config` separated** — state changes constantly, config rarely; listeners
   stay cheap and precise.
3. **`lastOnAt` server timestamp** — single value powering both safety and usage logic.
4. **Usage pre-aggregated per day** by the Cloud Function — the Reports screen reads one
   tiny node per device instead of scanning an event log.
5. **FCM topic messaging** (`home_{homeId}`) — no per-device token bookkeeping in the DB.

---

## 10. Cloud Functions Design

Three functions, each < ~40 lines, Node.js 20, firebase-admin SDK. All times use the home's
`meta.timezone`. All state writes set `lastChangedBy` so the origin is visible in the DB.

### 10.1 `usageLogger` — RTDB trigger

- **Trigger:** `onUpdate('/homes/{homeId}/devices/{deviceId}/state/isOn')`
- **Logic:** only act on a `true → false` transition. Read `state.lastOnAt`, compute
  `minutes = (now − lastOnAt) / 60000`. Read `config.wattage`. Atomically increment the
  daily record at `usage/{deviceId}/{yyyy-MM-dd}` using `ServerValue.increment`:
  `activeMinutes += minutes`, `sessions += 1`, `energyWh += minutes/60 × wattage`.
- **Why a trigger (not app-side counting):** works no matter which client flipped the
  switch, and can't be gamed by the app being closed.

### 10.2 `safetyMonitor` — scheduled (the spec's "server-side safety cutoff")

- **Trigger:** Cloud Scheduler, every 1 minute.
- **Logic:** query devices where `type == "IRON"` and `state/isOn == true`. For each, if
  `now − lastOnAt > config.maxActiveMinutes × 60000`:
  1. write `state.isOn = false`, `state.lastChangedBy = "safetyMonitor"`;
  2. publish FCM to topic `home_{homeId}` — title "Safety cutoff", body
     `"<device name> was automatically turned OFF after <limit> min"`.
- The simulator's own per-iron countdown display is cosmetic; **this function is the actual
  enforcement**, which is the point of the requirement ("regulated by a backend cloud
  listener or worker process").

### 10.3 `scheduleRunner` — scheduled (light automation)

- **Trigger:** Cloud Scheduler, every 1 minute.
- **Logic:** read all `enabled` schedules. Compute current `HH:mm` in `meta.timezone`.
  - If it equals `onTime` and `lastRunKey != "<today>-ON"` → set device `isOn = true`,
    store `lastRunKey = "<today>-ON"`.
  - Same pattern for `offTime` with `-OFF`.
- `lastRunKey` makes the function **idempotent**: re-running within the same minute can
  never double-toggle.

### 10.4 Practical notes

- Scheduled functions require the **Blaze (pay-as-you-go) plan** — expected cost is $0.00
  within free quotas (2M invocations/month free; we use ≈ 90k/month). Set a budget alert
  for safety and mention it in the report.
- Develop against the **Firebase Emulator Suite** first; deploy only when logic is proven.
- Notifications: Android subscribes to the topic after sign-in; `SmartHomeMessagingService`
  creates the notification channel and posts the alert. No token storage needed.

---

## 11. Hardware Simulator Architecture

### 11.1 Folder structure

```
hardware-simulator/
├── index.html                     # Floor tabs + device grid, loads app.js as ES module
├── css/
│   └── main.css                   # Card grid, status colors, simple responsive layout
├── js/
│   ├── config/
│   │   └── firebase-config.js     # Firebase web config object + HOME_ID constant
│   ├── services/
│   │   └── firebase-service.js    # THE ONLY file importing Firebase:
│   │                              #   signInAnonymously, onValue listener, update() writes,
│   │                              #   presence via .info/connected + onDisconnect()
│   ├── devices/
│   │   ├── base-device.js         # shared card factory: name, status badge, click wiring
│   │   ├── light.js               # bulb icon + on/off styling
│   │   ├── outlet.js              # socket icon + on/off styling
│   │   ├── switch-panel.js        # renders N sub-toggles from state.switches map
│   │   ├── iron.js                # toggle + live countdown (lastOnAt + maxActiveMinutes)
│   │   └── camera.js              # <img> snapshot, stream URL text, status dot
│   ├── ui/
│   │   └── floor-view.js          # groups devices by floorId, renders grid + tabs
│   └── app.js                     # boot: auth → subscribe → render; delegates events
├── assets/                        # icons (emoji/SVG), placeholder camera image
└── README.md
```

### 11.2 How it works

- **Read (cloud → simulator):** one `onValue` listener on `/homes/{HOME_ID}` (or
  `/devices` + `/floors`). Every change re-renders the affected card. Toggling from the
  Android app appears instantly.
- **Write (simulator → cloud):** clicking a card calls `update(ref(db, 'devices/' + id +
  '/state'), { isOn: ..., lastOnAt: serverTimestamp(), lastChangedBy: 'simulator' })`.
  The Android app updates instantly. Same write contract as the app — by design.
- **Presence (powers the `DISCONNECTED` status):** on `.info/connected`, the simulator sets
  `state.online = true` for its devices and registers `onDisconnect().update(online=false)`.
  Closing the browser tab visibly disconnects devices in the app — an excellent demo moment.
- **Fault simulation (powers the `ERROR` status):** each card has a small "⚠ fault" button
  toggling `state.error`. This exists because real hardware faults can't be demoed otherwise.
- **Iron countdown:** cosmetic client-side timer computed from `lastOnAt` and
  `maxActiveMinutes`; real enforcement is the Cloud Function (§10.2).
- **Camera:** snapshot from <https://picsum.photos> with a cache-busting query param for
  "refresh", plus a fake `streamUrl` string and the `online` dot.

### 11.3 Why vanilla JS (Option 1), not React

Zero build step, zero npm toolchain, runs from any static server, and the whole team can
read it. The simulator's job is to be a credible "physical device", not to showcase a
frontend framework. React would double Member C's setup time for no marks.

---

## 12. Device Status Model (ON / OFF / ERROR / DISCONNECTED)

The spec requires four statuses. We derive them from two booleans + the power flag —
never store a status string:

| Priority | Condition | Displayed status |
|---|---|---|
| 1 | `state.online == false` | `DISCONNECTED` (grey) |
| 2 | `state.error == true` | `ERROR` (red) |
| 3 | `state.isOn == true` | `ON` (green/amber) |
| 4 | otherwise | `OFF` (neutral) |

Both clients implement this same tiny `when`/`switch` (Android: a `Device.status()` helper
in `core/util`; simulator: in `base-device.js`). One rule, two implementations, always
consistent.

---

## 13. Practical Guardrails — What We Deliberately Do NOT Build

Student project, 3 people, ~4 weeks. Every "no" below protects the deadline:

| We do NOT use | Because |
|---|---|
| Room / local database | RTDB's built-in offline persistence is enough; Firebase is the single source of truth. |
| WorkManager for schedules | Scheduling must run with the app **closed** → belongs in Cloud Functions. |
| Clean Architecture use-case layer / multi-module | Repository pattern already satisfies the rubric; more layers = more ceremony, same marks. |
| Chart library (MPAndroidChart etc.) | Reports need simple bars → ~30 lines of Compose Canvas. |
| Real login screen / multi-user | Anonymous auth covers security rules; one hardcoded `HOME_ID` shared by app + simulator. |
| Real camera streaming | Spec says **mock** snapshots / mock URI. Picsum image + fake URL. |
| Raw toggle-event log in DB | Usage is pre-aggregated per day by `usageLogger`. Event logs don't scale and aren't needed. |
| Optimistic UI writes | Let Firebase echo the confirmed state (§6.2) — app and simulator can never disagree. |
| More than 3 Cloud Functions | Each new function is deploy/debug surface. Three covers every requirement. |

---

## 14. Technology & Library Choices

**Android**
Kotlin 2.x · AGP 8.x · Jetpack Compose (BOM 2024.xx) + Material 3 · Navigation Compose ·
Hilt (DI) · StateFlow + `lifecycle-runtime-compose` (`collectAsStateWithLifecycle`) ·
Kotlin Coroutines · Coil (camera snapshots) · Firebase BoM: `firebase-database`,
`firebase-auth`, `firebase-messaging` · DataStore Preferences (settings) · minSdk 24.

**Hardware simulator**
Vanilla HTML/CSS/JS (ES modules) · Firebase Web SDK v10+ via CDN (`gstatic` imports) ·
any static file server (`npx serve`, VS Code Live Server, `python3 -m http.server`).

**Cloud Functions**
Node.js 20 · `firebase-functions` + `firebase-admin` · Cloud Scheduler (1-min cron) ·
FCM topic messaging · Firebase Emulator Suite for local development.

**Backend services**
Realtime Database (source of truth) · Anonymous Authentication · Cloud Messaging.

---

*End of architecture document. Implementation order: see `development-plan.md`.*
