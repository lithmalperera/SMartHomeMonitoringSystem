# Demo Video Script — ≤ 25 Minutes

Assignment rules: all three members must appear and present, with introductions and
individual contributions. Target runtime **20–22 minutes** — never record against the
25:00 limit; leave headroom.

---

## 1. Setup Before Recording (checklist)

- [ ] Fresh seed data imported (`database-schema.md` §1) — predictable starting state
- [ ] Iron `maxActiveMinutes` set to **2** (short enough to demo the cutoff live)
- [ ] A schedule for Garden Light prepared but disabled; during recording set its ON time
      to ~2 minutes in the future and enable it
- [ ] Phone mirrored to the recording machine (`scrcpy`), simulator in a browser window —
      record both side by side (OBS or any screen recorder with two sources)
- [ ] Do Not Disturb ON everywhere **except** the test phone's app notifications
- [ ] Second phone/emulator optional but nice for the notification shot
- [ ] Architecture diagram (`architecture.md` §1) exported as an image for section 2
- [ ] One rehearsal run end-to-end; note anything flaky and re-seed before the real take

## 2. Script & Timing

| # | Section | Presenter | Time | Content |
|---|---|---|---|---|
| 1 | Introductions | All | 0:00–2:30 | Each member: name, index number, one sentence on owned components |
| 2 | Problem & architecture | B | 2:30–5:30 | Walk the diagram: app ↔ RTDB ↔ simulator, functions, FCM. Emphasize: **Firebase is the single source of truth; no client-to-client communication** |
| 3 | App tour | A | 5:30–8:00 | Floors, grid layout, device cards, the four status badges (point at an `ERROR` device) |
| 4 | Forward sync | A + C | 8:00–9:30 | Toggle Living Room Light on the phone → simulator card flips live. Explain the write path in one sentence |
| 5 | Reverse sync | C | 9:30–11:00 | Toggle TV Outlet in the simulator → phone updates. Then close the simulator tab → device shows `DISCONNECTED` in the app (presence demo) |
| 6 | Multi-switch panel | A | 11:00–12:00 | Toggle individual gangs from the app; show the map structure in the Firebase console |
| 7 | Safety cutoff | B | 12:00–15:00 | Show iron limit = 2 min. Turn iron ON from the phone. Talk through `lastOnAt` and `safetyMonitor` while waiting. Device auto-OFFs + push notification arrives. **This is the highest-mark moment — don't rush it** |
| 8 | Scheduling | B | 15:00–17:00 | Enable the Garden Light schedule (ON time ≈ now+1 min). Show `scheduleRunner` + `lastRunKey` concept. Light turns itself ON on camera |
| 9 | Camera & reports | A | 17:00–19:00 | Camera screen (snapshot, mock stream URL, status). Reports: today's active minutes, sessions, Wh — explain `usageLogger` pre-aggregation |
| 10 | Code highlights | B, A, C | 19:00–22:30 | B: one data source `callbackFlow` + repository mapping. A: one screen's `UiState` + `collectAsStateWithLifecycle`. C: simulator's `firebase-service.js` listener + write |
| 11 | Challenges & close | All | 22:30–end | One real challenge + lesson per member. Thank-yous, end card with repo URL |

## 3. Recording Tips

- Record each section separately and stitch — nobody needs a 22-minute single take.
- If a live demo step fails on camera, keep talking, re-seed, redo that section only.
- Narrate *causes*, not just clicks: "I tapped ON → the app wrote to `/state/isOn` → the
  simulator's listener fired."
- Keep the Firebase console open in a tab; showing the raw JSON flip during sync demos is
  very convincing for examiners.

## 4. After Recording

- [ ] Trim dead air; verify total ≤ 25:00 with margin
- [ ] Upload (unlisted YouTube or Drive), test the link in an incognito window
- [ ] Add video link + APK link to root `README.md` before submission
