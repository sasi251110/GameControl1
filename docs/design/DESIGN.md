---
name: GameControl Studio
colors:
  surface: '#121317'
  surface-dim: '#121317'
  surface-bright: '#38393d'
  surface-container-lowest: '#0d0e12'
  surface-container-low: '#1a1b20'
  surface-container: '#1f1f24'
  surface-container-high: '#292a2e'
  surface-container-highest: '#343439'
  on-surface: '#e3e2e7'
  on-surface-variant: '#c9c4d8'
  inverse-surface: '#e3e2e7'
  inverse-on-surface: '#2f3035'
  outline: '#938ea1'
  outline-variant: '#484555'
  surface-tint: '#cabeff'
  primary: '#cabeff'
  on-primary: '#32009a'
  primary-container: '#947dff'
  on-primary-container: '#2b0088'
  inverse-primary: '#613de0'
  secondary: '#7bd0ff'
  on-secondary: '#00354a'
  secondary-container: '#00a6e0'
  on-secondary-container: '#00374d'
  tertiary: '#4edea3'
  on-tertiary: '#003824'
  tertiary-container: '#00a572'
  on-tertiary-container: '#00311f'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#e6deff'
  primary-fixed-dim: '#cabeff'
  on-primary-fixed: '#1c0062'
  on-primary-fixed-variant: '#4918c8'
  secondary-fixed: '#c4e7ff'
  secondary-fixed-dim: '#7bd0ff'
  on-secondary-fixed: '#001e2c'
  on-secondary-fixed-variant: '#004c69'
  tertiary-fixed: '#6ffbbe'
  tertiary-fixed-dim: '#4edea3'
  on-tertiary-fixed: '#002113'
  on-tertiary-fixed-variant: '#005236'
  background: '#121317'
  on-background: '#e3e2e7'
  surface-variant: '#343439'
typography:
  headline-xl:
    fontFamily: Space Grotesk
    fontSize: 44px
    fontWeight: '700'
    lineHeight: 52px
    letterSpacing: -0.03em
  headline-xl-mobile:
    fontFamily: Space Grotesk
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Space Grotesk
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-lg-mobile:
    fontFamily: Space Grotesk
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Space Grotesk
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Space Grotesk
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 26px
    letterSpacing: 0em
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: -0.01em
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: 0em
  body-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
    letterSpacing: 0.01em
  label-lg:
    fontFamily: JetBrains Mono
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 18px
    letterSpacing: 0.02em
  label-md:
    fontFamily: JetBrains Mono
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.04em
  label-sm:
    fontFamily: JetBrains Mono
    fontSize: 10px
    fontWeight: '600'
    lineHeight: 14px
    letterSpacing: 0.08em
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-tablet: 1.25rem
  gutter-desktop: 1.5rem
  margin: 1rem
  margin-tablet: 2rem
  margin-desktop: 3rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2.5rem
---

## Brand & Style
The design system targets competitive esports athletes, performance-driven gamers, and hardware enthusiasts who demand precision and zero latency. The visual identity evokes an industrial-grade instrument: measured, authoritative, and tactically refined. It rejects chaotic RGB neon clutter, sci-fi tropes, and gratuitous gamification in favor of a sleek, dark-room cockpit aesthetic reminiscent of aerospace avionics and high-end audio mixing equipment.

The visual style blends **Tactile Industrial Precision** with **Sleek Matte Layering**:
- **Tactile feedback:** Interactions mimic low-travel mechanical microswitches and high-tolerance milled hardware surfaces with distinct, responsive depression states.
- **Controlled luminance:** Lighting is treated as physical emissive hardware LED indicators rather than decorative background glow.
- **Structural discipline:** Tightly calibrated hierarchies, fine technical borders with metallic bevels, and micro-metric telemetry layouts convey absolute hardware authority.

## Colors
The palette balances deep, light-absorbing non-reflective matte surfaces with targeted hardware LED illumination.

### Surface Hierarchy
- **Base Canvas (`#0D0E12`):** Primary background substrate mimicking textured polycarbonate controller housing.
- **Surface Level 1 (`#14161D`):** Primary container tier for module bays, panels, and large grouping cards.
- **Surface Level 2 (`#1C1F2A`):** Interactive utility containers, input slots, and recessed telemetry panels.
- **Surface Level 3 (`#262A38`):** Elevated tactile surfaces, active toggles, and floating hardware indicators.

### Accent & Emissive Tokens
- **Electric Violet (`#7C5CFC` primary, `#8E6FF7` hover/active):** Hardware control authority, active profiles, primary calls to action, and calibrated trigger zones.
- **Cool Cyan (`#38BDF8` secondary, `#0284C7` deep variant):** Peripheral linkages, secondary telemetry metrics, gyro overlays, and real-time polling data.

### System Telemetry & Status
- **Connected Emerald (`#10B981`):** Paired status, nominal battery levels (>20%), optimal input latency (<1ms).
- **Warning Amber (`#F59E0B`):** Low battery, packet jitter warnings, custom firmware calibration alerts.
- **Error Red (`#EF4444`):** Disconnection faults, stick drift threshold exceeded, hardware sync failures.
- **Disconnected Slate (`#64748B`):** Offline peripherals, unassigned button mapping slots, dormant modules.

## Typography
Typography reflects technical instrumentation. Headings feature **Space Grotesk** for clean, industrial geometric character without eccentric display flourishes. Functional content and body copy employ **Inter** for neutral legibility and sub-pixel rendering clarity on dark backdrops. 

Telemetry figures, key mappings, latency counters, and button badges utilize **JetBrains Mono** to enforce strict tabular alignment, monospaced data comparisons, and a machine-tool telemetry feel. All micro-labels (`label-sm`) default to uppercase tracking for rapid legibility under quick scanning.

## Layout & Spacing
The layout leverages a disciplined fluid grid derived from an 8pt base spatial unit:
- **Desktop (1200px+):** 12-column layout with 24px gutters and 48px canvas margins. Houses persistent device status rails alongside expansive dual-stick mapping visualizers.
- **Tablet (768px - 1199px):** 8-column layout with 20px gutters and 32px canvas margins. Controller layout and dynamic curve editors collapse into tabbed horizontal stacks.
- **Mobile (Under 768px):** 4-column layout with 16px gutters and 16px canvas margins. Optimized for one-hand companion utility with sticky bottom command anchors.

Spatial rhythm enforces structural density over excessive open space: operational information sits tight and orderly, reducing eye travel during active gameplay adjustments.

## Elevation & Depth
Depth is produced through structural surface stepping and metallic light deflection rather than muddy drop shadows.

- **Metallic Bevel Borders:** Surfaces feature a 1px composite outline: top/left borders pick up a subtle high-angle specular highlight (`rgba(255, 255, 255, 0.09)`), while bottom/right borders fall into shadow (`rgba(0, 0, 0, 0.65)`), generating a precision-machined edge.
- **Recessed Insets (Sub-tier Elevation):** Deadzone calibration wells and curve graphs use inner shadows (`inset 0 2px 4px rgba(0, 0, 0, 0.6)`) within `#14161D` backdrops to emulate physical recessed bays.
- **Emissive Proximity Bloom:** Interactive elements in active states project controlled edge illumination using pure primary or secondary tint: `0 0 12px rgba(124, 92, 252, 0.35)`, producing an authentic low-voltage LED glow without softening contrast boundaries.

## Shapes
The design system employs controlled, low-radius curvature (`roundedness: 1`). 
- Primary components, buttons, and telemetry cells feature `0.25rem` (4px) corner radii.
- Surface panels and diagnostic modules use `0.5rem` (8px, `rounded-lg`).
- Modular viewport windows and mapping modal dialogs use `0.75rem` (12px, `rounded-xl`).
- Physical circular controller face buttons (Cross, Circle, Square, Triangle), directional d-pad pivots, and thumbstick deadzone visualizers retain true mechanical `50%` circular geometries.

## Components

### Buttons & Trigger Controls
- **Primary Command:** Solid `#7C5CFC` fill, `#FFFFFF` text, with a 1px top highlight `rgba(255,255,255,0.2)`. On hover, transitions to `#8E6FF7` with an emissive violet boundary blur. On active press, visually displaces down 1px and increases inner depth (`inset 0 2px 3px rgba(0,0,0,0.5)`).
- **Secondary/Tactical:** Surface fill `#1C1F2A` with a 1px boundary of `#262A38`. On hover, the border shifts to `#38BDF8`. Text renders in crisp off-white (`#F1F5F9`).
- **PlayStation-Inspired Face Badges:** Circular or rounded-square mechanical micro-keys (`28px x 28px`) rendered on `#14161D` with high-contrast glyph etchings, active tactile ring glows, and low-travel travel displacement.

### Input Fields & Sliders
- **Numerical Calibration Steppers:** Built with `#14161D` background, recessed inner shadow, and mono-spaced `JetBrains Mono` values. Stepper arrows react with instant tactile depression states.
- **Response Curve Sliders:** Recessed `#0D0E12` tracks flanked by crisp tick lines. Active fill illuminates in `#7C5CFC` or `#38BDF8`. The thumb handle is a machined vertical pill with an emissive center line that expands slightly under drag interactions.

### Telemetry Cards & Module Bays
- **Container Structure:** Backed by `#14161D`, capped with a technical metadata bar (`label-sm` tracking, uppercase status icon, latency metrics). Outer border uses dual-tone metallic beveling.
- **Connected Status Pill:** Compact pill badge with a 6px pulsing emerald indicator dot (`#10B981`) paired with tabular latency readouts (e.g., `0.82 ms`).

### Checkboxes, Switches & Radio Tabs
- **Hardware Toggles:** Horizontal rocker style rather than soft mobile switches. Left/Right position shifts feature audible visual clicks, dark graphite bases, and discrete LED light pipes that switch from `#64748B` (Inactive) to `#7C5CFC` (Engaged).
- **Segmented Mode Selectors:** Integrated inline button groups with a sliding `#262A38` mechanical cap, delineating active controller profiles (e.g., `PROFILE: TOURNAMENT / DEFAULT / SNIPER`).

### Thumbstick Deadzone & Gyro Visualizers
- Vector-rendered crosshair grids utilizing `#1C1F2A` sub-lines, live coordinate readouts in `JetBrains Mono`, and active stick displacement markers traced with cool cyan `#38BDF8` vectors and semi-transparent radial boundaries.