# Technical Report — Smart Home Monitoring & Control System

**Course:** SCS 3311 — Mobile Application Design & Development (Mini-Project)
**Deliverable:** #2 — Technical Documentation (Synchronization, Floor Representation, Simulator Operations)

---

## Table of Contents

1. [System Overview](#1-system-overview)
   - 1.1 [Android App — MVVM Layered Architecture](#11-android-app--mvvm-layered-architecture)
   - 1.2 [Simulator — Component Architecture](#12-simulator--component-architecture)
2. [Synchronization Mechanism](#2-synchronization-mechanism)
   - 2.5 [Bidirectional Sync — Sequence Diagram](#25-bidirectional-sync--sequence-diagram)
3. [Floor Representation](#3-floor-representation)
   - 3.3 [Floor Grid — Visual Mapping](#33-floor-grid--visual-mapping)
4. [Simulator Operations](#4-simulator-operations)
5. [Firebase Data Structure](#5-firebase-data-structure)
   - 5.2 [Path Ownership — Who Reads and Writes What](#52-path-ownership--who-reads-and-writes-what)
6. [Cloud Functions — Server-Side Automation](#6-cloud-functions--server-side-automation)
   - 6.0 [Cloud Functions — Trigger Flow Diagram](#60-cloud-functions--trigger-flow-diagram)
7. [Device Status Model](#7-device-status-model)
   - 7.1 [Status Derivation Flow](#71-status-derivation-flow)
8. [Technology Stack](#8-technology-stack)

---

## 1. System Overview

```
┌──────────────────────────────┐        ┌──────────────────────────────┐
│      ANDROID APP             │        │   HARDWARE SIMULATOR (Web)   │
│  Kotlin · Jetpack Compose    │        │  React · TypeScript · Vite   │
│  MVVM · Hilt · StateFlow     │        │  Firebase Web SDK            │
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
│  │              Cloud Functions (Node.js)                       │   │
│  │  1. usageLogger     (RTDB onUpdate trigger)                 │   │
│  │  2. safetyMonitor   (RTDB onUpdate + self-kick watchdog)    │   │
│  │  3. scheduleRunner  (RTDB onWrite + self-kick loop)         │   │
│  └─────────┬───────────────────────────────────────────────────┘   │
│            │ publishes to FCM topic "home_<homeId>"                 │
│  ┌─────────▼────────────┐                                          │
│  │  Cloud Messaging     │──────────► push notification → Android   │
│  └──────────────────────┘                                          │
└─────────────────────────────────────────────────────────────────────┘
```

**Central design decision:** Firebase Realtime Database is the *single source of truth*. The Android app and the web simulator never talk to each other directly — both only read and write to the same database, and Firebase's own real-time listeners push every change to both sides. That's what gives us bidirectional sync without us having to build any custom networking code.

### 1.1 Android App — MVVM Layered Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│ UI LAYER  (Jetpack Compose)                                         │
│                                                                     │
│  HomeScreen    FloorScreen    DeviceControlSheet    ReportsScreen   │
│  AlertsScreen  ScheduleScreen SettingsScreen        CameraScreen    │
│                                                                     │
│  - observe viewModel.uiState via collectAsState()                   │
│  - send user events UP: viewModel.setPower(id, true)                │
│  - ZERO Firebase / business logic                                   │
└──────────────▲──────────────────────────────┬───────────────────────┘
               │ StateFlow<UiState>           │ plain function calls
┌──────────────┴──────────────────────────────▼───────────────────────┐
│ VIEWMODEL LAYER                                                     │
│                                                                     │
│  HomeViewModel       FloorViewModel       DeviceControlViewModel    │
│  ReportsViewModel    ScheduleViewModel    AlertsViewModel           │
│                                                                     │
│  - one immutable UiState data class per screen                      │
│  - viewModelScope for all coroutines                                │
│  - survives configuration change                                    │
└──────────────▲──────────────────────────────┬───────────────────────┘
               │ Flow<Model>                  │ suspend fun calls
┌──────────────┴──────────────────────────────▼───────────────────────┐
│ REPOSITORY LAYER  (interface in domain/, impl in data/)             │
│                                                                     │
│  DeviceRepository    FloorRepository    ScheduleRepository          │
│  UsageRepository     AlertRepository    AuthRepository              │
│                                                                     │
│  - single source of truth for ViewModels                            │
│  - bound via Hilt dependency injection                              │
└──────────────▲──────────────────────────────┬───────────────────────┘
               │ callbackFlow                 │ updateChildren / await
┌──────────────┴──────────────────────────────▼───────────────────────┐
│ DATA SOURCE LAYER  (only package importing Firebase SDK)            │
│                                                                     │
│  FirebaseDeviceDataSource    FirebaseFloorDataSource                │
│  FirebaseScheduleDataSource  FirebaseUsageDataSource                │
│  FirebaseAlertDataSource     FirebaseActivityDataSource             │
│                                                                     │
│  - ValueEventListener → callbackFlow → Flow<T>                     │
│  - suspend write functions using .await()                           │
└──────────────▲──────────────────────────────┬───────────────────────┘
               │                              │
               ▼                              ▼
        Firebase Realtime Database      Firebase Auth (Anonymous)
```

### 1.2 Simulator — Component Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                    SIMULATOR DASHBOARD (index.tsx)                   │
│                                                                     │
│  ┌─────────────┐  ┌──────────────────────────────────────────────┐  │
│  │ FloorSidebar│  │  StatusBar (connection: connected/syncing/   │  │
│  │             │  │               offline)                       │  │
│  │ - floor list│  ├──────────────────────────────────────────────┤  │
│  │ - selection │  │                                              │  │
│  │ - add floor │  │  FloorGrid                                   │  │
│  │             │  │  ┌──────────────────────────────────────┐    │  │
│  └─────────────┘  │  │  floor plan image (background)       │    │  │
│                   │  │  ┌─────┬─────┬─────┬─────┐           │    │  │
│                   │  │  │Tile │Tile │  +  │Tile │  ← CSS    │    │  │
│                   │  │  ├─────┼─────┼─────┼─────┤    Grid   │    │  │
│                   │  │  │Tile │  +  │Tile │Tile │           │    │  │
│                   │  │  ├─────┼─────┼─────┼─────┤           │    │  │
│                   │  │  │  +  │Tile │Tile │  +  │           │    │  │
│                   │  │  └─────┴─────┴─────┴─────┘           │    │  │
│                   │  └──────────────────────────────────────┘    │  │
│                   ├──────────────────────────────────────────────┤  │
│                   │  EventLog (rolling 200 entries, diff-based)  │  │
│                   └──────────────────────────────────────────────┘  │
│                                                                     │
│  Dialogs: AddFloorDialog · AddDeviceDialog · DeviceDetailDialog     │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │  SimulatorProvider (React Context)                            │   │
│  │  - floors[], devices[], logs[], connection state              │   │
│  │  - onValue(homeRef) → parse → setState                        │   │
│  │  - toggleDevice, addDevice, removeDevice, setStatus, etc.     │   │
│  │  - Firebase auth + presence (onDisconnect)                    │   │
│  └──────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 2. Synchronization Mechanism

### 2.1 Core Principle

Bidirectional synchronization is achieved entirely through Firebase Realtime Database's built-in real-time listener infrastructure. There is **no direct communication** between the Android app and the web simulator. Both clients:

1. **Write** state changes to the same Firebase database nodes.
2. **Listen** to those same nodes via `ValueEventListener` (Android) and `onValue` (Web SDK).
3. **React** to every change pushed by Firebase automatically — no polling, no manual refresh.

### 2.2 Read Path (Cloud → Client)

When any writer (app, simulator, or Cloud Function) changes a database node, Firebase pushes the updated snapshot to all active listeners:

**Android App:**
```
Firebase RTDB node changes
      │  ValueEventListener fires
      ▼
FirebaseDeviceDataSource: callbackFlow { ref.addValueEventListener(...) }
      ▼
Repository: maps DataSnapshot → Kotlin data classes
      ▼
ViewModel: .stateIn(viewModelScope) → StateFlow<UiState>
      ▼
Composable: collectAsState() → recomposition
```

**Web Simulator:**
```
Firebase RTDB node changes
      │  onValue callback fires
      ▼
SimulatorProvider: parses snapshot → Floor[] and Device[] state arrays
      ▼
React components: re-render via context consumer (useSimulator hook)
```

### 2.3 Write Path (User Action → Cloud → Other Client)

When a user toggles a device in either client:

**Android App writes:**
```kotlin
// FirebaseDeviceDataSource.kt
db.getReference("homes/home_001/devices/$deviceId/state")
  .updateChildren(mapOf(
      "isOn" to true,
      "lastOnAt" to ServerValue.TIMESTAMP,
      "lastChangedBy" to "android"
  ))
```

**Simulator writes:**
```typescript
// simulator-store.tsx
update(ref(db, `homes/${HOME_ID}/devices/${id}/state`), {
  isOn: nextIsOn,
  error: false,
  lastChangedBy: "simulator",
  lastOnAt: serverTimestamp()   // only when turning ON
});
```

**Key decision — no optimistic local updates.** Neither client mutates its own UI state after a write. Instead, both write to Firebase and let the listener push the confirmed value back down. RTDB latency on normal Wi-Fi is ~100–300 ms (imperceptible), and this guarantees the app and simulator can *never* disagree — which is exactly what the "no manual refresh" requirement demands.

### 2.4 Worked Example (Bidirectional Sync)

1. User taps **Light ON** in Android → `updateChildren` on `.../state`.
2. Simulator's `onValue` listener fires → bulb tile turns on. *(forward sync)*
3. User clicks the bulb in the simulator → writes `isOn: false`.
4. Android's `ValueEventListener` fires → `FloorUiState.devices` updates → card recomposes to OFF. *(reverse sync)*
5. The `usageLogger` Cloud Function sees the ON→OFF transition and accumulates today's `activeMinutes` in `usage/`.

### 2.5 Bidirectional Sync — Sequence Diagram

```
 ANDROID APP              FIREBASE RTDB              WEB SIMULATOR
     │                         │                          │
     │  ── User taps ON ──►    │                          │
     │  updateChildren({       │                          │
     │    isOn: true,          │                          │
     │    lastOnAt: TIMESTAMP, │                          │
     │    lastChangedBy:       │                          │
     │      "android"          │                          │
     │  })                     │                          │
     │ ─────────────────────►  │                          │
     │                         │                          │
     │                         │  ── onValue fires ──►    │
     │                         │  ────────────────────►   │
     │                         │                          │
     │                         │              Tile turns GREEN (ON)
     │                         │                          │
     │                         │   ◄── User clicks tile ──│
     │                         │   update({               │
     │                         │     isOn: false,         │
     │                         │     lastChangedBy:       │
     │                         │       "simulator"        │
     │                         │   })                     │
     │                         │  ◄────────────────────   │
     │                         │                          │
     │  ◄── listener fires ──  │                          │
     │  ◄────────────────────  │                          │
     │                         │                          │
     │  Card recomposes OFF    │                          │
     │                         │                          │
     │                         │  ── usageLogger ──►      │
     │                         │  (ON→OFF trigger)        │
     │                         │  increments usage/       │
     │                         │                          │
```

### 2.6 Presence & Disconnection Detection

The simulator uses Firebase's `onDisconnect()` mechanism to manage device presence:

```typescript
// On connection: mark device online
update(stateRef, { online: true, lastChangedBy: "simulator" });

// Register disconnect handler
onDisconnect(stateRef).update({ online: false, lastChangedBy: "simulator" });
```

When the simulator browser tab is closed, Firebase automatically sets `online: false` for all its devices. The Android app's listener picks this up immediately and displays `DISCONNECTED` status — no heartbeat polling required.

### 2.6 Write Contract

Every power-state write, from either client, uses `updateChildren` on `.../state` with:

| Field | Value |
|---|---|
| `isOn` | `true` or `false` |
| `lastOnAt` | `ServerValue.TIMESTAMP` (only when turning ON) |
| `lastChangedBy` | `"android"` or `"simulator"` |

We never use `setValue` on the whole `state` node from a client — that would wipe fields owned by the other client (e.g., the app would erase the simulator's `online` flag).

---

## 3. Floor Representation

### 3.1 Data Model

Floors are stored as a flat map under `homes/{homeId}/floors/`:

```json
{
  "floor_ground": { "name": "Ground Floor", "order": 0, "gridColumns": 4, "gridRows": 4 },
  "floor_first":  { "name": "First Floor",  "order": 1, "gridColumns": 4, "gridRows": 4 }
}
```

| Field | Purpose |
|---|---|
| `name` | Display name shown in both app and simulator |
| `order` | Integer for sorting floor tabs (0 = ground, 1 = first, etc.) |
| `gridColumns` / `gridRows` | Dimensions of the abstract grid overlay (e.g., 4×4 = 16 cells) |

### 3.2 Device-to-Floor Mapping

Devices are stored in a **flat** map under `homes/{homeId}/devices/` — *not* nested under floors. Each device carries a `floorId` field and a `position { x, y }` indicating its grid cell:

```json
{
  "dev_light_living": {
    "name": "Living Room Light",
    "type": "LIGHT",
    "floorId": "floor_ground",
    "room": "Living Room",
    "position": { "x": 0, "y": 0 },
    "state": { ... },
    "config": { ... }
  }
}
```

**Why flat, not nested:** Nesting devices under floors would force downloading entire floors to show one device and complicate moving devices between floors. With the flat structure, we query per floor using `orderByChild("floorId").equalTo(...)` on Android, and filter client-side in the simulator.

### 3.3 Floor Grid — Visual Mapping

```
DATABASE                              ANDROID APP                    SIMULATOR
────────                              ───────────                    ─────────

floors/floor_ground:                  ┌──────────────────┐          ┌──────────────────┐
  gridColumns: 4                      │  Floor Plan Image │          │  Floor Plan Image │
  gridRows: 4                         │  (background)     │          │  (background)     │
                                      │                   │          │                   │
devices:                              │  ┌───┬───┬───┬───┐│          │  ┌───┬───┬───┬───┐│
  dev_light_living:                   │  │💡 │🔌 │   │📷 ││          │  │💡 │🔌 │   │📷 ││
    position: {x:0, y:0}  ─────────► │  ├───┼───┼───┼───┤│  ─────► │  ├───┼───┼───┼───┤│
  dev_outlet_tv:                      │  │🔧 │   │🎛️│   ││          │  │🔧 │   │🎛️│   ││
    position: {x:1, y:0}  ─────────► │  ├───┼───┼───┼───┤│  ─────► │  ├───┼───┼───┼───┤│
  dev_panel_hall:                     │  │   │   │   │   ││          │  │   │   │   │   ││
    position: {x:2, y:0}  ─────────► │  ├───┼───┼───┼───┤│  ─────► │  ├───┼───┼───┼───┤│
  dev_cam_porch:                      │  │   │   │   │💡 ││          │  │   │   │   │💡 ││
    position: {x:3, y:0}  ─────────► │  └───┴───┴───┴───┘│          │  └───┴───┴───┴───┘│
  dev_iron_kitchen:                   └──────────────────┘          └──────────────────┘
    position: {x:0, y:1}               (0,0)  (1,0) (2,0) (3,0)      (0,0)  (1,0) (2,0) (3,0)
                                       (0,1)  (1,1) (2,1) (3,1)      (0,1)  (1,1) (2,1) (3,1)
                                       (0,2)  (1,2) (2,2) (3,2)      (0,2)  (1,2) (2,2) (3,2)
                                       (0,3)  (1,3) (2,3) (3,3)      (0,3)  (1,3) (2,3) (3,3)

  x = column (horizontal position)
  y = row (vertical position)
```

### 3.4 Android Floor Rendering

The Android `FloorScreen` renders a three-layer composition:

1. **Bottom layer** — Floor plan background image (loaded via Coil `AsyncImage` or a default icon).
2. **Middle layer** — Subtle radial gradient overlay for visual depth.
3. **Top layer** — Device nodes positioned using `BoxWithConstraints` + `offset()`:

```kotlin
val cellW = maxWidth / gridColumns
val cellH = maxHeight / gridRows
devices.forEach { device ->
    DeviceNode(
        device = device,
        modifier = Modifier
            .offset(
                x = cellW * device.position.x,
                y = cellH * device.position.y
            )
    )
}
```

Each device is rendered as a circular icon with a name label, color-coded by status (ON = amber, ERROR = red, DISCONNECTED = grey, OFF = neutral).

### 3.4 Simulator Floor Rendering

The simulator's `FloorGrid` component renders a CSS Grid overlay on top of a floor plan image:

```tsx
<div className="relative">
  <img src={floor.planImage} className="absolute inset-0 opacity-25" />
  <div className="relative grid gap-2 p-3"
       style={{ gridTemplateColumns: `repeat(${floor.cols}, minmax(0, 1fr))` }}>
    {cells.map(({ row, col }) => {
      const device = floorDevices.find(d => d.row === row && d.col === col);
      return device ? <DeviceTile device={device} /> : <AddButton />;
    })}
  </div>
</div>
```

Empty grid cells show a "+" button that opens the Add Device dialog. Occupied cells show a `DeviceTile` with status-colored ring, icon, name, and status badge.

### 3.5 Floor Management

Both clients support adding and removing floors:

- **Android:** FAB on the Home screen opens an Add Floor dialog; writes to `floors/{newId}`.
- **Simulator:** Sidebar "+" button opens Add Floor dialog; writes grid dimensions and name.
- Floor switching is done via a dropdown (Android) or sidebar selection (Simulator).

---

## 4. Simulator Operations

### 4.1 Architecture

The hardware simulator is a **React + TypeScript** web application built with Vite and TanStack Router. It uses the Firebase Web SDK for real-time database communication.

```
hardware-simulator/
├── src/
│   ├── lib/
│   │   ├── firebase.ts              # Firebase config + SDK exports
│   │   ├── simulator-types.ts       # TypeScript interfaces (Device, Floor, etc.)
│   │   └── simulator-store.tsx      # React Context: all Firebase I/O + state management
│   ├── components/simulator/
│   │   ├── FloorGrid.tsx            # Grid overlay with device tiles
│   │   ├── DeviceTile.tsx           # Individual device card
│   │   ├── DeviceDetailDialog.tsx   # Expanded device inspector
│   │   ├── FloorSidebar.tsx         # Floor list + selection
│   │   ├── StatusBar.tsx            # Connection status indicator
│   │   ├── EventLog.tsx             # Real-time event log panel
│   │   ├── AddFloorDialog.tsx       # Floor creation form
│   │   └── AddDeviceDialog.tsx      # Device provisioning form
│   └── routes/index.tsx             # Main dashboard page
```

### 4.2 Real-Time Subscription

On startup, the simulator authenticates anonymously and attaches a single `onValue` listener to the entire home node:

```typescript
const homeRef = ref(db, `homes/${HOME_ID}`);
onValue(homeRef, (snapshot) => {
  const val = snapshot.val();
  // Parse floors, devices, schedules from the snapshot
  // Update React state → triggers re-render of affected components
});
```

This single listener receives the complete home state on every change — floors, devices, schedules, usage, and alerts. The simulator parses this into typed `Floor[]` and `Device[]` arrays and distributes them via React Context.

### 4.3 Device Control Operations

| Operation | How it works |
|---|---|
| **Toggle ON/OFF** | Click a tile → `update(ref(db, '.../state'), { isOn, lastChangedBy: 'simulator' })`. The Android app updates instantly via its own listener. |
| **Multi-switch control** | Open detail dialog → toggle individual gang switches → writes `switches/{key}` + derived `isOn` (true if any gang is on). |
| **Fault simulation** | Detail dialog "Fault" button → writes `state.error = true`. Both clients display `ERROR` status. Toggling the device clears the fault. |
| **Presence (online/offline)** | On connect: `update(state, { online: true })` + `onDisconnect().update({ online: false })`. Closing the browser tab visibly disconnects devices in the app. |
| **Camera snapshot refresh** | Appends cache-busting query param: `snapshotUrl?t=<timestamp>`. Android's Coil image loader fetches the new image. |
| **Hazard auto-cutoff** | Client-side watchdog checks every 1s: if `elapsed >= maxOnDurationMin * 60`, writes `isOn: false`. (The Cloud Function `safetyMonitor` is the authoritative enforcer.) |
| **Add/Remove devices** | Dialog forms write full device payloads to `devices/{newId}` or call `remove()` on existing refs. |
| **Add/Remove floors** | Writes to `floors/{newId}` with grid dimensions; `remove()` to delete. |

### 4.4 Differential Event Logging

The simulator maintains a rolling event log (last 200 entries) by diffing consecutive snapshots:

```typescript
if (prevState.isOn !== nextState.isOn) {
  log("info", device.name, `Power turned ${nextState.isOn ? "ON" : "OFF"}`);
}
if (prevState.error !== nextState.error) {
  log(nextState.error ? "error" : "info", device.name, "Fault enabled/cleared");
}
if (prevState.online !== nextState.online) {
  log(nextState.online ? "info" : "warn", device.name, "Went online/offline");
}
```

This log shows changes originating from *any* source (Android app, Cloud Functions, or the simulator itself), making it a useful debugging and demonstration tool.

### 4.5 Device Type Handling

| Device Type | Simulator Behavior |
|---|---|
| **Outlet** | Simple ON/OFF toggle on tile click |
| **Light (Scheduled)** | ON/OFF toggle; schedule managed via Cloud Function |
| **Switch Panel** | Opens detail dialog with individual gang toggles (e.g., "Ceiling Fan", "Main Light", "Wall Lamp") |
| **Iron (Hazard)** | Opens detail dialog with toggle + live countdown timer; client-side auto-cutoff watchdog |
| **Camera** | Opens detail dialog showing snapshot image, stream URI, refresh button |
| **Lock** | ON/OFF toggle representing locked/unlocked state |
| **Thermostat** | Detail dialog with target temperature control |

---

## 5. Firebase Data Structure

Our data is structured under one home node — **floors, devices, schedules, usage, and alerts**. Each device has a **state block**, which changes constantly, separated from a **config block**, which rarely changes — so listeners stay cheap and precise.

```
homes/{homeId}/
├── meta/                    name, timezone
├── floors/{floorId}         name, order, gridColumns, gridRows
├── devices/{deviceId}
│   ├── name, type, floorId, room, position{x,y}
│   ├── state/               isOn, switches{}, online, error, streamUrl,
│   │                        snapshotUrl, lastOnAt, lastChangedBy
│   └── config/              maxActiveMinutes, wattage
├── schedules/{scheduleId}   deviceId, onTime, offTime, enabled, lastRunKey
├── usage/{deviceId}/{yyyy-MM-dd}
│                            activeMinutes, sessions, energyWh, autoCutoffs
├── alerts/{alertId}         title, message, severity, deviceId, timestamp, read
└── activities/{activityId}  (recent activity log)

users/{uid}/                 homeId, createdAt
```

### 5.1 State vs Config Separation

| Block | Changes frequency | Contains |
|---|---|---|
| `state/` | Many times per minute | `isOn`, `switches`, `online`, `error`, `lastOnAt`, `lastChangedBy`, camera URLs |
| `config/` | Rarely (user edits) | `maxActiveMinutes` (iron safety limit), `wattage` (energy estimate) |

This separation means a listener on `state/` does not fire when a user edits the safety limit, and vice versa — keeping bandwidth and processing minimal.

### 5.2 Path Ownership — Who Reads and Writes What

```
  DATABASE PATH                    WRITERS                    READERS
  ─────────────                    ───────                    ───────

  meta/                        App (Settings)          All
  floors/{id}                  App, Simulator          App, Simulator
  devices/{id} (identity)      App (Add Device)        App, Simulator
    .../state/isOn             App, Simulator,          App, Simulator,
                               scheduleRunner,          usageLogger
                               safetyMonitor
    .../state/switches/{key}   App, Simulator          App, Simulator
    .../state/online           Simulator only           App, Simulator
                               (+ onDisconnect)
    .../state/error            Simulator only           App, Simulator
                               ("fault" button)
    .../state/lastOnAt         Any client on ON-write   safetyMonitor,
                                                        usageLogger, both UIs
    .../state/lastChangedBy    Every writer             Debug / demo
    .../config/                App (Device Control)     safetyMonitor, both UIs
  schedules/{id}               App (Schedule screen)    scheduleRunner, App
  usage/{dev}/{date}           usageLogger,             App (Reports)
                               safetyMonitor
  alerts/{id}                  safetyMonitor,           App (Alerts screen)
                               App (user events)
```

### 5.3 Key Design Decisions

1. **Flat devices map with `floorId` field** — not nested under floors. Enables efficient per-floor queries.
2. **`lastOnAt` server timestamp** — single value powering the iron countdown UI, safety cutoff logic, and usage duration math. Clients never trust their own clocks.
3. **Usage pre-aggregated per day** by the `usageLogger` Cloud Function — the Reports screen reads one tiny node per device per day instead of scanning an event log.
4. **`lastChangedBy` field** — records the origin of every write (`"android"`, `"simulator"`, `"safetyMonitor"`, `"scheduleRunner"`), visible in both UIs for debugging and demonstration.
5. **FCM topic messaging** (`home_{homeId}`) — no per-device token bookkeeping in the database.

---

## 6. Cloud Functions — Server-Side Automation

Three Cloud Functions handle backend automation that must work even when the app is closed:

### 6.0 Cloud Functions — Trigger Flow Diagram

```
                    FIREBASE REALTIME DATABASE
                    ─────────────────────────
                              │
          ┌───────────────────┼───────────────────┐
          │                   │                   │
          ▼                   ▼                   ▼
  ┌───────────────┐  ┌───────────────┐  ┌───────────────────┐
  │  usageLogger  │  │ safetyMonitor │  │  scheduleRunner   │
  │               │  │               │  │                   │
  │ TRIGGER:      │  │ TRIGGER:      │  │ TRIGGER:          │
  │ onUpdate      │  │ onUpdate      │  │ onWrite           │
  │ .../state/isOn│  │ .../state     │  │ .../schedules/    │
  │               │  │               │  │   {scheduleId}    │
  │ ON true→false │  │ IRON + isOn   │  │                   │
  │   ↓           │  │   ↓           │  │   ↓               │
  │ compute mins  │  │ wait until    │  │ wait until        │
  │ from lastOnAt │  │ cutoff time   │  │ onTime / offTime  │
  │   ↓           │  │   ↓           │  │   ↓               │
  │ WRITE:        │  │ WRITE:        │  │ WRITE:            │
  │ usage/{dev}/  │  │ state.isOn    │  │ state.isOn        │
  │   {date}      │  │   = false     │  │   = true/false    │
  │ +activeMin    │  │ +alert row    │  │ +lastRunKey       │
  │ +sessions     │  │ +autoCutoffs  │  │   (idempotent)    │
  │ +energyWh     │  │ +FCM push     │  │                   │
  └───────────────┘  └───────────────┘  └───────────────────┘
          │                   │                   │
          ▼                   ▼                   ▼
     usage/ node        alerts/ node        devices/ node
     (Reports screen)   (Alerts screen)     (both clients update)
```

### 6.1 `usageLogger` — RTDB Trigger

- **Trigger:** `onUpdate` on `.../state/isOn`
- **Logic:** On `true → false` transition, reads `lastOnAt`, computes `minutes = (now − lastOnAt) / 60000`, reads `config.wattage`, and atomically increments the daily usage record:
  - `activeMinutes += minutes`
  - `sessions += 1`
  - `energyWh += minutes/60 × wattage`
- **Why server-side:** Works no matter which client flipped the switch, and can't be gamed by the app being closed.

### 6.2 `safetyMonitor` — Server-Side Safety Cutoff

- **Trigger:** `onUpdate` on `.../state` (for IRON devices)
- **Logic:** When an iron turns ON, the function calculates the cutoff time (`lastOnAt + maxActiveMinutes × 60000`). It waits in-process (bounded by the 540s gen-1 timeout) and self-kicks via a `safetyPing` field if the wait exceeds one invocation. At cutoff:
  1. Writes `state.isOn = false`, `lastChangedBy = "safetyMonitor"`
  2. Increments `usage/{deviceId}/{date}/autoCutoffs`
  3. Creates a persistent alert in `alerts/`
  4. Sends FCM push notification to topic `home_{homeId}`
- **This is the authoritative enforcement** of the safety requirement — the simulator's client-side countdown is cosmetic.

### 6.3 `scheduleRunner` — Light Automation

- **Trigger:** `onWrite` on `.../schedules/{scheduleId}`
- **Logic:** Reads `onTime`/`offTime`, waits until the next fire time (in the home's timezone), and toggles the device. Uses `lastRunKey` (`"<today>-ON"` / `"<today>-OFF"`) for idempotency — re-running within the same minute can never double-toggle. Self-kicks via a `ping` field for gaps longer than the invocation timeout.

### 6.4 Android-Side Automation (Complementary)

The Android app also runs a `HomeAutomation` service that provides client-side fallback for safety cutoff, usage tracking, and schedule execution. This ensures the system functions even without Cloud Function deployment (e.g., during local development with the Firebase Emulator).

---

## 7. Device Status Model

The assignment requires four statuses: `ON`, `OFF`, `ERROR`, `DISCONNECTED`. We derive them from two booleans + the power flag — we never store a status string:

### 7.1 Status Derivation Flow

```
  device.state
  ┌─────────────────────────────────────────────────────────────────┐
  │                                                                 │
  │  online == false ?  ──── YES ──►  DISCONNECTED  (grey)         │
  │       │                                                       │
  │      NO                                                       │
  │       ▼                                                       │
  │  error == true ?   ──── YES ──►  ERROR         (red)          │
  │       │                                                       │
  │      NO                                                       │
  │       ▼                                                       │
  │  isOn == true ?    ──── YES ──►  ON            (green/amber)  │
  │       │                                                       │
  │      NO                                                       │
  │       ▼                                                       │
  │                     ──────────►  OFF           (neutral)      │
  │                                                                 │
  └─────────────────────────────────────────────────────────────────┘

  WRITERS:
  ┌─────────────────────────────────────────────────────────────────┐
  │  online  →  Simulator only  (on connect / onDisconnect)        │
  │  error   →  Simulator only  ("fault" button in detail dialog)  │
  │  isOn    →  Android, Simulator, safetyMonitor, scheduleRunner  │
  └─────────────────────────────────────────────────────────────────┘
```

| Priority | Condition | Displayed Status | Color |
|---|---|---|---|
| 1 | `state.online == false` | `DISCONNECTED` | Grey |
| 2 | `state.error == true` | `ERROR` | Red |
| 3 | `state.isOn == true` | `ON` | Green / Amber |
| 4 | otherwise | `OFF` | Neutral |

Both clients implement this same priority logic:

**Android** (`DeviceStatus.kt`):
```kotlin
fun Device.status(): DeviceStatus = when {
    !state.online -> DeviceStatus.DISCONNECTED
    state.error   -> DeviceStatus.ERROR
    state.isOn    -> DeviceStatus.ON
    else          -> DeviceStatus.OFF
}
```

**Simulator** (`simulator-store.tsx`):
```typescript
let status: DeviceStatus = "off";
if (state.online === false) status = "disconnected";
else if (state.error === true) status = "error";
else if (state.isOn === true) status = "on";
```

---

## 8. Technology Stack

| Component | Technology |
|---|---|
| **Android App** | Kotlin, Jetpack Compose (Material 3), MVVM + Repository pattern, Hilt (DI), StateFlow, Navigation Compose, Coil |
| **Hardware Simulator** | React 18, TypeScript, Vite, TanStack Router, shadcn/ui, Firebase Web SDK v10+ |
| **Cloud Functions** | Node.js, `firebase-functions` + `firebase-admin`, Firebase Realtime Database triggers |
| **Backend Services** | Firebase Realtime Database (source of truth), Anonymous Authentication, Cloud Messaging (FCM) |
| **Version Control** | Git + GitHub |

---

*End of technical report.*
