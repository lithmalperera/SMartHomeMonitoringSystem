import { Trash2 } from "lucide-react";
import { useSimulator } from "@/lib/simulator-store";
import { STATUS_LABELS, type Device } from "@/lib/simulator-types";
import { DEVICE_ICONS, STATUS_BADGE, STATUS_RING } from "./device-visuals";

export function DeviceTile({ device, onOpen }: { device: Device; onOpen: (d: Device) => void }) {
  const { toggleDevice, removeDevice } = useSimulator();
  const Icon = DEVICE_ICONS[device.type];
  const opensDetail = device.type === "camera" || device.type === "hazard" || device.type === "multi_switch" || device.type === "thermostat" || device.type === "lock";

  return (
    <div
      role="button"
      tabIndex={0}
      onClick={() => (opensDetail ? onOpen(device) : toggleDevice(device.id))}
      onKeyDown={(e) => {
        if (e.key === "Enter" || e.key === " ") {
          e.preventDefault();
          opensDetail ? onOpen(device) : toggleDevice(device.id);
        }
      }}
      className={`group relative flex h-full w-full cursor-pointer flex-col justify-between gap-1 rounded-lg border bg-card/95 p-2 text-left shadow-sm backdrop-blur transition-all duration-200 hover:-translate-y-0.5 hover:shadow-md ${STATUS_RING[device.status]}`}
    >
      <div className="flex items-start justify-between gap-1">
        <span
          className={`grid h-7 w-7 shrink-0 place-items-center rounded-md transition-colors ${
            device.status === "on" ? "bg-status-on/15 text-status-on" : "bg-muted text-muted-foreground"
          }`}
        >
          <Icon className="h-4 w-4" />
        </span>
        <button
          aria-label={`Remove ${device.name}`}
          onClick={(e) => {
            e.stopPropagation();
            removeDevice(device.id);
          }}
          className="opacity-0 transition-opacity group-hover:opacity-100"
        >
          <Trash2 className="h-3.5 w-3.5 text-muted-foreground hover:text-destructive" />
        </button>
      </div>
      <p className="line-clamp-2 text-[11px] font-medium leading-tight">{device.name}</p>
      <span
        className={`inline-flex w-fit items-center rounded px-1.5 py-0.5 font-mono text-[9px] font-bold tracking-wider ${STATUS_BADGE[device.status]}`}
      >
        {STATUS_LABELS[device.status]}
      </span>
    </div>
  );
}