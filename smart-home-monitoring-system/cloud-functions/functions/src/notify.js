const { getMessaging } = require("firebase-admin/messaging");

// Publishes a safety-cutoff alert to the home's FCM topic.
// The Android app subscribes to topic `home_{homeId}` after sign-in.
async function notifyHome(homeId, deviceName, limitMin) {
  try {
    await getMessaging().send({
      topic: `home_${homeId}`,
      notification: {
        title: "Safety cutoff",
        body: `${deviceName} was automatically turned OFF after ${limitMin} min`,
      },
    });
  } catch (error) {
    // A missing/expired topic must not break the cutoff itself.
    console.error("FCM send failed:", error.message);
  }
}

module.exports = { notifyHome };
