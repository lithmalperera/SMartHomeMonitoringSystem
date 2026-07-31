import { Activity, RefreshCw, WifiOff } from "lucide-react";
import { useSimulator } from "@/lib/simulator-store";
import type { DeviceStatus } from "@/lib/simulator-types";

const counters: { key: DeviceStatus; label: string; dot: string }[] = [
  { key: "on", label: "On", dot: "bg-status-on" },
  { key: "off", label: "Off", dot: "bg-status-off" },
  { key: "error", label: "Error", dot: "bg-status-error" },
  { key: "disconnected", label: "Offline", dot: "bg-status-disconnected" },
];

export function StatusBar() {
  const { devices, connection } = useSimulator();

  return (
    <header className="grid grid-cols-[minmax(0,1fr)_auto] items-center gap-3 border-b border-border bg-card px-4 py-3 sm:flex sm:flex-wrap sm:justify-between">
      <div className="flex min-w-0 items-center gap-3">
        <span
          className={`grid h-9 w-9 shrink-0 place-items-center rounded-lg ${
            connection === "connected" ? "bg-status-on/15 text-status-on" : "bg-muted text-muted-foreground"
          }`}
        >
          {connection === "offline" ? (
            <WifiOff className="h-4 w-4" />
          ) : connection === "connected" ? (
            <Activity className="h-4 w-4" />
          ) : (
            <RefreshCw className="h-4 w-4 animate-spin" />
          )}
        </span>
        <div className="min-w-0">
          <p className="truncate text-sm font-semibold">
            {connection === "connected"
              ? "Backend connected"
              : connection === "syncing"
                ? "Connecting to backend…"
                : "Backend unreachable"}
          </p>
          <p className="truncate font-mono text-[11px] uppercase tracking-wider text-muted-foreground">
            realtime channel · {devices.length} devices registered
          </p>
        </div>
      </div>

      <div className="flex flex-wrap items-center gap-2">
        {counters.map((c) => (
          <div
            key={c.key}
            className="flex items-center gap-2 rounded-md border border-border bg-secondary px-2.5 py-1.5"
          >
            <span className={`h-2 w-2 rounded-full ${c.dot}`} />
            <span className="text-xs text-muted-foreground">{c.label}</span>
            <span className="font-mono text-xs font-semibold tabular-nums">
              {devices.filter((d) => d.status === c.key).length}
            </span>
          </div>
        ))}
      </div>
    </header>
  );
}