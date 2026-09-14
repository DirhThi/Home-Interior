# Changelog

All notable changes to InteriorDesign3D. Format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

Nothing has shipped yet — `versionName 1.0`, `versionCode 1`, no git tags — so
everything below sits under Unreleased. When the first build goes out, cut a
`## [1.0] — YYYY-MM-DD` section here and open a fresh Unreleased above it.

## [Unreleased]

### Added

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
