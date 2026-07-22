// Entry point — exports all Cloud Functions. Implementation: Week 3.
// Planned exports (see documentation/architecture.md §10):
//
//   usageLogger    — RTDB onUpdate trigger on …/state/isOn:
//                    accumulates activeMinutes / sessions / energyWh into
//                    usage/{deviceId}/{yyyy-MM-dd} on ON→OFF transitions.
//
//   safetyMonitor  — scheduled (every 1 min): auto-OFFs irons that exceed
//                    config.maxActiveMinutes; FCM alert to topic home_{homeId}.
//
//   scheduleRunner — scheduled (every 1 min): fires enabled light schedules at
//                    onTime/offTime in homes/{id}/meta/timezone; idempotent
//                    via schedules/{id}.lastRunKey.
//
// const { usageLogger } = require("./src/usage");
// const { safetyMonitor } = require("./src/safety");
// const { scheduleRunner } = require("./src/scheduling");
// module.exports = { usageLogger, safetyMonitor, scheduleRunner };
