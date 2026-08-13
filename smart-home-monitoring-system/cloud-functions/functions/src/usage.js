const functions = require("firebase-functions");
const { getDatabase, ServerValue } = require("firebase-admin/database");

function dateKey(timeZone) {
  return new Intl.DateTimeFormat("en-CA", { timeZone }).format(new Date());
}

// usageLogger — accumulates per-device daily usage on every ON -> OFF transition.
// Gen-1 database trigger: works on the free Spark plan.
exports.usageLogger = functions.database
  .ref("/homes/{homeId}/devices/{deviceId}/state/isOn")
  .onUpdate(async (change, context) => {
    const before = change.before.val();
    const after = change.after.val();

    // Only act on a true -> false transition.
    if (before !== true || after !== false) return;

    const { homeId, deviceId } = context.params;
    const db = getDatabase();

    const stateSnap = await db
      .ref(`homes/${homeId}/devices/${deviceId}/state`)
      .once("value");
    const state = stateSnap.val() || {};
    const lastOnAt = state.lastOnAt || 0;
    if (!lastOnAt) return; // no record of when it turned on — nothing to count

    const minutes = Math.floor((Date.now() - lastOnAt) / 60000);
    if (minutes <= 0) return;

    const configSnap = await db
      .ref(`homes/${homeId}/devices/${deviceId}/config`)
      .once("value");
    const config = configSnap.val() || {};
    const wattage = config.wattage || 0;
    const energyWh = (minutes / 60) * wattage;

    const metaSnap = await db.ref(`homes/${homeId}/meta`).once("value");
    const meta = metaSnap.val() || {};

    await db
      .ref(`homes/${homeId}/usage/${deviceId}/${dateKey(meta.timezone || "UTC")}`)
      .update({
        activeMinutes: ServerValue.increment(minutes),
        sessions: ServerValue.increment(1),
        energyWh: ServerValue.increment(energyWh),
      });
  });
