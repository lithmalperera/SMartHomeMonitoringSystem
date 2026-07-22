# Firebase Realtime Database Schema

Single source of truth for the whole system. Both clients (Android app + simulator) and the
Cloud Functions read/write this tree. See `architecture.md` §9 for the summary and §12 for
the status derivation rule.

**Seed data:** the JSON in §1 is valid and complete — save it as `seed-data.json` and use
Realtime Database console → ⋮ → *Import JSON* to bootstrap the project.

---

## 1. Full JSON Tree (seed data)

```json
{
  "homes": {
    "home_001": {
      "meta": {
        "name": "My Smart Home",
        "timezone": "Asia/Colombo"
      },

      "floors": {
        "floor_ground": { "name": "Ground Floor", "order": 0, "gridColumns": 4, "gridRows": 4 },
        "floor_first":  { "name": "First Floor",  "order": 1, "gridColumns": 4, "gridRows": 4 }
      },

      "devices": {
        "dev_light_living": {
          "name": "Living Room Light",
          "type": "LIGHT",
          "floorId": "floor_ground",
          "room": "Living Room",
          "position": { "x": 0, "y": 0 },
          "state": {
            "isOn": false,
            "online": true,
            "error": false,
            "lastOnAt": 0,
            "lastChangedBy": "android"
          },
          "config": { "wattage": 12, "maxActiveMinutes": 0 }
        },

        "dev_light_garden": {
          "name": "Garden Light",
          "type": "LIGHT",
          "floorId": "floor_ground",
          "room": "Garden",
          "position": { "x": 3, "y": 3 },
          "state": {
            "isOn": false,
            "online": true,
            "error": false,
            "lastOnAt": 0,
            "lastChangedBy": "function"
          },
          "config": { "wattage": 15, "maxActiveMinutes": 0 }
        },

        "dev_outlet_tv": {
          "name": "TV Outlet",
          "type": "OUTLET",
          "floorId": "floor_ground",
          "room": "Living Room",
          "position": { "x": 1, "y": 0 },
          "state": {
            "isOn": true,
            "online": true,
            "error": false,
            "lastOnAt": 1753104000000,
            "lastChangedBy": "simulator"
          },
          "config": { "wattage": 120, "maxActiveMinutes": 0 }
        },

        "dev_iron_kitchen": {
          "name": "Iron",
          "type": "IRON",
          "floorId": "floor_ground",
          "room": "Kitchen",
          "position": { "x": 0, "y": 1 },
          "state": {
            "isOn": false,
            "online": true,
            "error": false,
            "lastOnAt": 0,
            "lastChangedBy": "android"
          },
          "config": { "maxActiveMinutes": 15, "wattage": 1000 }
        },

        "dev_panel_hall": {
          "name": "Hall Switch Panel",
          "type": "SWITCH_PANEL",
          "floorId": "floor_ground",
          "room": "Hall",
          "position": { "x": 2, "y": 0 },
          "state": {
            "switches": { "s1": true, "s2": false, "s3": true },
            "online": true,
            "error": false,
            "lastChangedBy": "simulator"
          },
          "config": { "wattage": 0, "maxActiveMinutes": 0 }
        },

        "dev_cam_porch": {
          "name": "Porch Camera",
          "type": "CAMERA",
          "floorId": "floor_ground",
          "room": "Porch",
          "position": { "x": 3, "y": 0 },
          "state": {
            "online": true,
            "error": false,
            "streamUrl": "https://demo.smarthome.local/stream/porch",
            "snapshotUrl": "https://picsum.photos/seed/porch/640/360",
            "lastChangedBy": "simulator"
          },
          "config": { "wattage": 5, "maxActiveMinutes": 0 }
        }
      },

      "schedules": {
        "sch_light_garden": {
          "deviceId": "dev_light_garden",
          "onTime": "18:00",
          "offTime": "06:00",
          "enabled": true,
          "lastRunKey": ""
        }
      },

      "usage": {
        "dev_iron_kitchen": {
          "2026-07-20": { "activeMinutes": 25, "sessions": 2, "energyWh": 416.7 }
        },
        "dev_light_living": {
          "2026-07-20": { "activeMinutes": 180, "sessions": 3, "energyWh": 36.0 }
        }
      }
    }
  },

  "users": {
    "anonUid_example": {
      "homeId": "home_001",
      "createdAt": 1753000000000
    }
  }
}
```

---

## 2. Path Reference & Ownership

`{homeId}` is a single hardcoded constant (`home_001`) shared by the app, simulator and
functions (defined in `core/util/Constants.kt` and `js/config/firebase-config.js`).

| Path | Written by | Read/listened by | Notes |
|---|---|---|---|
| `homes/{id}/meta` | App (Settings) | All | `timezone` drives the scheduled functions |
| `homes/{id}/floors/{floorId}` | App (Home screen) | App, Simulator | Grid dims live here |
| `homes/{id}/devices/{deviceId}` (identity fields) | App (Add Device) | App, Simulator | `name`, `type`, `floorId`, `room`, `position` |
| `.../state/isOn` | App, Simulator, `scheduleRunner`, `safetyMonitor` | App, Simulator, `usageLogger` | The most-written field in the system |
| `.../state/switches/{key}` | App, Simulator | App, Simulator | Variable-length map → 2/3/5-gang panels |
| `.../state/online` | **Simulator only** (+ `onDisconnect()`) | App, Simulator | Powers `DISCONNECTED` status |
| `.../state/error` | **Simulator only** ("⚠ fault" button) | App, Simulator | Powers `ERROR` status |
| `.../state/lastOnAt` | Any client on ON-write | `safetyMonitor`, `usageLogger`, both UIs | Server timestamp; linchpin of safety + usage |
| `.../state/lastChangedBy` | Every writer | Debug/demo | `"android"` / `"simulator"` / `"function"` |
| `.../state/streamUrl`, `snapshotUrl` | Seed data / Simulator | App (Camera screen) | Mock camera |
| `.../config/maxActiveMinutes` | App (Device Control sheet) | `safetyMonitor`, both UIs | The spec's `max_on_duration` |
| `.../config/wattage` | App / seed data | `usageLogger` | For the energy estimate |
| `homes/{id}/schedules/{scheduleId}` | App (Schedule screen) | `scheduleRunner`, App | One schedule per device is enough |
| `homes/{id}/usage/{deviceId}/{yyyy-MM-dd}` | **`usageLogger` only** | App (Reports) | Pre-aggregated daily totals |
| `users/{uid}` | App on first sign-in | App | Maps anonymous uid → homeId |

**Rule of thumb:** the app owns *structure* (floors, devices, schedules, config), the
simulator owns *physical truth* (`online`, `error`), both own *state*, and functions own
*derived data* (`usage`) and *enforcement writes* (safety/schedule flips).

---

## 3. Device Status Derivation (display rule)

Never store a status string. Derive from fields, in this priority order:

| Priority | Condition | Status shown |
|---|---|---|
| 1 | `state.online == false` | `DISCONNECTED` |
| 2 | `state.error == true` | `ERROR` |
| 3 | `state.isOn == true` | `ON` |
| 4 | otherwise | `OFF` |

Identical logic in Android (`core/util`) and simulator (`base-device.js`).

---

## 4. Design Decisions (be ready to defend these)

1. **Flat `devices` map keyed by id, with `floorId` as a field** — not nested under floors.
   Query per floor with `orderByChild("floorId").equalTo(...)`. Nesting would force
   downloading entire floors to show one device and complicate moving devices.
2. **`state` vs `config` split** — state changes many times/minute, config almost never.
   Separate nodes mean listeners on `state` don't fire when a limit is edited.
3. **`lastOnAt` (server timestamp)** — one value powers the iron countdown UI, the safety
   cutoff, and usage duration math. Clients never trust their own clocks for this.
4. **Usage pre-aggregated per day** — `usageLogger` accumulates at toggle time; the Reports
   screen reads one small node per device per day. No raw event log is ever stored.
5. **`energyWh` computed server-side** in `usageLogger` (`minutes/60 × wattage`) so every
   client displays the same number with zero duplicated math.
6. **FCM via topic** `home_{homeId}` — no FCM token storage in the DB at all.
7. **Unused fields per type stay at defaults** (e.g. `switches` empty for an outlet) —
   the flat model keeps Firebase (de)serialization trivial; see `architecture.md` §8.1.

---

## 5. Security Rules (design sketch)

Goal: any signed-in user (anonymous auth counts) can access the single demo home; nothing
else is world-readable. Deploy via `firebase deploy --only database`.

```json
{
  "rules": {
    "homes": {
      "$homeId": {
        ".read": "auth != null",
        ".write": "auth != null"
      }
    },
    "users": {
      "$uid": {
        ".read": "auth != null && auth.uid == $uid",
        ".write": "auth != null && auth.uid == $uid"
      }
    }
  }
}
```

Deliberately simple: one home, no per-home membership check. For a production system you'd
verify `auth.uid` belongs to `$homeId`; for the demo, `auth != null` plus a hardcoded
`HOME_ID` is sufficient and easy to explain. Start in **locked mode**, never use
`".read": true` public rules.

---

## 6. Write Contract (both clients must follow)

Every power-state write, from either client, uses `updateChildren` on `.../state` with:

```
isOn:           <boolean>
lastOnAt:       ServerValue.TIMESTAMP   (only when turning ON)
lastChangedBy:  "android" | "simulator"
```

Never `setValue` the whole `state` node from a client — that would wipe fields owned by
the other client (e.g. the app would erase the simulator's `online` flag).
