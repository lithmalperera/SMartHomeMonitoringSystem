# Hardware Simulator (Web)

A static web app that plays the role of the **physical house**: it renders every device as
an interactive card, listens to Firebase in real time, and writes state changes back — so
the Android app sees the "hardware" respond instantly. No build step, no framework.

> Design details: `../documentation/architecture.md` §11. Data contract:
> `../documentation/database-schema.md` §2 and §6.

## Planned structure

```
hardware-simulator/
├── index.html                  # floor tabs + device grid; loads js/app.js as an ES module
├── css/main.css                # card grid + status colors
├── js/
│   ├── config/firebase-config.js   # Firebase web config + HOME_ID constant
│   ├── services/firebase-service.js# ONLY Firebase-importing file:
│   │                               #   anonymous sign-in, onValue listeners, update() writes,
│   │                               #   presence (.info/connected + onDisconnect())
│   ├── devices/                # base-device.js + light / outlet / switch-panel / iron / camera
│   ├── ui/floor-view.js        # group devices by floor, render grid
│   └── app.js                  # boot: auth → subscribe → render → wire events
└── assets/                     # icons / placeholder images
```

## Setup (when implementation starts — Week 1–2)

1. Firebase console → Project settings → *Your apps* → add a **Web app**, copy the
   `firebaseConfig` object into `js/config/firebase-config.js`.
2. Set `HOME_ID = "home_001"` in the same file (must match the Android app).
3. Firebase Web SDK is imported from the CDN (`https://www.gstatic.com/firebasejs/...`) —
   no npm, no bundler.

## Run

Any static file server from this folder, e.g.:

```bash
npx serve .            # or: python3 -m http.server 8080
```

(Opening `index.html` via `file://` will NOT work — ES modules require HTTP.)

## Behaviour contract

- **Mirror:** one `onValue` listener re-renders cards on every DB change (app → simulator).
- **Actuate:** clicking a card writes `state` via `update()` with
  `lastChangedBy: "simulator"` (simulator → app).
- **Presence:** on connect, sets `state.online = true` and registers
  `onDisconnect()` → `online = false` (drives the app's `DISCONNECTED` status).
- **Fault button:** toggles `state.error` (drives the `ERROR` status).
- **Iron countdown** is cosmetic; real enforcement is the `safetyMonitor` Cloud Function.
