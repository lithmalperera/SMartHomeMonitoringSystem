# Development Plan — 4 Weeks, 3 Members

Practical roadmap. Each week ends with a demo-able milestone; if a week slips, the next
week's scope is cut, never the milestone's demo quality.

---

## 1. Team Roles

| Member | Primary ownership | Secondary |
|---|---|---|m.l;pl''''''''''''];;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;//////////////////////
| **A — Android UI** | All screens, shared components, theme, navigation | Camera + reports screens polish |
| **B — Data & Backend** | Android data layer (models, data sources, repos), Firebase setup, Cloud Functions, FCM | Security rules, seed data |
| **C — Simulator & Docs** | Hardware simulator (all of it), documentation upkeep, test/demo data, demo video editing | Manual QA across app |

Integration contract (agreed **Day 2**, before parallel work starts): the repository
interfaces from `architecture.md` §8.3. Member A builds UI against **fake repositories**
while B builds the Firebase implementations — neither blocks the other.

Weekly 30-minute sync (same slot every week). Blockers go in the group chat immediately,
not at the sync.

---

## 2. Week-by-Week Roadmap

### Week 1 — Foundations  → milestone `v0.1-foundations`

| Day | Tasks | Owner |
|---|---|---|
| 1 | Create GitHub repo, branch protection, `.gitignore`, this documentation set | All |
| 1–2 | Firebase project: RTDB, Anonymous Auth, FCM. Import `seed-data.json` from `database-schema.md` | B |
| 2 | Agree repo interfaces + model field lists; freeze them | All |
| 2–4 | Generate Android project (see `android-app/README.md`); Hilt, NavGraph with 8 placeholder screens, theme | A |
| 3–5 | Simulator: firebase-config, anonymous sign-in, live JSON dump of `/devices` on the page | C |
| 4–7 | Android data layer: models + `AuthRepository` + one working `DeviceRepository.observeAllDevices()` | B |

**Milestone M1 demo:** changing a value in the Firebase console appears live in both the
Android placeholder screen and the simulator page.

### Week 2 — Core Bidirectional Sync → milestone `v0.2-sync`

| Tasks | Owner |
|---|---|
| Home dashboard (floor list) + Floor screen (grid + device cards + status badges) | A |
| Device control bottom sheet for all 5 device types (incl. N-gang switch panel) | A |
| Remaining repositories + data sources wired to real Firebase | B |
| Simulator: card UI per device type, click-to-toggle, presence (`online`/`onDisconnect`), "⚠ fault" button | C |
| Add-floor / add-device dialogs (writes `floors/`, `devices/`) | A + B |

**Milestone M2 demo:** toggle a light on the phone → simulator flips instantly; toggle an
outlet in the simulator → phone flips instantly. Close the simulator tab → device shows
`DISCONNECTED` in the app. This is the heart of the assignment.

### Week 3 — Automation & Notifications → milestone `v0.3-automation`

| Tasks | Owner |
|---|---|
| `usageLogger` function (emulator first, then deploy) | B |
| `safetyMonitor` scheduled function + FCM topic publish | B |
| `scheduleRunner` scheduled function with `lastRunKey` idempotency | B |
| Android: FCM service, topic subscription, notification channel | B |
| Schedule screen (times + enable) | A |
| Camera screen (Coil snapshot, stream URL, status) | A |
| Iron UI: live "ON for X / limit Y min" + limit editor | A |
| Simulator: iron countdown display, camera card | C |

**Milestone M3 demo:** set iron limit to 2 min, turn it ON from the phone, walk away — it
turns itself OFF and the phone gets a push notification. Set a light schedule 2 minutes in
the future and watch it fire.

### Week 4 — Reports, Polish, Submission → milestone `v0.4-submission`

| Tasks | Owner |
|---|---|
| Reports screen: date selector, per-device active time/sessions/Wh, Canvas bar chart | A |
| Loading / empty / error states everywhere; disabled controls when `DISCONNECTED` | A |
| Simulator final styling pass (presentable in the video) | C |
| Technical report assembled from `documentation/` | C (all review) |
| Release APK build; GitHub release with APK attached; link in README | B |
| Record demo video per `demo-script.md`; re-record weak sections | All |
| Buffer for bugs (there will be bugs) | All |

---

## 3. Git Workflow

### Branches

```
main        ── protected; always demo-able; only merges from develop at milestones
develop     ── integration branch; all feature work lands here via PR
feature/*   ── one branch per task, branched from develop
```

Naming: `feature/floor-grid-ui`, `feature/device-repository`, `feature/simulator-cards`,
`fix/iron-countdown`, `docs/week2-report`.

### Flow

1. `git checkout develop && git pull` → branch `feature/<area>-<desc>`.
2. Small commits, push daily (even unfinished — it's a backup).
3. Open PR → **develop**. One teammate reviews (15 min, checklist below). No self-merge
   except trivial docs.
4. Merge (squash for messy histories). Delete the branch.
5. At each milestone: PR `develop` → `main`, tag it (`v0.2-sync`, …).

### Commit convention (Conventional Commits)

```
feat: floor grid with device cards
fix: switch panel toggles wrong gang key
docs: add database schema
chore: firebase boM bump
refactor: extract StatusBadge component
```

Imperative mood, ≤ 72 char subject, body only when the *why* isn't obvious.

### PR review checklist (keep it light)

- [ ] Builds and runs; no crashes on the reviewer’s device/emulator
- [ ] Layer rules respected (no Firebase imports outside `data/remote`)
- [ ] No hardcoded `home_001` outside the single constant
- [ ] Writes follow the §6 write contract in `database-schema.md`

### Rules

- Never commit directly to `main` or `develop`.
- Never commit `node_modules/`, `build/`, `local.properties`, `serviceAccount*.json`
  (already in `.gitignore`). `google-services.json` **is** committed — deliberate choice
  for a private coursework repo.
- Resolve merge conflicts the same day they appear; long-lived branches are the enemy.

---

## 4. Deliverables Mapping

| Assignment deliverable | Where it lives |
|---|---|
| Source code + Git hosting + APK link | This repo; APK attached to the `v0.4-submission` GitHub Release, link in root `README.md` |
| Technical documentation (sync mechanism, floor representation, simulator operations) | `documentation/architecture.md` §6, §7, §11 + `database-schema.md` |
| ≤ 25 min demo video, all members presenting | Planned in `documentation/demo-script.md` |

---

## 5. Definition of Done (per feature)

- Works on emulator **and** at least one physical phone.
- State change visible in the simulator (and vice versa) without refresh.
- Correct status badge (`ON/OFF/ERROR/DISCONNECTED`) in all four conditions.
- Merged via reviewed PR; milestone demo still passes.
