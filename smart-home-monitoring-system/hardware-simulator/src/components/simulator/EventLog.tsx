import { useSimulator } from "@/lib/simulator-store";
import type { LogLevel } from "@/lib/simulator-types";

const levelColor: Record<LogLevel, string> = {
  info: "text-status-on",
  warn: "text-chart-5",
  error: "text-status-error",
};

export function EventLog() {
  const { logs } = useSimulator();

  return (
    <section className="flex h-56 flex-col border-t border-sidebar-border bg-panel text-panel-foreground">
      <div className="flex items-center justify-between border-b border-sidebar-border px-4 py-2">
        <h2 className="font-mono text-[11px] font-semibold uppercase tracking-[0.18em] text-panel-foreground/70">
          Event log
        </h2>
        <span className="font-mono text-[11px] text-panel-foreground/50">{logs.length} events</span>
      </div>
      <div className="flex-1 overflow-y-auto px-4 py-2 font-mono text-xs">
        {logs.length === 0 && <p className="py-6 text-center text-panel-foreground/40">Awaiting device events…</p>}
        {logs.map((l) => (
          <div
            key={l.id}
            className="flex animate-in fade-in slide-in-from-bottom-1 gap-3 border-b border-white/5 py-1.5 last:border-0"
          >
            <span className="shrink-0 tabular-nums text-panel-foreground/45">
              {new Date(l.at).toLocaleTimeString()}
            </span>
            <span className={`w-12 shrink-0 uppercase ${levelColor[l.level]}`}>{l.level}</span>
            <span className="shrink-0 truncate text-panel-foreground/70 sm:w-40">{l.source}</span>
            <span className="min-w-0 flex-1 break-words">{l.message}</span>
          </div>
        ))}
      </div>
    </section>
  );
}