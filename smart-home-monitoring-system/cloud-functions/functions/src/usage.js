// usageLogger — RTDB onUpdate trigger on /homes/{homeId}/devices/{deviceId}/state/isOn.
// Implementation: Week 3.
// On a true→false transition: minutes = (now - state.lastOnAt) / 60000, then atomically
// increment usage/{deviceId}/{yyyy-MM-dd}: activeMinutes += minutes, sessions += 1,
// energyWh += minutes / 60 * config.wattage. See documentation/architecture.md §10.1.
