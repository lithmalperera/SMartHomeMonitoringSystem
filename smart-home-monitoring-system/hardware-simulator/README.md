# Smart Home Simulator

Build a responsive web-based "Hardware Simulator Dashboard" for an IoT smart home system, using React + Tailwind CSS. This is an admin/simulator tool (not the end-user mobile app) that represents physical smart home devices and syncs with a backend database in real time.

Layout: Sidebar with list of floors (+ "Add Floor" button). Main panel shows the selected floor's grid layout with an uploaded/placeholder floor plan image and a grid overlay where devices are placed as icons/tiles.

Add Floor modal: name field, floor plan image upload/URL, grid rows/cols input.

Add Device modal: device type selector (Outlet, Multi-Switch Unit, Iron/Hazard Device, Scheduled Light, Security Camera) that dynamically shows relevant fields:

Outlet: name only

Multi-Switch: name + number of gangs (2/3/5), auto-generates individually toggleable sub-switches

Iron/Hazard: name + max_on_duration (minutes)

Scheduled Light: name + start/end schedule time

Camera: name + mock image URL / mock stream URI

Device tile on grid: shows icon based on type, name, and a colored status badge (green=ON, gray=OFF, red=ERROR, black/striped=DISCONNECTED). Clicking toggles state (for outlets/switches/lights) or opens detail view (for cameras/irons).

Iron/hazard device detail: live countdown timer while ON, progress bar toward max_on_duration, auto-flips to OFF with a toast/alert when duration is exceeded.

Camera detail: shows a mock snapshot image in a modal with a "refresh snapshot" button.

Bottom panel: live scrolling event log (timestamped state changes, faults, auto-cutoffs), and a top status bar showing backend connection state and aggregate counts of devices by status.

Style: clean, technical/dashboard aesthetic (dark sidebar, card-based grid, subtle animations on state change), distinct color coding per status, mobile-responsive but optimized for desktop/tablet use.

Use mock/local state for now, structured so device state updates can later be wired to real-time database listeners (e.g., Firebase/Supabase onSnapshot).

This project was built with [Lovable](https://lovable.dev).

## Build with Lovable

Continue developing this project in the [Lovable editor](https://lovable.dev/projects/8b959fd9-5805-4fb3-a1cf-1d3c6e44b93d).

- **Ship faster**: describe what you want to build and Lovable handles the code.
- **Stay in sync**: every change made in Lovable is committed straight to this repository.
- **Full ownership**: this code is yours. Push to `main` on GitHub and your changes sync back into Lovable, ready for your next prompt.

## Development

Prefer working locally? You need Node.js and npm — [install with nvm](https://github.com/nvm-sh/nvm#installing-and-updating).

```sh
git clone <this-repository-url>
cd <repository-name>
npm i
npm run dev
```
