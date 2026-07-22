package com.smarthome.monitor.data.remote

// data/remote — the ONLY package allowed to import com.google.firebase.database.
// Week 1–2: FirebaseDeviceDataSource, FirebaseFloorDataSource,
// FirebaseScheduleDataSource, FirebaseUsageDataSource. Each exposes callbackFlow
// readers + suspend write functions (write contract: database-schema.md §6).
