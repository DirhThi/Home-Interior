# Changelog

All notable changes to InteriorDesign3D. Format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

Nothing has shipped yet — `versionName 1.0`, `versionCode 1`, no git tags — so
everything below sits under Unreleased. When the first build goes out, cut a
`## [1.0] — YYYY-MM-DD` section here and open a fresh Unreleased above it.

## [Unreleased]

### Added

- **The Home card reads the plan.** Each row shows room count, area, storeys and
  ceiling height, all derived from `floorPlanJson`; a room with nothing drawn says
  so instead of faking a size. The thumbnail is an axonometric doll's house drawn
  straight from the plan — floors, walls stood up, doors and windows punched
  through, the furniture actually in the room, and storeys stacked. Walls facing
  the viewer are dropped so you can see in, and walls carry a lit top edge while
  the open sides carry a floor slab edge — without those the corner flips inside
  out as a Necker cube. Canvas only: no 3D engine, no cached bitmap, and it cannot
  show a stale picture.
- **Pitched roofs, and the Thai multi-mass roof.** Three shapes in the Outside tab:
  **Flat** (also the roof terrace), **Single hip** — one roof over the whole
  footprint, which on an L covers the notch as a porch — and **Thai**, which splits
  the outline into rectangular masses and gives each its own hip, stepped down from
  the main one. Pitch, eaves and an **end taper** are all adjustable, and the taper
  is what turns a hip into a gable: 100% pulls the ridge fully in (chóp), 0% runs it
  out to the wall so the end plane stands up as a triangle (tam giác). The panel
  sits at the bottom of the exterior view, next to the roof it changes, and says what
  each pick will do to *this* plan — how many masses it will use, or that a single hip
  will overhang the notch — rather than letting the 3D be a surprise.
- **15 new materials, all real scans.** Terracotta and aged-clay roof tiles, slate,
  corrugated steel, real grass, gravel and bare earth outside; exposed and painted
  brick, wood panelling and wall tile inside; two more floors. All ambientCG CC0,
  fetched and converted by [`tools/make_material.py`](tools/make_material.py), which
  rebuilds the `mat_*.glb` pipeline that had only ever existed in a scratch folder.
- **Exterior finishes.** Roof and ground presets in a new Outside tab of the
  surfaces sheet, stored on the plan. No new assets: every one is a tint over a
  material the build already shipped.
- **Exterior view.** A third editor mode that steps outside: every storey built, no
  walls hidden, a flat roof over each storey with the one above cut out of it, a
  paved plot and the ground the house stands on. `FloorPlan.outlineRings(level)`
  walks the edges exactly one room uses to get the storey's outline, so an L-shaped
  or split plan roofs correctly — and a courtyard comes back as a ring wound the
  other way, which is how callers can tell it apart.
- **Balconies.** A fourth placement tool: tap a wall and a slab hangs off the outside
  of it, railed on the three open sides, with width and projection editable. It only
  takes on a wall a single room uses, because one on an interior wall would hang into
  the next room. Pinned to the node pair like a door, so reshaping the plan carries it.
- **Guard rails where a floor opens or ends.** A parapet round every flat roof, so a
  terrace reads as somewhere you could stand, and a rail round the stairwell opening
  upstairs on every side but the one you step out of — `Stair.wellGuards` works that
  side out from where the last run actually ends.
- **Handrails on every flight.** A raking rail and balusters down both sides of each
  run, and a guard on the landing sides no run arrives at, all derived from the same
  nosing line so they meet whatever the pitch. `buildBox` gained a `pitchDeg` that
  tilts a box about its own length — a pitched roof will want the same thing.
- **Stair proportions per shape.** Each shape carries its own default depth, so a
  new flight gets a ~25 cm tread instead of 17.5 cm; `legCm` and `wellCm` are
  editable in the panel; and the panel warns when a flight is squeezed shallower
  than 22 cm to fit the room above.
- **Stairs**, straight, L-shaped and U-shaped. A flight is described as straight
  runs plus flat landings, cuts its own rectangular opening in the slab above,
  and is scaled down to fit inside one room on the storey above. Steps are shared
  across the runs by length; the landing sits level with the last step feeding
  it. Materials configurable separately from walls and floors.
  (`7e9294d`, `dbf5500`)
- **Two storeys.** `FloorPlan` carries `roomLevels`, a level switcher drives both
  editors, the 3D view stacks storeys with a floor slab between them, and the 2D
  plan traces the storey below as a dashed guide. Wall and floor finish is stored
  per storey. (`de9a022`, `dbf5500`)
- **Sample floor plans** — studio, two-bedroom, L-shaped, townhouse — loadable
  from Home. (`0934bae`)
- **Doors and windows are proper objects**: selectable and editable from the 2D
  plan *and* by tapping them in the 3D view, with show/hide leaf and open-swing
  options, and a wall cut derived from the fitted model instead of the slider.
  (`9e86050`)
- **Wall lengths on the floor plan**, de-duplicated per edge and pushed clear of
  the wall line. (`b11e412`)
- **Per-room ceiling height**, with a control in the surfaces sheet. (`dcdd7de`)
- **Number input dialog** for exact size, rotation and height values.
  (`f5294b6`, `5917970`)
- **Undoable delete** on Home. (`96e196b`)

### Changed

- **Rebuilt as one package per screen** on the A045_ByteClean pattern —
  `Screen` / `ViewModel` / `state` / `view`. ViewModels own the database;
  composables never touch it. (`0759edb`, `96e196b`, `dbc67a9`)
- **One wall per shared edge.** Adjacent rooms used to each build their own wall,
  which doubled party walls and z-fought. Openings are now keyed by node pair, so
  a door on a shared wall opens both rooms. (`9e86050`)
- **Clay-on-white palette.** Pure white ground, clay primary, teal accent;
  surfaces separate with 1 dp hairline borders instead of by value, and the
  contrast comes from the near-black 3D stage. (`812dcf3`, `d98846b`, `6ce036a`)
- **Furniture panel reformatted as tabs** with a single horizontally scrolling
  row, and the sheet tiles shrunk. (`7b09f3b`, `82138c8`)
- **Furniture catalog moved to the data layer**, labels translated to English,
  surface picks actually persisted. (`c2e2edf`)
- **Database pinned at version 1** with `fallbackToDestructiveMigration()`. This
  is a single-developer app with no installed users, so a schema change wipes
  instead of carrying a migration. (`492a513`)

### Fixed

- **The floor-plan tool row wrapped its last label to two lines** instead of admitting
  four chips no longer fit a phone. It scrolls sideways now.
- **A roof tint could not make a dark tile red.** `baseColorFactor` multiplies the
  texture, so it only ever darkens — terracotta had to come from a scan that is
  already terracotta, not from tinting slate.
- **Stepping inside after looking round the outside showed a black screen.** The
  orbit carried across: a low, level angle is natural outside and under the floor
  inside. Each mode now parks its own viewpoint and gets it back.
- **Pitched roofs floated above the walls.** Eaves were placed at the storey above's
  floor level, which is right for a flat slab and a slab's thickness too high for a
  pitched one; and the Thai masses were stepped *up* from there, lifting the smaller
  roofs clear of their own walls. Every mass now rests on the wall head, and ridge
  heights differ from mass width alone, which is where the difference belongs.
- **The plan was only saved by Save or by entering Design mode.** Draw a room and
  press Back and it was gone; the same went for every wall, floor, stair and roof
  pick, which all live on the plan now. It auto-saves on the same 400 ms debounce
  furniture already used.
- **Every Home card showed the same frozen values** — `380 × 520 cm`, `Hardwood`
  and one fixed pair of colours — because `DesignRoom` kept summary columns that
  stopped being written once `FloorPlan` became the source of truth.
- **A schema change crashed the app on launch** rather than wiping the database.
  `fallbackToDestructiveMigration()` only runs when `@Database(version)` moves; at
  an unchanged version Room compares a schema hash and throws. The version is now
  bumped on every schema change.
- Number dialog's Apply used the first value the field ever held, not what was
  typed. (`5917970`)
- Toolbar labels clipped to `U n` / `Cl e`. (`e9a4984`)
- A white inset strip along the bottom of the designer. (`2d32f1e`)
- Hardcoded dark surface hexes in `Theme.kt` tinting one mode and surviving
  palette swaps. (`c6d96ca`)
- Corner posts stepping at outside corners and protruding at T-junctions.
  (`9e86050`)
- Window frames not matching their wall cut. (`9e86050`)
- Level-1 wall tops coplanar with the level-2 floor, which z-fought and let walls
  poke through. (`de9a022`)
- Ragged stairwell holes on L and U flights, and corner steps fanning out with
  gaps where two runs met. (`dbf5500`)

### Removed

- Nine dead columns from `DesignRoom` (`widthCm`, `lengthCm`, `wallColor`,
  `floorColor`, `floorMaterial`, `thumbnailPath`, `wallPointsJson`,
  `wallPresetIdx`, `floorPresetIdx`), the `FloorMaterial` enum and both Room
  `TypeConverter`s that existed only to serve them.
- The legacy Canvas renderer (`Room3DViewport`), its toggle and the duplicate
  zoom buttons. Filament is the only renderer now, with no fallback. (`7c67454`)
- `ColorPickerScreen` and its route — it never wrote to the database. Folded into
  the surfaces sheet. (`dbc67a9`)
- Seven declared dependencies no source file referenced: ARCore, CameraX, Coil,
  colorpicker-compose, accompanist, Gson, DataStore. (`7a9d0e5`)
- Every Room `Migration`. (`492a513`)

---

## Before this changelog

History up to 2026-09-11 — the initial project, the first 3D view, the Filament
viewport and the furniture catalogue — is only in `git log`. It was not written
up, and backfilling it now would be guesswork.

---

## How to keep this file

One entry per user-visible change, written in the same voice as the commit
titles: lower-case, plain, what changed rather than which class moved. Group
under **Added / Changed / Fixed / Removed**, newest at the top of its group.
Reference the short hash so the diff is one command away.

Skip: refactors nobody can see, formatting, doc-only commits.
