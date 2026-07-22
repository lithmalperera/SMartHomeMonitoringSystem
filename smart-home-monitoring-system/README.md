# Smart Home Monitoring & Control System

**Course:** SCS 3311 – Mobile Application Design & Development (Mini-Project)
**Type:** Group project — 3 members

An IoT-style smart home system: an **Android app** (Kotlin + Jetpack Compose, MVVM) and a
**web-based hardware simulator**, connected in real time through **Firebase**, with
server-side safety automation (Cloud Functions) and push alerts (FCM).

---

## 1. Problem Specification (Summary)

Build a mobile Smart Home Monitoring and Control system consisting of a mobile application
client and a companion cloud-connected hardware simulation system. Users interact with
multiple floor plans, monitor and toggle equipment with differing specialized capabilities,
and automated backend-driven safety rules protect life and property.

### Core Functional Requirements

| Area | Requirement |
|---|---|
| Multi-floor dashboard | Add/manage multiple floor plans; simple abstract grid mapping overlaid on floor layouts; tap devices to control them |
| Device control UI | Toggles reactively update the UI; devices display status: `ON`, `OFF`, `ERROR`, `DISCONNECTED` |
| Electrical outlets | Simple single-node binary power supply |
| Multi-switch units | One physical gang-box managing a variable number (2/3/5…) of individually addressable switches, mapped to a single entity |
| Safety-critical slots | E.g. clothing iron: configurable **maximum permissible active duration**; backend auto-OFF + alert on breach |
| Lighting | Manual ON/OFF + automatic scheduled ON/OFF during preset time periods |
| Security cameras | Mock camera snapshots / mock URI streams + connection status |
| Online synchronization | Bidirectional sync: app → cloud → simulator and simulator → cloud → app, **no manual refresh** |
| Server-side safety cutoff | Backend cloud listener/worker flips state to OFF when `max_on_duration` is breached and pushes an alert |
| Reporting | Usage data of important devices viewable in the app |
| Hardware simulator | Web-based dashboard representing the physical appliances, listening to DB updates and reflecting changes live |

### Assessment Deliverables

- [ ] Source code hosted on GitHub + link to final APK
- [ ] Technical documentation: synchronization mechanism, floor representation, simulator operations (see `documentation/`)
- [ ] Recorded demo video, ≤ 25 minutes, all three members presenting with introductions and individual contributions (see `documentation/demo-script.md`)

---

## 2. Tech Stack

| Component | Technology |
|---|---|
| Android app | Kotlin, Jetpack Compose (Material 3), MVVM + Repository, Hilt, StateFlow, Navigation Compose, Coil |
| Backend | Firebase Realtime Database, Firebase Authentication (Anonymous), Cloud Functions (Node.js 20), Cloud Messaging |
| Hardware simulator | HTML + CSS + vanilla JavaScript (ES modules), Firebase Web SDK via CDN — no build step |
| Version control | Git + GitHub (branching model in `documentation/development-plan.md`) |

---

## 3. Repository Structure

```
smart-home-monitoring-system/           (this repository)
│
├── android-app/                        # Android Studio project (open this folder in Android Studio)
│   └── README.md                       # How to generate/set up the project + target package structure
│
├── hardware-simulator/                 # Web-based hardware simulator (static site, no build step)
│   ├── index.html                      # (to be created in Phase 4)
│   ├── css/
│   ├── js/
│   │   ├── config/                     # Firebase web config
│   │   ├── services/                   # Firebase read/write layer
│   │   ├── devices/                    # One JS module per device type
│   │   └── ui/                         # Floor/grid rendering
│   ├── assets/
│   └── README.md
│
├── cloud-functions/                    # Firebase Cloud Functions project
│   └── functions/
│       └── src/                        # safetyMonitor, scheduleRunner, usageLogger
│
├── documentation/                      # All technical documentation (deliverable #2)
│   ├── architecture.md                 # Complete system architecture & design
│   ├── database-schema.md              # RTDB JSON tree, ownership, security rules
│   ├── development-plan.md             # 4-week roadmap, team roles, Git workflow
│   └── demo-script.md                  # Plan for the ≤25 min demo video
│
├── .gitignore
└── README.md                           # This file
```

---

## 4. Getting Started

Each component is set up independently — see its README:

1. **Firebase:** create a project at <https://console.firebase.google.com>, enable Realtime
   Database, Anonymous Authentication, and Cloud Messaging. Seed the DB with the sample tree
   from `documentation/database-schema.md`.
2. **Android app:** see `android-app/README.md` (generate via Android Studio wizard, add
   `google-services.json`).
3. **Simulator:** see `hardware-simulator/README.md` (paste Firebase web config, serve
   statically).
4. **Cloud Functions:** see `cloud-functions/README.md` (requires Firebase CLI; Blaze plan
   with free-tier quotas).

---

## 5. Team & Responsibilities

| Member | Index No. | Primary responsibility |
|---|---|---|
| Member 1 | _TBD_ | Android UI — screens, components, theme |
| Member 2 | _TBD_ | Android data layer + Firebase + Cloud Functions |
| Member 3 | _TBD_ | Hardware simulator + documentation + testing |

(All members contribute across areas; see `documentation/development-plan.md`.)

---

## 6. Project Status

Track milestones in GitHub Projects/Issues. Planned tags:

| Tag | Milestone |
|---|---|
| `v0.1-foundations` | Repo, Firebase, skeletons, first live sync |
| `v0.2-sync` | Full bidirectional device control |
| `v0.3-automation` | Safety cutoff, scheduling, notifications |
| `v1.0-submission` | Reports, polish, APK, documentation, video |
