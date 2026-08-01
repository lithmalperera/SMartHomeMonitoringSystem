---
name: Lumina Home
colors:
  surface: '#f8f9ff'
  surface-dim: '#cbdbf5'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eff4ff'
  surface-container: '#e5eeff'
  surface-container-high: '#dce9ff'
  surface-container-highest: '#d3e4fe'
  on-surface: '#0b1c30'
  on-surface-variant: '#434655'
  inverse-surface: '#213145'
  inverse-on-surface: '#eaf1ff'
  outline: '#737686'
  outline-variant: '#c3c6d7'
  surface-tint: '#0053db'
  primary: '#004ac6'
  on-primary: '#ffffff'
  primary-container: '#2563eb'
  on-primary-container: '#eeefff'
  inverse-primary: '#b4c5ff'
  secondary: '#5c5f61'
  on-secondary: '#ffffff'
  secondary-container: '#e0e3e5'
  on-secondary-container: '#626567'
  tertiary: '#943700'
  on-tertiary: '#ffffff'
  tertiary-container: '#bc4800'
  on-tertiary-container: '#ffede6'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dbe1ff'
  primary-fixed-dim: '#b4c5ff'
  on-primary-fixed: '#00174b'
  on-primary-fixed-variant: '#003ea8'
  secondary-fixed: '#e0e3e5'
  secondary-fixed-dim: '#c4c7c9'
  on-secondary-fixed: '#191c1e'
  on-secondary-fixed-variant: '#444749'
  tertiary-fixed: '#ffdbcd'
  tertiary-fixed-dim: '#ffb596'
  on-tertiary-fixed: '#360f00'
  on-tertiary-fixed-variant: '#7d2d00'
  background: '#f8f9ff'
  on-background: '#0b1c30'
  surface-variant: '#d3e4fe'
typography:
  headline-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.01em
  label-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.02em
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 4px
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 32px
  margin-mobile: 20px
  gutter-mobile: 12px
---

## Brand & Style

The design system is centered on the concept of "Invisible Intelligence"—a framework that feels reliable, effortless, and human-centric. The target audience includes homeowners and tech enthusiasts who prioritize safety and efficiency. 

The visual style is **Modern / Corporate**, leaning heavily into high-clarity minimalism. It utilizes ample whitespace to reduce cognitive load in data-dense environments. Surfaces are defined by soft, organic depth rather than harsh lines, evoking a sense of calm and control. The interface prioritizes high-contrast legibility to ensure the home remains accessible to all age groups and lighting conditions.

## Colors

The palette is anchored by a trustworthy Primary Blue, used for active states and critical actions. 

- **Primary (#2563EB):** Use for primary buttons, active toggles, and brand identifiers.
- **Secondary (#F8FAFC):** The foundational canvas color. Use for page backgrounds and subtle grouping containers.
- **Semantic Colors:** 
    - **Success (#22C55E):** Exclusively for 'ON' states and completed automations.
    - **Warning (#F59E0B):** For temporary states like "Auto-off in progress" or battery low.
    - **Danger (#EF4444):** For critical hardware errors, offline status, or security alerts.
- **Neutral:** A range of Slate grays (from #0F172A for text to #E2E8F0 for borders) provides structure and signifies 'OFF' or 'Standby' states.

## Typography

The design system utilizes **Inter** for its exceptional legibility on small screens and its neutral, systematic aesthetic. 

- **Hierarchy:** Use `headline-lg` for room names or dashboard greetings. `headline-sm` is reserved for device card titles.
- **Weight:** Use Bold (700) for primary data points (e.g., temperature) and Semi-Bold (600) for navigation.
- **Labels:** Small labels (`label-sm`) should be used for secondary metadata like "Last seen 2m ago" or "Serial Number."
- **Contrast:** Ensure all body text maintains a minimum contrast ratio of 4.5:1 against secondary background colors.

## Layout & Spacing

This design system uses a **Fluid Grid** with a base 4px scaling system. 

- **Mobile Layout:** A 4-column grid with 20px side margins and 12px gutters. 
- **Rhythm:** Vertical spacing between device cards should be 16px (md). Spacing between functional groups (e.g., "Lights" vs "Security") should be 32px (xl).
- **Safe Areas:** Adhere to platform-specific safe areas (notch/bottom indicator), ensuring that the primary "Quick Action" buttons are within the thumb-zone for one-handed use.

## Elevation & Depth

Hierarchy is established through **Ambient Shadows** and **Tonal Layers**. 

- **Level 0 (Background):** The Secondary color (#F8FAFC) serves as the base layer.
- **Level 1 (Cards):** Main device and floor containers use a white surface with a very soft, diffused shadow: `box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05)`.
- **Level 2 (Active/Interaction):** When a user interacts with a card or a modal appears, elevation increases with a slightly larger shadow: `box-shadow: 0 10px 25px rgba(0, 0, 0, 0.08)`.
- **Level 3 (Sticky Elements):** Bottom navigation bars use a subtle top-border (`1px #E2E8F0`) and no shadow to maintain a clean, flat appearance.

## Shapes

The shape language is friendly and tactile. 

- **Standard Containers:** Use 16px (rounded-lg) for device cards and room selectors.
- **Interactive Elements:** Buttons and input fields follow the 12px (0.75rem) standard.
- **Status Indicators:** Badges and toggles should be fully rounded (Pill-shaped) to distinguish them from structural layout elements.
- **Icons:** Icons should be contained within a 40px x 40px rounded square (12px radius) to create a consistent visual "hit area."

## Components

- **Device Cards:** The primary UI unit. Features a 16px corner radius, a white background, and a subtle shadow. The top-left contains the icon; the top-right contains the status badge (Success green for 'ON', Neutral gray for 'OFF'). The bottom-left displays the device name and location.
- **Tactile Toggles:** Large-scale switches (min 48px height) with a clear thumb-slide. When active, the track fills with Primary Blue; when inactive, the track is a light gray.
- **Status Badges:** Compact pills with high-contrast text. Use Success Green for "Active", Danger Red for "Error", and Warning Amber for "Alert".
- **Input Fields:** Used for scene naming or Wi-Fi configuration. Minimalist design with a 1px border (#E2E8F0) that turns Primary Blue on focus.
- **Floor Selectors:** A horizontal scrolling list of chips at the top of the dashboard. Selected state uses a Primary Blue background with white text; unselected uses a white background with a light border.
- **Icons:** Use a consistent 2pt stroke weight with rounded caps. Icons for Outlet, Switch, Iron, Bulb, and Camera should have a unified visual volume.