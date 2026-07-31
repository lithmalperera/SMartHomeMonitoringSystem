import { Plus } from "lucide-react";
import { useSimulator } from "@/lib/simulator-store";
import type { Device, Floor } from "@/lib/simulator-types";
import { DeviceTile } from "./DeviceTile";

export function FloorGrid({
  floor,
  onAddAt,
  onOpenDevice,
}: {
  floor: Floor;
  onAddAt: (cell: { row: number; col: number }) => void;
  onOpenDevice: (d: Device) => void;
}) {
  const { devices } = useSimulator();
  const floorDevices = devices.filter((d) => d.floorId === floor.id);
  const cells = Array.from({ length: floor.rows * floor.cols }, (_, i) => ({
    row: Math.floor(i / floor.cols),
    col: i % floor.cols,
  }));

  return (
    <div className="relative overflow-hidden rounded-xl border border-border bg-card">
      <img
        src={floor.planImage}
        alt={`${floor.name} floor plan`}
        loading="lazy"
        className="absolute inset-0 h-full w-full object-cover opacity-25"
      />
      <div
        className="relative grid gap-2 p-3"
        style={{
          gridTemplateColumns: `repeat(${floor.cols}, minmax(0, 1fr))`,
          gridAutoRows: "minmax(84px, auto)",
        }}
      >
        {cells.map(({ row, col }) => {
          const device = floorDevices.find((d) => d.row === row && d.col === col);
          return device ? (
            <DeviceTile key={device.id} device={device} onOpen={onOpenDevice} />
          ) : (
            <button
              key={`${row}-${col}`}
              onClick={() => onAddAt({ row, col })}
              aria-label={`Add device at row ${row + 1} column ${col + 1}`}
              className="grid place-items-center rounded-lg border border-dashed border-border/70 bg-background/40 text-muted-foreground/50 transition-colors hover:border-primary hover:bg-primary/5 hover:text-primary"
            >
              <Plus className="h-4 w-4" />
            </button>
          );
        })}
      </div>
    </div>
  );
}