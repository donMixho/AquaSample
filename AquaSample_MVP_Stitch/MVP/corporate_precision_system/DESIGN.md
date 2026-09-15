---
name: Corporate Precision System
colors:
  surface: '#fcf9f8'
  surface-dim: '#dcd9d9'
  surface-bright: '#fcf9f8'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f6f3f2'
  surface-container: '#f0eded'
  surface-container-high: '#eae7e7'
  surface-container-highest: '#e5e2e1'
  on-surface: '#1b1c1c'
  on-surface-variant: '#424751'
  inverse-surface: '#303030'
  inverse-on-surface: '#f3f0ef'
  outline: '#727782'
  outline-variant: '#c2c6d3'
  surface-tint: '#1f5eac'
  primary: '#003974'
  on-primary: '#ffffff'
  primary-container: '#00509e'
  on-primary-container: '#a5c5ff'
  inverse-primary: '#a9c7ff'
  secondary: '#0061a5'
  on-secondary: '#ffffff'
  secondary-container: '#4ba4fd'
  on-secondary-container: '#003965'
  tertiary: '#652800'
  on-tertiary: '#ffffff'
  tertiary-container: '#893900'
  on-tertiary-container: '#ffb38c'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#d6e3ff'
  primary-fixed-dim: '#a9c7ff'
  on-primary-fixed: '#001b3d'
  on-primary-fixed-variant: '#00468b'
  secondary-fixed: '#d2e4ff'
  secondary-fixed-dim: '#a0caff'
  on-secondary-fixed: '#001c37'
  on-secondary-fixed-variant: '#00497e'
  tertiary-fixed: '#ffdbcb'
  tertiary-fixed-dim: '#ffb691'
  on-tertiary-fixed: '#341100'
  on-tertiary-fixed-variant: '#783100'
  background: '#fcf9f8'
  on-background: '#1b1c1c'
  surface-variant: '#e5e2e1'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 44px
    letterSpacing: -0.02em
  display-lg-mobile:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
    letterSpacing: -0.01em
  headline-lg:
    fontFamily: Inter
    fontSize: 30px
    fontWeight: '700'
    lineHeight: 38px
    letterSpacing: -0.015em
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Inter
    fontSize: 22px
    fontWeight: '600'
    lineHeight: 28px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
    letterSpacing: 0em
  title-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 22px
    letterSpacing: 0em
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: 0em
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
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 18px
    letterSpacing: 0.01em
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.02em
  label-sm:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '500'
    lineHeight: 14px
    letterSpacing: 0.04em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-desktop: 1.5rem
  margin: 1rem
  margin-tablet: 1.5rem
  margin-desktop: 2.5rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
  space-2xl: 3rem
---

## Brand & Style

This design system delivers a structured, dependable, and high-clarity environment tailored for operational efficiency and professional workflows. The audience comprises professionals and enterprise users managing critical operations, assessments, or institutional protocols where cognitive overhead must be minimized.

The visual direction draws from **Corporate / Modern** principles with subtle tactile definition:
- Crisp information density with deliberate hierarchy.
- Reassuring structural stability through high-contrast structural framing (#00509E) and precise secondary accents (#4DA6FF).
- Utilitarian focus: elements are balanced, legible, and unadorned by decorative visual noise. Interfaces project authority, trust, speed, and validation clarity.

## Colors

The palette establishes a strict functional role for every token, eliminating ambiguity in high-consequence corporate operations.

### Roles & Semantic Application
- **Primary (`#00509E`)**: Used for top navigation bars, structural master headers, primary action buttons (solid CTA), and core navigational states. Conveys authority and bedrock reliability.
- **Secondary (`#4DA6FF`)**: Used for active interactive icons, floating auxiliary controls, active pill tags, focus outlines, and selected borders. It provides vibrant feedback against dark and light tones.
- **Background (`#F5F5F5`)**: Applied to general screen canvasses, layout backdrops, and viewports.
- **Surface / Card (`#FFFFFF`)**: Pure white reserved for cards, data tables, modals, and container elements to achieve clear elevation above `#F5F5F5`.
- **Text & Neutral (`#212121`)**: Primary body typography, form input labels, data values, and high-legibility section titles. Subdued text utilizes `#616161` (neutral-60) and `#9E9E9E` for disabled states.
- **Success / Validated (`#2E7D32`)**: Verification badges, completed audit flags, confirmation notices, and validated metric readouts.
- **Alert / Critical Error (`#FF6D2D`)**: Unresolved tasks, validation warnings, missing requirements, and active warning ribbons.

## Typography

Inter serves as the single unified typeface across display, text, and data-density tiers. It offers neutral clarity, tall x-height, and open apertures essential for high-throughput corporate scanning.

### Hierarchy Guidelines
- Titles and high-level headers use heavier weights (`600` and `700`) paired with subtle negative letter spacing for crisp horizontal rhythm.
- Body copy relies on strict tabular alignment and uniform line-height to ensure that long-form notes, data values, and descriptions do not cause visual drift across cards.
- Form field labels always utilize `label-md` or `label-lg` with `fontWeight: 600` in `#212121` to anchor inputs unambiguously.

## Layout & Spacing

The layout is built upon an 8pt spatial grid with a standard 12-column responsive layout engine on desktop, collapsing to 8 columns on tablet and 4 columns on mobile.

### Breakpoints & Canvas Bounds
- **Mobile (`< 768px`)**: Single-column vertical stack with `margin: 1rem` and `gutter: 1rem`. Cards span full width between margins.
- **Tablet (`768px - 1024px`)**: Multi-column responsive layout with `margin-tablet: 1.5rem`. Side panels and auxiliary toolbars shift into collapsibles or bottom drawers.
- **Desktop (`> 1024px`)**: Max container width capped at `1440px` centered with auto-margins. Lateral navigation fixed at 280px or collapsed to 72px rail.

Content cards and dashboards maintain consistent internal padding using `space-lg` (24px) for desktop containers and `space-md` (16px) for nested lists or mobile screens.

## Elevation & Depth

Visual hierarchy uses a dual strategy: crisp tonal contrast against the `#F5F5F5` canvas and ambient, low-spread drop shadows that simulate real-world card planes without visual heaviness.

### Tonal Tiers
- **Canvas (Level 0)**: `#F5F5F5` base plane.
- **Surface / Cards (Level 1)**: `#FFFFFF` with `box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05), 0 4px 12px rgba(0, 0, 0, 0.04)`. Outlines are kept soft: `1px solid rgba(0, 0, 0, 0.06)`.
- **Raised / Hovered Cards (Level 2)**: `#FFFFFF` with `box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.06), 0 10px 20px -2px rgba(0, 80, 158, 0.08)`.
- **Top Navigation Bar**: `#00509E` structural bar anchoring the screen header; elevated via `0 2px 8px rgba(0, 32, 64, 0.2)`.
- **Modals & Overlays (Level 3)**: `#FFFFFF` with `box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)` and a `rgba(33, 33, 33, 0.5)` backdrop wash.

## Shapes

The design uses balanced, rounded geometry (`roundedness: 2`) to humanize the interface while preserving corporate structure.

- **Standard Elements (inputs, buttons, list cells)**: `0.5rem` (8px).
- **Cards, Modules, and Modals (`rounded-lg` / `rounded-xl`)**: Primary content cards take `rounded-lg` (`1rem` / 16px) or `rounded-xl` (`1.5rem` / 24px) on featured dashboard summaries.
- **Chips, Pills, and Status Badges**: `9999px` (fully rounded pill geometry) to distinctively contrast against rectilinear data containers.

## Components

### Buttons
- **Primary CTA**: Background `#00509E`, text `#FFFFFF`, border `none`, border-radius `0.5rem`, height `40px` (or `48px` for prominent mobile actions). Active/Hover state deepens to `#003D7A`. Focused with a 2px offset ring in `#4DA6FF`.
- **Secondary Button**: Background `transparent` or `#FFFFFF`, border `1.5px solid #4DA6FF`, text `#00509E` or `#4DA6FF`. On hover: subtle `#4DA6FF1A` (10% tint).
- **Destructive / Alert Action**: Background `#FF6D2D`, text `#FFFFFF`.

### Cards
- Pure white (`#FFFFFF`) background against the `#F5F5F5` canvas.
- Border radius: `1rem` (`rounded-lg`).
- Border: `1px solid rgba(0, 0, 0, 0.06)`.
- Internal padding: `1.5rem` (`space-lg`).
- Header actions, badges, and validation states are docked to the top-right corner.

### Chips & Badges
- **Status Validated / Success**: Background `#E8F5E9`, text `#2E7D32`, optional 1px border `#2E7D3233`.
- **Status Alert / Pending Critical**: Background `#FFF3E0`, text `#FF6D2D`, border `1px solid #FF6D2D4D`.
- **Active Interactive Tag**: Background `#EBF5FF`, text `#00509E`, border `1px solid #4DA6FF`.

### Input Fields & Controls
- **Inputs**: Height `42px`, white background, `1px solid #D1D5DB`, border-radius `0.5rem`. Font color `#212121`.
- **Focused Input**: Border color `#4DA6FF`, box-shadow ring `0 0 0 3px rgba(77, 166, 255, 0.25)`.
- **Error State**: Border `#FF6D2D`, helper message in `#FF6D2D`.
- **Checkboxes & Radios**: Checked background `#00509E`, tick/dot `#FFFFFF`. Focus ring `#4DA6FF`.

### App Bar & Header
- Master top bar rendered in `#00509E`, with navigation labels, identity logo, and profile controls in `#FFFFFF` or translucent white overlays (`rgba(255, 255, 255, 0.8)`).
- Sub-navigation or tab bar stays cleanly anchored with `#4DA6FF` 3px bottom indicator for active tabs.