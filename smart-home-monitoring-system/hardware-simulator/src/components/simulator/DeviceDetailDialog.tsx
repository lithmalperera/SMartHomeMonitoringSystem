import { useEffect, useState } from "react";
import { RefreshCw } from "lucide-react";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Switch } from "@/components/ui/switch";
import { useSimulator } from "@/lib/simulator-store";
import { DEVICE_LABELS, STATUS_LABELS, type Device } from "@/lib/simulator-types";
import { STATUS_BADGE } from "./device-visuals";

function formatDuration(sec: number) {
  const m = Math.floor(sec / 60);
  const s = Math.floor(sec % 60);
  return `${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
}

export function DeviceDetailDialog({
  deviceId,
  onOpenChange,
}: {
  deviceId: string | null;
  onOpenChange: (o: boolean) => void;
}) {
  const { devices, toggleDevice, toggleSubSwitch, setStatus, refreshSnapshot, setTargetTemperature, log } = useSimulator();
  const device: Device | undefined = devices.find((d) => d.id === deviceId);
  const [now, setNow] = useState(Date.now());
  const [snapshotKey, setSnapshotKey] = useState(0);

  useEffect(() => {
    const i = setInterval(() => setNow(Date.now()), 500);
    return () => clearInterval(i);
  }, []);

  if (!device) return null;

  const elapsed = device.onSince ? (now - device.onSince) / 1000 : 0;
  const limit = (device.maxOnDurationMin ?? 0) * 60;
  const pct = limit ? Math.min(100, (elapsed / limit) * 100) : 0;
  const imgUrl = device.snapshotUrl || "https://picsum.photos/seed/camera/640/360";

  return (
    <Dialog open={!!deviceId} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle className="flex flex-wrap items-center gap-2">
            {device.name}
            <span className={`rounded px-1.5 py-0.5 font-mono text-[10px] font-bold ${STATUS_BADGE[device.status]}`}>
              {STATUS_LABELS[device.status]}
            </span>
          </DialogTitle>
          <DialogDescription>{DEVICE_LABELS[device.type]}</DialogDescription>
        </DialogHeader>

        {device.type === "hazard" && (
          <div className="grid gap-3">
            <div className="rounded-lg border border-border bg-secondary p-4 text-center">
              <p className="font-mono text-[11px] uppercase tracking-[0.18em] text-muted-foreground">
                {device.status === "on" ? "Time powered" : "Idle"}
              </p>
              <p className="font-mono text-4xl font-bold tabular-nums">{formatDuration(elapsed)}</p>
              <p className="mt-1 text-xs text-muted-foreground">
                Safety cutoff at {device.maxOnDurationMin} min
              </p>
            </div>
            <div className="h-2 overflow-hidden rounded-full bg-muted">
              <div
                className={`h-full transition-all duration-500 ${pct > 80 ? "bg-status-error" : "bg-status-on"}`}
                style={{ width: `${pct}%` }}
              />
            </div>
            <div className="flex gap-2">
              <Button className="flex-1" onClick={() => toggleDevice(device.id)}>
                {device.status === "on" ? "Switch OFF" : "Switch ON"}
              </Button>
              <Button variant="outline" onClick={() => setStatus(device.id, "error", "Fault injected by operator")}>
                Inject fault
              </Button>
            </div>
          </div>
        )}

        {device.type === "multi_switch" && (
          <div className="grid gap-2">
            {device.subSwitches?.map((s) => (
              <div key={s.id} className="flex items-center justify-between rounded-lg border border-border bg-secondary px-3 py-2">
                <span className="text-sm font-medium">{s.label}</span>
                <Switch checked={s.on} onCheckedChange={() => toggleSubSwitch(device.id, s.id)} />
              </div>
            ))}
            <Button variant="outline" onClick={() => toggleDevice(device.id)}>
              Toggle all gangs
            </Button>
          </div>
        )}

        {device.type === "camera" && (
          <div className="grid gap-3">
            <div className="overflow-hidden rounded-lg border border-border bg-muted">
              <img
                key={snapshotKey}
                src={`${imgUrl}${imgUrl.includes("?") ? "&" : "?"}k=${snapshotKey}`}
                alt={`${device.name} snapshot`}
                loading="lazy"
                className="aspect-video w-full animate-in fade-in object-cover"
              />
            </div>
            <p className="truncate font-mono text-xs text-muted-foreground">{device.streamUri ?? "no stream uri"}</p>
            <Button
              variant="outline"
              onClick={() => {
                refreshSnapshot(device.id);
                setSnapshotKey((k) => k + 1);
                log("info", device.name, "Snapshot refreshed");
              }}
            >
              <RefreshCw className="mr-2 h-4 w-4" /> Refresh snapshot
            </Button>
          </div>
        )}

        {device.type === "lock" && (
          <div className="grid gap-3">
            <div className="rounded-lg border border-border bg-secondary p-4 text-center">
              <p className="font-mono text-[11px] uppercase tracking-[0.18em] text-muted-foreground">
                Current Status
              </p>
              <p className="font-mono text-3xl font-bold tabular-nums">
                {device.status === "on" ? "🔒 LOCKED" : "🔓 UNLOCKED"}
              </p>
            </div>
            <div className="flex gap-2">
              <Button className="flex-1" onClick={() => toggleDevice(device.id)}>
                {device.status === "on" ? "Unlock" : "Lock"}
              </Button>
            </div>
          </div>
        )}

        {device.type === "thermostat" && (
          <div className="grid gap-3">
            <div className="rounded-lg border border-border bg-secondary p-4 text-center">
              <p className="font-mono text-[11px] uppercase tracking-[0.18em] text-muted-foreground">
                Target Temperature
              </p>
              <p className="font-mono text-4xl font-bold tabular-nums">
                {device.targetTemperature ?? 24}°C
              </p>
              <p className="mt-1 text-xs text-muted-foreground">
                Status: {device.status === "on" ? "AC Cooling" : "Idle"}
              </p>
            </div>
            <div className="flex gap-2">
              <Button
                variant="outline"
                className="flex-1"
                disabled={device.status !== "on"}
                onClick={() => {
                  const currentTemp = device.targetTemperature ?? 24;
                  if (currentTemp > 16) {
                    setTargetTemperature(device.id, currentTemp - 1);
                  }
                }}
              >
                - 1°C
              </Button>
              <Button
                variant="outline"
                className="flex-1"
                disabled={device.status !== "on"}
                onClick={() => {
                  const currentTemp = device.targetTemperature ?? 24;
                  if (currentTemp < 30) {
                    setTargetTemperature(device.id, currentTemp + 1);
                  }
                }}
              >
                + 1°C
              </Button>
            </div>
            <Button className="w-full" onClick={() => toggleDevice(device.id)}>
              {device.status === "on" ? "Turn AC OFF" : "Turn AC ON"}
            </Button>
          </div>
        )}

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Close</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}