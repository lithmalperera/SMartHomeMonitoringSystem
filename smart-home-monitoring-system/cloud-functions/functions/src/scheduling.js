const functions = require("firebase-functions");
const { getDatabase, ServerValue } = require("firebase-admin/database");

// Schedule listener — "backend cloud listener" for scheduled lights.
// Triggered on every change to a schedule node; waits until the next
// onTime/offTime (in the home timezone), fires it, and self-kicks by
// writing a ping field so the function is re-triggered — this bridges gaps
// longer than the gen-1 invocation timeout (e.g. overnight 18:00-06:00).
const MAX_HOP_MS = 8 * 60 * 1000; // < 540s gen-1 timeout, with margin
const FIRE_GRACE_MS = 90 * 1000; // fire window around the due time

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

function pad2(n) {
  return String(n).padStart(2, "0");
}

function timeKey(timeZone) {
  return new Intl.DateTimeFormat("en-GB", {
    timeZone,
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  }).format(new Date());
}

function dateKey(timeZone) {
  return new Intl.DateTimeFormat("en-CA", { timeZone }).format(new Date());
}

// Next onTime/offTime strictly after "now", expressed as a Date whose
// getHours()/getMinutes() are the home timezone's wall-clock values.
function nextFireTime(timeZone, onTime, offTime) {
  const now = new Date();
  const [onH, onM] = onTime.split(":").map(Number);
  const [offH, offM] = offTime.split(":").map(Number);
  const events = [
    { h: onH, m: onM },
    { h: offH, m: offM },
  ];

  const fmt = new Intl.DateTimeFormat("en-CA", {
    timeZone,
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  });

  const parse = (offsetDays, h, m) => {
    const d = new Date(now.getTime() + offsetDays * 86400000);
    const parts = fmt.formatToParts(d);
    const get = (type) => parts.find((p) => p.type === type)?.value;
    const wall = `${get("year")}-${get("month")}-${get("day")}T${pad2(h)}:${pad2(m)}:00`;
    return new Date(wall);
  };

  const future = [];
  for (let offset = 0; offset <= 1; offset++) {
    events.forEach((e) => {
      const candidate = parse(offset, e.h, e.m);
      if (candidate.getTime() > now.getTime()) future.push(candidate);
    });
  }
  return future.sort((a, b) => a.getTime() - b.getTime())[0];
}

exports.scheduleRunner = functions.runWith({ timeoutSeconds: 540 })
  .database.ref("/homes/{homeId}/schedules/{scheduleId}")
  .onWrite(async (change, context) => {
    const { homeId, scheduleId } = context.params;
    const db = getDatabase();

    const schedSnap = await db
      .ref(`homes/${homeId}/schedules/${scheduleId}`)
      .once("value");
    const sched = schedSnap.val();
    if (!sched || !sched.enabled) return;

    const deviceId = sched.deviceId;
    const onTime = sched.onTime;
    const offTime = sched.offTime;
    if (!deviceId || !onTime || !offTime) return;

    const metaSnap = await db.ref(`homes/${homeId}/meta`).once("value");
    const meta = metaSnap.val() || {};
    const timeZone = meta.timezone || "UTC";

    console.log(`scheduleRunner: watching ${scheduleId} (${onTime} / ${offTime})`);

    for (;;) {
      const due = nextFireTime(timeZone, onTime, offTime);
      const delay = due.getTime() - Date.now();

      if (delay > MAX_HOP_MS) {
        // Gap too long for one invocation — sleep the max hop, then ping
        // ourselves to re-trigger and continue waiting.
        await sleep(MAX_HOP_MS);
        await db.ref(`homes/${homeId}/schedules/${scheduleId}`).update({
          ping: ServerValue.increment(1),
        });
        return;
      }

      if (delay > FIRE_GRACE_MS) {
        await sleep(delay - FIRE_GRACE_MS);
        continue; // re-evaluate with the fresh "now"
      }

      // Due — fire exactly once per day per direction (idempotent via lastRunKey).
      const freshSnap = await db
        .ref(`homes/${homeId}/schedules/${scheduleId}`)
        .once("value");
      const fresh = freshSnap.val();
      if (!fresh || !fresh.enabled) return;
      const today = dateKey(timeZone);
      const dueMinutes = due.getHours() * 60 + due.getMinutes();
      const [onH, onM] = onTime.split(":").map(Number);
      const [offH, offM] = offTime.split(":").map(Number);

      if (dueMinutes === onH * 60 + onM) {
        const onKey = `${today}-ON`;
        if (fresh.lastRunKey !== onKey) {
          await db.ref(`homes/${homeId}/devices/${deviceId}/state`).update({
            isOn: true,
            lastOnAt: Date.now(),
            lastChangedBy: "scheduleRunner",
          });
          await db.ref(`homes/${homeId}/schedules/${scheduleId}`).update({
            lastRunKey: onKey,
          });
          console.log(`scheduleRunner: ${deviceId} ON at ${timeKey(timeZone)}`);
        }
      } else if (dueMinutes === offH * 60 + offM) {
        const offKey = `${today}-OFF`;
        if (fresh.lastRunKey !== offKey) {
          await db.ref(`homes/${homeId}/devices/${deviceId}/state`).update({
            isOn: false,
            lastChangedBy: "scheduleRunner",
          });
          await db.ref(`homes/${homeId}/schedules/${scheduleId}`).update({
            lastRunKey: offKey,
          });
          console.log(`scheduleRunner: ${deviceId} OFF at ${timeKey(timeZone)}`);
        }
      }

      // Loop continues: wait for the next event (long gaps self-kick via ping).
    }
  });
