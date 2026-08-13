const functions = require("firebase-functions");
const { getDatabase, ServerValue } = require("firebase-admin/database");
const { notifyHome } = require("./notify");

// Safety listener — "backend cloud listener" per the assignment wording.
// Triggered on every state change of a device. When an iron turns ON it
// starts watching: it waits in-process (max ~8 min per invocation, gen-1
// limit) and self-kicks by writing state/safetyPing to re-trigger itself
// until the cutoff time arrives. Then it re-checks the live state and
// enforces the cutoff. Works on the free Spark plan.
const MAX_WAIT_MS = 8 * 60 * 1000; // < 540s gen-1 timeout, with margin
const CHECK_INTERVAL_MS = 30 * 1000;

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

exports.safetyMonitor = functions.runWith({ timeoutSeconds: 540 })
  .database.ref("/homes/{homeId}/devices/{deviceId}/state")
  .onUpdate(async (change, context) => {
    const { homeId, deviceId } = context.params;
    const db = getDatabase();

    const deviceSnap = await db
      .ref(`homes/${homeId}/devices/${deviceId}`)
      .once("value");
    const device = deviceSnap.val() || {};

    if (device.type !== "IRON") return;

    const limitMin = (device.config && device.config.maxActiveMinutes) || 0;
    if (!limitMin) return;

    const state = device.state || {};
    const isOn = state.isOn === true;
    const lastOnAt = state.lastOnAt || 0;
    if (!isOn || !lastOnAt) return; // only watch while it is ON

    const cutoffAt = lastOnAt + limitMin * 60000;
    const now = Date.now();

    if (now >= cutoffAt) {
      // Enforce: re-check live state first (user may have toggled since).
      const liveSnap = await db
        .ref(`homes/${homeId}/devices/${deviceId}/state`)
        .once("value");
      const liveState = liveSnap.val() || {};
      if (liveState.isOn !== true) return;
      if (liveState.lastOnAt !== lastOnAt) return;

      const deviceName = device.name || deviceId;

      // 1. Flip the database state OFF
      await db.ref(`homes/${homeId}/devices/${deviceId}/state`).update({
        isOn: false,
        lastChangedBy: "safetyMonitor",
      });

      // 2. Count the cutoff in today's usage record
      const metaSnap = await db.ref(`homes/${homeId}/meta`).once("value");
      const meta = metaSnap.val() || {};
      const dateKey = new Intl.DateTimeFormat("en-CA", {
        timeZone: meta.timezone || "UTC",
      }).format(new Date());
      await db
        .ref(`homes/${homeId}/usage/${deviceId}/${dateKey}`)
        .update({
          autoCutoffs: ServerValue.increment(1),
        });

      // 3. Persistent alert row for the app's notification log
      const alertId = `alert_${Date.now()}_${deviceId}`;
      await db.ref(`homes/${homeId}/alerts/${alertId}`).set({
        deviceId,
        title: "Safety cutoff",
        message: `${deviceName} was automatically turned OFF after exceeding the ${limitMin} min safety limit.`,
        timestamp: Date.now(),
        severity: "CRITICAL",
        read: false,
      });

      // 4. Push notification
      await notifyHome(homeId, deviceName, limitMin);

      console.log(
        `safetyMonitor: cut off ${deviceName} (${deviceId}) in ${homeId} after ${limitMin} min`
      );
      return;
    }

    // Wait until the cutoff time (bounded by the invocation timeout).
    const waitMs = Math.min(cutoffAt - now, MAX_WAIT_MS);
    while (Date.now() < now + waitMs) {
      await sleep(Math.min(waitMs, CHECK_INTERVAL_MS));
    }

    // Still not due and still ON — self-kick to continue watching.
    if (Date.now() < cutoffAt) {
      await db.ref(`homes/${homeId}/devices/${deviceId}/state`).update({
        safetyPing: ServerValue.increment(1),
      });
    }
  });
