import { createFileRoute } from "@tanstack/react-router";
import { useState } from "react";
import { Plus } from "lucide-react";
import { SimulatorProvider, useSimulator } from "@/lib/simulator-store";
import { FloorSidebar } from "@/components/simulator/FloorSidebar";
import { StatusBar } from "@/components/simulator/StatusBar";
import { FloorGrid } from "@/components/simulator/FloorGrid";
import { EventLog } from "@/components/simulator/EventLog";
import { AddFloorDialog } from "@/components/simulator/AddFloorDialog";
import { AddDeviceDialog } from "@/components/simulator/AddDeviceDialog";
import { DeviceDetailDialog } from "@/components/simulator/DeviceDetailDialog";
import { Button } from "@/components/ui/button";

export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: "Hardware Simulator Dashboard — IoT Smart Home Rig" },
      {
        name: "description",
        content:
          "Admin simulator for smart home hardware: floor grids, outlets, multi-switches, hazard timers, cameras and a live event log.",
      },
      { property: "og:title", content: "Hardware Simulator Dashboard — IoT Smart Home Rig" },
      {
        property: "og:description",
        content: "Simulate and monitor smart home devices across floors with live status and event logging.",
      },
      { property: "og:type", content: "website" },
      { name: "twitter:card", content: "summary_large_image" },
    ],
  }),
  component: () => (
    <SimulatorProvider>
      <SimulatorDashboard />
    </SimulatorProvider>
  ),
});

function SimulatorDashboard() {
  const { floors, selectedFloorId } = useSimulator();
  const floor = floors.find((f) => f.id === selectedFloorId) ?? floors[0];
  const [floorOpen, setFloorOpen] = useState(false);
  const [deviceCell, setDeviceCell] = useState<{ row: number; col: number } | null>(null);
  const [detailId, setDetailId] = useState<string | null>(null);

  return (
    <div className="flex min-h-screen flex-col bg-background text-foreground md:h-screen md:flex-row md:overflow-hidden">
      <FloorSidebar onAddFloor={() => setFloorOpen(true)} />

      <div className="flex min-w-0 flex-1 flex-col">
        <StatusBar />

        <main className="flex-1 overflow-y-auto p-4">
          {floor ? (
            <>
              <div className="mb-3 grid grid-cols-[minmax(0,1fr)_auto] items-center gap-3 sm:flex sm:justify-between">
                <div className="min-w-0">
                  <h1 className="truncate text-lg font-semibold">{floor.name}</h1>
                  <p className="truncate font-mono text-[11px] uppercase tracking-wider text-muted-foreground">
                    grid {floor.rows}×{floor.cols} · click a tile to toggle or inspect
                  </p>
                </div>
                <Button size="sm" onClick={() => setDeviceCell({ row: 0, col: 0 })}>
                  <Plus className="mr-1.5 h-4 w-4" /> Add Device
                </Button>
              </div>
              <FloorGrid floor={floor} onAddAt={setDeviceCell} onOpenDevice={(d) => setDetailId(d.id)} />
            </>
          ) : (
            <p className="py-20 text-center text-muted-foreground">No floors yet — add one to begin.</p>
          )}
        </main>

        <EventLog />
      </div>

      <AddFloorDialog open={floorOpen} onOpenChange={setFloorOpen} />
      <AddDeviceDialog
        open={!!deviceCell}
        onOpenChange={(o) => !o && setDeviceCell(null)}
        floorId={floor?.id ?? null}
        cell={deviceCell}
      />
      <DeviceDetailDialog deviceId={detailId} onOpenChange={(o) => !o && setDetailId(null)} />
    </div>
  );
}
