export type DeviceType = "outlet" | "multi_switch" | "hazard" | "scheduled_light" | "camera" | "lock" | "thermostat";

export type DeviceStatus = "on" | "off" | "error" | "disconnected";

export interface SubSwitch {
  id: string;
  label: string;
  on: boolean;
}

export interface Device {
  id: string;
  floorId: string;
  name: string;
  type: DeviceType;
  status: DeviceStatus;
  row: number;
  col: number;
  /** multi_switch */
  gangs?: number;
  subSwitches?: SubSwitch[];
  /** hazard */
  maxOnDurationMin?: number;
  onSince?: number | null;
  /** scheduled_light */
  scheduleStart?: string;
  scheduleEnd?: string;
  /** camera */
  snapshotUrl?: string;
  streamUri?: string;
  /** thermostat */
  targetTemperature?: number;
}

export interface Floor {
  id: string;
  name: string;
  planImage: string;
  rows: number;
  cols: number;
}

export type LogLevel = "info" | "warn" | "error";

export interface LogEntry {
  id: string;
  at: number;
  level: LogLevel;
  message: string;
  source: string;
}

export const DEVICE_LABELS: Record<DeviceType, string> = {
  outlet: "Outlet",
  multi_switch: "Multi-Switch Unit",
  hazard: "Iron / Hazard Device",
  scheduled_light: "Scheduled Light",
  camera: "Security Camera",
  lock: "Smart Lock",
  thermostat: "Thermostat / AC",
};

export const STATUS_LABELS: Record<DeviceStatus, string> = {
  on: "ON",
  off: "OFF",
  error: "ERROR",
  disconnected: "OFFLINE",
};