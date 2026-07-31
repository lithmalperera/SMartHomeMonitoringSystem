import { useState } from "react";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useSimulator } from "@/lib/simulator-store";
import fallbackPlan from "@/assets/floor-plan-ground.jpg";

export function AddFloorDialog({ open, onOpenChange }: { open: boolean; onOpenChange: (o: boolean) => void }) {
  const { addFloor } = useSimulator();
  const [name, setName] = useState("");
  const [imageUrl, setImageUrl] = useState("");
  const [rows, setRows] = useState(4);
  const [cols, setCols] = useState(6);

  const handleFile = (file?: File) => {
    if (!file) return;
    const reader = new FileReader();
    reader.onload = () => setImageUrl(String(reader.result));
    reader.readAsDataURL(file);
  };

  const submit = () => {
    if (!name.trim()) return;
    addFloor({
      name: name.trim(),
      planImage: imageUrl.trim() || fallbackPlan,
      rows: Math.min(Math.max(rows, 1), 10),
      cols: Math.min(Math.max(cols, 1), 10),
    });
    setName("");
    setImageUrl("");
    setRows(4);
    setCols(6);
    onOpenChange(false);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Add floor</DialogTitle>
          <DialogDescription>Register a new floor layout in the simulator.</DialogDescription>
        </DialogHeader>
        <div className="grid gap-4">
          <div className="grid gap-2">
            <Label htmlFor="floor-name">Floor name</Label>
            <Input id="floor-name" value={name} onChange={(e) => setName(e.target.value)} placeholder="Basement" />
          </div>
          <div className="grid gap-2">
            <Label htmlFor="floor-url">Floor plan image URL</Label>
            <Input id="floor-url" value={imageUrl.startsWith("data:") ? "" : imageUrl} onChange={(e) => setImageUrl(e.target.value)} placeholder="https://…" />
            <Input type="file" accept="image/*" onChange={(e) => handleFile(e.target.files?.[0])} />
            <p className="text-xs text-muted-foreground">Leave empty to use a placeholder blueprint.</p>
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div className="grid gap-2">
              <Label htmlFor="rows">Grid rows</Label>
              <Input id="rows" type="number" min={1} max={10} value={rows} onChange={(e) => setRows(Number(e.target.value))} />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="cols">Grid columns</Label>
              <Input id="cols" type="number" min={1} max={10} value={cols} onChange={(e) => setCols(Number(e.target.value))} />
            </div>
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Cancel</Button>
          <Button onClick={submit} disabled={!name.trim()}>Create floor</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}