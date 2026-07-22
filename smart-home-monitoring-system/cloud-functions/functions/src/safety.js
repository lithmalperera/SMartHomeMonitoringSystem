// safetyMonitor — scheduled function (every 1 min). Implementation: Week 3.
// Query devices where type == "IRON" and state/isOn == true; if
// now - state.lastOnAt > config.maxActiveMinutes * 60000:
//   1. write state.isOn = false, state.lastChangedBy = "safetyMonitor"
//   2. FCM to topic home_{homeId}: "<name> was automatically turned OFF after <limit> min"
// See documentation/architecture.md §10.2.
