// scheduleRunner — scheduled function (every 1 min). Implementation: Week 3.
// For each enabled schedule: compare current HH:mm in homes/{id}/meta/timezone with
// onTime/offTime. Fire only if lastRunKey != "<today>-ON" / "<today>-OFF", then set the
// device state and update lastRunKey (idempotency — no double-toggle within the minute).
// See documentation/architecture.md §10.3.
