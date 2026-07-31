import { useState } from "react";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { useSimulator } from "@/lib/simulator-store";
import { DEVICE_LABELS, type DeviceType } from "@/lib/simulator-types";

export function AddDeviceDialog({
  open,
  onOpenChange,
  floorId,
  cell,
}: {
  open: boolean;
  onOpenChange: (o: boolean) => void;
  floorId: string | null;
  cell: { row: number; col: number } | null;
}) {
  const { addDevice } = useSimulator();
  const [type, setType] = useState<DeviceType>("outlet");
  const [name, setName] = useState("");
  const [gangs, setGangs] = useState("2");
  const [maxDuration, setMaxDuration] = useState(30);
  const [start, setStart] = useState("18:00");
  const [end, setEnd] = useState("23:00");
  const [snapshotUrl, setSnapshotUrl] = useState("https://picsum.photos/seed/cam/640/360");
  const [streamUri, setStreamUri] = useState("rtsp://192.168.1.50:554/stream1");

  const submit = () => {
    if (!floorId || !cell || !name.trim()) return;
    addDevice(floorId, cell, {
      name: name.trim(),
      type,
      gangs: Number(gangs),
      maxOnDurationMin: maxDuration,
      scheduleStart: start,
      scheduleEnd: end,
      snapshotUrl,
      streamUri,
    });
    setName("");
    onOpenChange(false);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Add device</DialogTitle>
          <DialogDescription>
            {cell ? `Placing at row ${cell.row + 1}, column ${cell.col + 1}.` : "Select a grid cell."}
          </DialogDescription>
        </DialogHeader>

        <div className="grid gap-4">
          <div className="grid gap-2">
            <Label>Device type</Label>
            <Select value={type} onValueChange={(v) => setType(v as DeviceType)}>
              <SelectTrigger><SelectValue /></SelectTrigger>
              <SelectContent>
                {(Object.keys(DEVICE_LABELS) as DeviceType[]).map((t) => (
                  <SelectItem key={t} value={t}>{DEVICE_LABELS[t]}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className="grid gap-2">
            <Label htmlFor="device-name">Device name</Label>
            <Input id="device-name" value={name} onChange={(e) => setName(e.target.value)} placeholder="Kitchen outlet" />
          </div>

          {type === "multi_switch" && (
            <div className="grid gap-2">
              <Label>Number of gangs</Label>
              <Select value={gangs} onValueChange={setGangs}>
                <SelectTrigger><SelectValue /></SelectTrigger>
                <SelectContent>
                  {["2", "3", "5"].map((g) => (
                    <SelectItem key={g} value={g}>{g} gangs</SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <p className="text-xs text-muted-foreground">Sub-switches are generated and individually toggleable.</p>
            </div>
          )}

          {type === "hazard" && (
            <div className="grid gap-2">
              <Label htmlFor="max-dur">Max on-duration (minutes)</Label>
              <Input id="max-dur" type="number" min={1} value={maxDuration} onChange={(e) => setMaxDuration(Number(e.target.value))} />
            </div>
          )}

          {type === "scheduled_light" && (
            <div className="grid grid-cols-2 gap-3">
              <div className="grid gap-2">
                <Label htmlFor="start">Schedule start</Label>
                <Input id="start" type="time" value={start} onChange={(e) => setStart(e.target.value)} />
              </div>
              <div className="grid gap-2">
                <Label htmlFor="end">Schedule end</Label>
                <Input id="end" type="time" value={end} onChange={(e) => setEnd(e.target.value)} />
              </div>
            </div>
          )}

          {type === "camera" && (
            <div className="grid gap-3">
              <div className="grid gap-2">
                <Label htmlFor="snap">Mock snapshot image URL</Label>
                <Input id="snap" value={snapshotUrl} onChange={(e) => setSnapshotUrl(e.target.value)} />
              </div>
              <div className="grid gap-2">
                <Label htmlFor="stream">Mock stream URI</Label>
                <Input id="stream" value={streamUri} onChange={(e) => setStreamUri(e.target.value)} />
              </div>
            </div>
          )}
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Cancel</Button>
          <Button onClick={submit} disabled={!name.trim()}>Add device</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}