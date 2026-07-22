# Cloud Functions

Backend automation for the smart home: safety cutoffs, light scheduling, usage accounting,
and push alerts. Node.js 20, plain JavaScript.

> Full design (triggers, logic, idempotency): `../documentation/architecture.md` §10.

## Planned functions

| Function | Trigger | Purpose |
|---|---|---|
| `usageLogger` | RTDB `onUpdate` on `…/state/isOn` | On ON→OFF transitions, accumulate `activeMinutes`, `sessions`, `energyWh` into `usage/{deviceId}/{yyyy-MM-dd}` |
| `safetyMonitor` | Scheduled (every 1 min) | Auto-OFF any iron past `config.maxActiveMinutes`; FCM alert to topic `home_{homeId}` |
| `scheduleRunner` | Scheduled (every 1 min) | Fire enabled light schedules at `onTime`/`offTime` in the home timezone; `lastRunKey` prevents double-firing |

## Setup (when implementation starts — Week 3)

```bash
npm install -g firebase-tools
firebase login
firebase init functions     # existing project → JavaScript → Node 20 → run in THIS folder
```

Directory layout after init:

```
cloud-functions/
├── firebase.json           # also holds RTDB rules + emulator config
└── functions/
    ├── package.json
    ├── index.js            # exports the three functions
    └── src/                # safety.js, scheduling.js, usage.js, notify.js
```

## Develop & deploy

```bash
firebase emulators:start                 # local RTDB + functions, no deploy needed
firebase deploy --only functions         # requires Blaze plan (free-tier quotas are enough)
firebase deploy --only database          # security rules from database-schema.md §5
```

**Billing note:** scheduled functions require the Blaze (pay-as-you-go) plan. Expected cost
is $0.00 within free quotas — set a budget alert anyway and note it in the report.

**Timezone:** all schedule/safety logic uses `homes/home_001/meta/timezone`
(`Asia/Colombo` in seed data), never the server's clock.
