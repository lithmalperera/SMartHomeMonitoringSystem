import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState, type ReactNode } from "react";
import { toast } from "sonner";
import type { Device, DeviceStatus, DeviceType, Floor, LogEntry, LogLevel } from "./simulator-types";
import { DEVICE_LABELS } from "./simulator-types";
import floorPlanA from "@/assets/floor-plan-ground.jpg";
import floorPlanB from "@/assets/floor-plan-upper.jpg";
import {
  db,
  ref,
  onValue,
  update,
  serverTimestamp,
  onDisconnect,
  remove,
  auth,
  signInAnonymously,
  HOME_ID
} from "./firebase";

const uid = () => Math.random().toString(36).slice(2, 10);

const getFloorPlan = (name: string, index: number) => {
  if (name.toLowerCase().includes("ground")) return floorPlanA;
  if (name.toLowerCase().includes("upper") || name.toLowerCase().includes("first") || name.toLowerCase().includes("second")) return floorPlanB;
  return index % 2 === 0 ? floorPlanA : floorPlanB;
};

const getSubSwitchLabel = (deviceName: string, key: string) => {
  const normalizedName = deviceName.toLowerCase();
  if (normalizedName.includes("living room") || normalizedName.includes("hall") || normalizedName.includes("panel")) {
    if (key === "s1") return "Ceiling Fan";
    if (key === "s2") return "Main Light";
    if (key === "s3") return "Wall Lamp";
  }
  return key.toUpperCase().replace("S", "Gang ");
};

const typeMap: Record<string, DeviceType> = {
  LIGHT: "scheduled_light",
  OUTLET: "outlet",
  IRON: "hazard",
  SWITCH_PANEL: "multi_switch",
  CAMERA: "camera",
  LOCK: "lock",
  THERMOSTAT: "thermostat"
};

const reverseTypeMap: Record<DeviceType, string> = {
  scheduled_light: "LIGHT",
  outlet: "OUTLET",
  hazard: "IRON",
  multi_switch: "SWITCH_PANEL",
  camera: "CAMERA",
  lock: "LOCK",
  thermostat: "THERMOSTAT"
};

interface AddFloorInput {
  name: string;
  planImage: string;
  rows: number;
  cols: number;
}

interface AddDeviceInput {
  name: string;
  type: DeviceType;
  gangs?: number;
  maxOnDurationMin?: number;
  scheduleStart?: string;
  scheduleEnd?: string;
  snapshotUrl?: string;
  streamUri?: string;
}

interface SimulatorContextValue {
  floors: Floor[];
  devices: Device[];
  logs: LogEntry[];
  connection: "connected" | "syncing" | "offline";
  selectedFloorId: string | null;
  selectFloor: (id: string) => void;
  addFloor: (input: AddFloorInput) => void;
  addDevice: (floorId: string, cell: { row: number; col: number }, input: AddDeviceInput) => void;
  removeDevice: (id: string) => void;
  toggleDevice: (id: string) => void;
  toggleSubSwitch: (deviceId: string, subId: string) => void;
  setStatus: (id: string, status: DeviceStatus, reason?: string) => void;
  refreshSnapshot: (id: string) => void;
  setTargetTemperature: (id: string, temp: number) => void;
  log: (level: LogLevel, source: string, message: string) => void;
}

const SimulatorContext = createContext<SimulatorContextValue | null>(null);

export function SimulatorProvider({ children }: { children: ReactNode }) {
  const [floors, setFloors] = useState<Floor[]>([]);
  const [devices, setDevices] = useState<Device[]>([]);
  const [logs, setLogs] = useState<LogEntry[]>([]);
  const [connection, setConnection] = useState<"connected" | "syncing" | "offline">("syncing");
  const [selectedFloorId, setSelectedFloorId] = useState<string | null>(null);
  
  const logRef = useRef<(level: LogLevel, source: string, message: string) => void>(() => {});
  const presenceRegisteredRef = useRef<Set<string>>(new Set());
  const prevHomeDataRef = useRef<any>(null);

  const log = useCallback((level: LogLevel, source: string, message: string) => {
    setLogs((prev) => [{ id: uid(), at: Date.now(), level, source, message }, ...prev].slice(0, 200));
  }, []);
  logRef.current = log;

  // Firebase connection and real-time subscription
  useEffect(() => {
    setConnection("syncing");
    
    // 1. Sign in anonymously
    signInAnonymously(auth)
      .then((userCredential) => {
        logRef.current("info", "gateway", `Authorized anonymously: ${userCredential.user.uid}`);
        
        // 2. Subscribe to home data
        const homeRef = ref(db, `homes/${HOME_ID}`);
        const unsubscribe = onValue(homeRef, (snapshot) => {
          const val = snapshot.val();
          if (!val) {
            setConnection("offline");
            logRef.current("error", "database", `No data found at homes/${HOME_ID}. Please seed database.`);
            return;
          }
          
          setConnection("connected");
          
          // Parse floors
          const rawFloors = val.floors || {};
          const parsedFloors: Floor[] = Object.entries(rawFloors)
            .map(([id, f]: [string, any], index) => ({
              id,
              name: f.name || "Floor",
              planImage: getFloorPlan(f.name || "", index),
              rows: f.gridRows || 4,
              cols: f.gridColumns || 4
            }))
            .sort((a, b) => {
              const orderA = rawFloors[a.id].order ?? 0;
              const orderB = rawFloors[b.id].order ?? 0;
              return orderA - orderB;
            });
            
          setFloors(parsedFloors);
          
          // Automatically select first floor if none selected
          setSelectedFloorId((prevSelected) => {
            if (parsedFloors.length > 0) {
              if (!prevSelected || !parsedFloors.some(f => f.id === prevSelected)) {
                return parsedFloors[0].id;
              }
            }
            return prevSelected;
          });
          
          // Parse devices and schedules
          const rawDevices = val.devices || {};
          const rawSchedules = val.schedules || {};
          
          // Setup presence for any new devices
          Object.keys(rawDevices).forEach((deviceId) => {
            if (!presenceRegisteredRef.current.has(deviceId)) {
              presenceRegisteredRef.current.add(deviceId);
              const stateRef = ref(db, `homes/${HOME_ID}/devices/${deviceId}/state`);
              update(stateRef, { online: true, lastChangedBy: "simulator" });
              onDisconnect(stateRef).update({ online: false, lastChangedBy: "simulator" });
            }
          });
          
          const parsedDevices: Device[] = Object.entries(rawDevices).map(([id, d]: [string, any]) => {
            const state = d.state || {};
            const config = d.config || {};
            
            // Determine status based on priorities
            let status: DeviceStatus = "off";
            if (state.online === false) {
              status = "disconnected";
            } else if (state.error === true) {
              status = "error";
            } else if (state.isOn === true) {
              status = "on";
            }
            
            // Map type
            const type = typeMap[d.type] || "outlet";
            
            // Multi-switch details
            const swMap = state.switches || {};
            const subSwitches = Object.entries(swMap)
              .map(([key, val]) => ({
                id: key,
                label: getSubSwitchLabel(d.name || "Device", key),
                on: !!val
              }))
              .sort((a, b) => a.id.localeCompare(b.id));
              
            // Schedule details (for LIGHT/scheduled_light type)
            const schedule = Object.values(rawSchedules).find((s: any) => s.deviceId === id) as any;
            const scheduleStart = schedule?.onTime || "18:30";
            const scheduleEnd = schedule?.offTime || "23:00";
            
            return {
              id,
              floorId: d.floorId,
              name: d.name || "Device",
              type,
              status,
              row: d.position?.y ?? 0,
              col: d.position?.x ?? 0,
              gangs: subSwitches.length || config.gangs || 2,
              subSwitches: subSwitches.length ? subSwitches : undefined,
              maxOnDurationMin: config.maxActiveMinutes || undefined,
              onSince: state.isOn ? (state.lastOnAt || Date.now()) : null,
              scheduleStart,
              scheduleEnd,
              snapshotUrl: state.snapshotUrl,
              streamUri: state.streamUrl,
              targetTemperature: state.targetTemperature
            };
          });
          
          setDevices(parsedDevices);
          
          // Differential Logging of State Changes
          if (prevHomeDataRef.current) {
            const prevDevices = prevHomeDataRef.current.devices || {};
            const nextDevices = val.devices || {};
            
            Object.entries(nextDevices).forEach(([id, d]: [string, any]) => {
              const prevDev = prevDevices[id];
              if (!prevDev) {
                logRef.current("info", d.name || id, "Device added");
                return;
              }
              
              const prevState = prevDev.state || {};
              const nextState = d.state || {};
              
              if (prevState.isOn !== nextState.isOn) {
                logRef.current("info", d.name || id, `Power turned ${nextState.isOn ? "ON" : "OFF"}`);
              }
              if (prevState.error !== nextState.error) {
                logRef.current(nextState.error ? "error" : "info", d.name || id, nextState.error ? "Fault enabled" : "Fault cleared");
              }
              if (prevState.online !== nextState.online) {
                logRef.current(nextState.online ? "info" : "warn", d.name || id, nextState.online ? "Went online" : "Went offline");
              }
              
              // switches diffing for SWITCH_PANEL
              const prevSwitches = prevState.switches || {};
              const nextSwitches = nextState.switches || {};
              Object.keys({ ...prevSwitches, ...nextSwitches }).forEach((sKey) => {
                if (prevSwitches[sKey] !== nextSwitches[sKey]) {
                  logRef.current("info", d.name || id, `${getSubSwitchLabel(d.name || id, sKey)} turned ${nextSwitches[sKey] ? "ON" : "OFF"}`);
                }
              });
              
              if (prevState.snapshotUrl !== nextState.snapshotUrl) {
                logRef.current("info", d.name || id, "Snapshot refreshed");
              }
            });
            
            // Deleted devices
            Object.keys(prevDevices).forEach((id) => {
              if (!nextDevices[id]) {
                logRef.current("warn", prevDevices[id].name || id, "Device removed");
              }
            });
          }
          prevHomeDataRef.current = val;
        }, (error) => {
          setConnection("offline");
          logRef.current("error", "database", `Subscription error: ${error.message}`);
        });
        
        return () => unsubscribe();
      })
      .catch((error) => {
        setConnection("offline");
        logRef.current("error", "auth", `Authentication failed: ${error.message}`);
      });
  }, []);

  // Hazard auto-cutoff watchdog
  useEffect(() => {
    const interval = setInterval(() => {
      devices.forEach((d) => {
        if (d.type !== "hazard" || d.status !== "on" || !d.onSince || !d.maxOnDurationMin) return;
        const elapsed = (Date.now() - d.onSince) / 1000;
        if (elapsed >= d.maxOnDurationMin * 60) {
          logRef.current("warn", d.name, `Auto-cutoff triggered after ${d.maxOnDurationMin} min safety limit`);
          toast.warning(`${d.name} auto-switched OFF`, {
            description: `Max on-duration of ${d.maxOnDurationMin} min exceeded.`,
          });
          // Write to Firebase to switch it off
          update(ref(db, `homes/${HOME_ID}/devices/${d.id}/state`), {
            isOn: false,
            lastChangedBy: "simulator"
          });
        }
      });
    }, 1000);
    return () => clearInterval(interval);
  }, [devices]);

  const selectFloor = useCallback((id: string) => setSelectedFloorId(id), []);

  const addFloor = useCallback(
    (input: AddFloorInput) => {
      const floorId = "floor_" + uid();
      update(ref(db, `homes/${HOME_ID}/floors/${floorId}`), {
        name: input.name,
        order: floors.length,
        gridColumns: input.cols,
        gridRows: input.rows
      }).then(() => {
        log("info", "system", `Floor "${input.name}" registered`);
      });
    },
    [floors.length, log],
  );

  const addDevice = useCallback(
    (floorId: string, cell: { row: number; col: number }, input: AddDeviceInput) => {
      const deviceId = "dev_" + uid();
      const dbType = reverseTypeMap[input.type];
      
      const deviceState: any = {
        isOn: false,
        online: true,
        error: false,
        lastOnAt: 0,
        lastChangedBy: "simulator"
      };
      
      if (input.type === "multi_switch") {
        const switches: any = {};
        const numGangs = input.gangs ?? 2;
        for (let i = 1; i <= numGangs; i++) {
          switches[`s${i}`] = false;
        }
        deviceState.switches = switches;
      } else if (input.type === "camera") {
        deviceState.snapshotUrl = input.snapshotUrl || "https://picsum.photos/seed/camera/640/360";
        deviceState.streamUrl = input.streamUri || "rtsp://192.168.1.100/stream";
      } else if (input.type === "thermostat") {
        deviceState.targetTemperature = 24;
      }
      
      const devicePayload = {
        name: input.name,
        type: dbType,
        floorId: floorId,
        room: "General",
        position: { x: cell.col, y: cell.row },
        state: deviceState,
        config: {
          wattage: input.type === "hazard" ? 1000 : 
                   (input.type === "scheduled_light" ? 12 : 
                   (input.type === "outlet" ? 120 : 
                   (input.type === "camera" ? 5 : 
                   (input.type === "lock" ? 2 : 
                   (input.type === "thermostat" ? 1500 : 0))))),
          maxActiveMinutes: input.type === "hazard" ? (input.maxOnDurationMin ?? 15) : 0
        }
      };
      
      update(ref(db, `homes/${HOME_ID}/devices/${deviceId}`), devicePayload).then(() => {
        log("info", input.name, `Provisioned ${DEVICE_LABELS[input.type]} at R${cell.row + 1}C${cell.col + 1}`);
      });
      
      if (input.type === "scheduled_light") {
        const scheduleId = "sch_" + deviceId;
        update(ref(db, `homes/${HOME_ID}/schedules/${scheduleId}`), {
          deviceId: deviceId,
          onTime: input.scheduleStart || "18:30",
          offTime: input.scheduleEnd || "23:00",
          enabled: true,
          lastRunKey: ""
        });
      }
    },
    [log],
  );

  const removeDevice = useCallback(
    (id: string) => {
      const target = devices.find((d) => d.id === id);
      if (target) {
        remove(ref(db, `homes/${HOME_ID}/devices/${id}`));
        remove(ref(db, `homes/${HOME_ID}/schedules/sch_${id}`)).then(() => {
          log("warn", target.name, "Device removed from database");
        });
      }
    },
    [devices, log],
  );

  const toggleDevice = useCallback(
    (id: string) => {
      const dev = devices.find((d) => d.id === id);
      if (!dev) return;
      
      if (dev.status === "disconnected") {
        log("error", dev.name, "Toggle rejected — device is disconnected");
        toast.error(`${dev.name} is offline`);
        return;
      }
      
      const nextIsOn = dev.status !== "on";
      const patch: any = {
        isOn: nextIsOn,
        error: false, // Toggling device clears its error state
        lastChangedBy: "simulator"
      };
      
      if (nextIsOn) {
        patch.lastOnAt = serverTimestamp();
      }
      
      if (dev.type === "multi_switch" && dev.subSwitches) {
        const switchesPatch = dev.subSwitches.reduce((acc: any, s) => {
          acc[s.id] = nextIsOn;
          return acc;
        }, {});
        patch.switches = switchesPatch;
      }
      
      update(ref(db, `homes/${HOME_ID}/devices/${id}/state`), patch);
    },
    [devices, log],
  );

  const toggleSubSwitch = useCallback(
    (deviceId: string, subId: string) => {
      const dev = devices.find((d) => d.id === deviceId);
      if (!dev || !dev.subSwitches) return;
      
      const sub = dev.subSwitches.find((s) => s.id === subId);
      if (!sub) return;
      
      const nextSubOn = !sub.on;
      const anyOn = dev.subSwitches.some((s) => (s.id === subId ? nextSubOn : s.on));
      
      update(ref(db, `homes/${HOME_ID}/devices/${deviceId}/state`), {
        [`switches/${subId}`]: nextSubOn,
        isOn: anyOn,
        lastChangedBy: "simulator"
      });
    },
    [devices],
  );

  const setStatus = useCallback(
    (id: string, status: DeviceStatus, reason?: string) => {
      const dev = devices.find((d) => d.id === id);
      if (!dev) return;
      
      if (status === "error") {
        const nextError = dev.status !== "error";
        update(ref(db, `homes/${HOME_ID}/devices/${id}/state`), {
          error: nextError,
          lastChangedBy: "simulator"
        });
      } else {
        const nextIsOn = status === "on";
        const patch: any = {
          isOn: nextIsOn,
          error: false,
          lastChangedBy: "simulator"
        };
        if (nextIsOn) {
          patch.lastOnAt = serverTimestamp();
        }
        update(ref(db, `homes/${HOME_ID}/devices/${id}/state`), patch);
      }
    },
    [devices],
  );

  const refreshSnapshot = useCallback(
    (id: string) => {
      const dev = devices.find((d) => d.id === id);
      if (!dev || !dev.snapshotUrl) return;
      
      const baseUrl = dev.snapshotUrl.split("?")[0];
      const newUrl = `${baseUrl}?t=${Date.now()}`;
      
      update(ref(db, `homes/${HOME_ID}/devices/${id}/state`), {
        snapshotUrl: newUrl,
        lastChangedBy: "simulator"
      });
    },
    [devices],
  );

  const setTargetTemperature = useCallback(
    (id: string, temp: number) => {
      update(ref(db, `homes/${HOME_ID}/devices/${id}/state`), {
        targetTemperature: temp,
        lastChangedBy: "simulator"
      });
    },
    [],
  );

  const value = useMemo(
    () => ({
      floors,
      devices,
      logs,
      connection,
      selectedFloorId,
      selectFloor,
      addFloor,
      addDevice,
      removeDevice,
      toggleDevice,
      toggleSubSwitch,
      setStatus,
      refreshSnapshot,
      setTargetTemperature,
      log,
    }),
    [floors, devices, logs, connection, selectedFloorId, selectFloor, addFloor, addDevice, removeDevice, toggleDevice, toggleSubSwitch, setStatus, refreshSnapshot, setTargetTemperature, log],
  );

  return <SimulatorContext.Provider value={value}>{children}</SimulatorContext.Provider>;
}

export function useSimulator() {
  const ctx = useContext(SimulatorContext);
  if (!ctx) throw new Error("useSimulator must be used inside SimulatorProvider");
  return ctx;
}