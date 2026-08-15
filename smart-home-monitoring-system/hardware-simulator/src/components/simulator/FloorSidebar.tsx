import { Building2, Plus } from "lucide-react";
import { useSimulator } from "@/lib/simulator-store";

export function FloorSidebar({ onAddFloor }: { onAddFloor: () => void }) {
  const { floors, devices, selectedFloorId, selectFloor } = useSimulator();

  return (
    <aside className="flex w-full shrink-0 flex-col bg-sidebar text-sidebar-foreground md:h-full md:w-64 md:border-r md:border-sidebar-border">
      <div className="flex items-center gap-2 border-b border-sidebar-border px-4 py-4">
        <div className="h-10 w-10 shrink-0">
          <img src="/ic_logo.png" alt="Hestia Logo" className="h-full w-full object-contain" />
        </div>
        <div className="min-w-0">
          <p className="truncate text-sm font-semibold">Hardware Simulator</p>
          <p className="truncate font-mono text-[10px] uppercase tracking-[0.16em] text-sidebar-foreground/50">
            Hestia
          </p>
        </div>
      </div>

      <nav className="flex-1 overflow-y-auto p-3">
        <p className="px-2 pb-2 font-mono text-[10px] uppercase tracking-[0.18em] text-sidebar-foreground/45">
          Floors
        </p>
        <ul className="flex flex-col gap-1">
          {floors.map((f) => {
            const active = f.id === selectedFloorId;
            const count = devices.filter((d) => d.floorId === f.id).length;
            return (
              <li key={f.id}>
                <button
                  onClick={() => selectFloor(f.id)}
                  className={`flex w-full items-center gap-2 rounded-md px-2.5 py-2 text-left text-sm transition-colors ${
                    active
                      ? "bg-sidebar-accent text-sidebar-accent-foreground"
                      : "text-sidebar-foreground/75 hover:bg-sidebar-accent/60"
                  }`}
                >
                  <Building2 className="h-4 w-4 shrink-0" />
                  <span className="min-w-0 flex-1 truncate">{f.name}</span>
                  <span className="shrink-0 rounded bg-sidebar-border px-1.5 py-0.5 font-mono text-[10px]">
                    {count}
                  </span>
                </button>
              </li>
            );
          })}
        </ul>
      </nav>

      <div className="border-t border-sidebar-border p-3">
        <button
          onClick={onAddFloor}
          className="flex w-full items-center justify-center gap-2 rounded-md bg-sidebar-primary px-3 py-2 text-sm font-medium text-sidebar-primary-foreground transition-opacity hover:opacity-90"
        >
          <Plus className="h-4 w-4" /> Add Floor
        </button>
      </div>
    </aside>
  );
}