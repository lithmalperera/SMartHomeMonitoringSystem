// The ONLY file allowed to import Firebase (implemented in Week 1–2).
// Responsibilities (see documentation/architecture.md §11.2 and database-schema.md §6):
//   - signInAnonymously(auth)
//   - onValue listener on /homes/{HOME_ID}  → callback to UI layer
//   - update(ref(db, 'devices/{id}/state'), { isOn, lastOnAt: serverTimestamp(),
//     lastChangedBy: 'simulator' })  → actuation writes
//   - presence: on .info/connected → set state.online = true and register
//     onDisconnect() → state.online = false   (drives DISCONNECTED in the app)
//   - fault simulation: toggle state.error    (drives ERROR in the app)
