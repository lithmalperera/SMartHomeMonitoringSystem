import { Plug, ToggleLeft, Flame, Lightbulb, Camera, Lock, Thermometer, type LucideIcon } from "lucide-react";
import type { DeviceStatus, DeviceType } from "@/lib/simulator-types";

export const DEVICE_ICONS: Record<DeviceType, LucideIcon> = {
  outlet: Plug,
  multi_switch: ToggleLeft,
  hazard: Flame,
  scheduled_light: Lightbulb,
  camera: Camera,
  lock: Lock,
  thermostat: Thermometer,
};

export const STATUS_BADGE: Record<DeviceStatus, string> = {
  on: "bg-status-on text-status-on-foreground",
  off: "bg-status-off text-status-off-foreground",
  error: "bg-status-error text-status-error-foreground",
  disconnected:
    "bg-status-disconnected text-status-disconnected-foreground bg-[repeating-linear-gradient(45deg,var(--status-disconnected),var(--status-disconnected)_4px,transparent_4px,transparent_8px)]",
};

export const STATUS_RING: Record<DeviceStatus, string> = {
  on: "border-status-on/70 shadow-[0_0_0_1px_var(--status-on)]",
  off: "border-border",
  error: "border-status-error/70 shadow-[0_0_0_1px_var(--status-error)]",
  disconnected: "border-status-disconnected/60 opacity-70",
};