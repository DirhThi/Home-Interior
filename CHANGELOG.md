# Changelog

All notable changes to InteriorDesign3D. Format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

Nothing has shipped yet — `versionName 1.0`, `versionCode 1`, no git tags — so
everything below sits under Unreleased. When the first build goes out, cut a
`## [1.0] — YYYY-MM-DD` section here and open a fresh Unreleased above it.

## [Unreleased]

### Added

- **A first-open flow: splash, then a three-page intro.** The splash is a brand moment while Koin
  and the database come up, and it decides where to go; the intro covers the three things the app
  does — draw the plan, furnish it in 3D, step outside — and is shown once, gated on a
  SharedPreferences flag. Skip is reachable from every page: an intro nobody can leave is a toll gate.

- **Real backdrop blur where there is a backdrop to blur.** `io.github.kyant0:backdrop` now backs
  `Modifier.glass`: with a `LocalGlassBackdrop` and `RenderEffect` (API 31+) the pane blurs what is
  behind it, otherwise it falls back to the tint-and-rim it always had. Only the 2D plan canvas
  supplies one — the Filament viewport is a `SurfaceView` composited on its own layer, so its pixels
  never reach this draw pass at any API level, and the main tab screens are flat enough that a blur
  would show nothing.

- **Three top-level destinations, on a floating tab bar.** Home / Projects / Settings. Home is two
  cards — start a project, or explore furniture — because those are the only two things you can do
  from a standing start. Projects is the saved room list. Settings is a screen now rather than a
  sheet, with room for the options that will accumulate.
- **A catalogue you can browse without a room open**, and an item screen that renders **just that
  model in 3D**, on a turntable, framed from the camera's own field of view so a tall chair and a
  wide sofa both fit. "Add to a room" drops it into a project; with no projects yet it offers to
  make one first. It is a separate, much smaller Filament scene — `FilamentRoomViewport` exists to
  build walls, floors and roofs from a plan, and none of that applies to one glTF sitting on nothing.
- **Navigation3 and Koin**, replacing Navigation-Compose and the hand-rolled `rememberScreenViewModel`
  DI stand-in. `Dest` is the root back stack; `DestMain` is the tabs' own. The designer and the
  catalogue are pushed over the shell rather than living in tabs, so they get the whole screen —
  stacking a second bar under the designer's own mode bar would be worse than having none.

- **A floating tab bar for the three editor modes.** Plan / Interior / Exterior were
  the app's real top-level navigation but lived as two unlabelled icon buttons in the
  top bar, so there was no way to tell where you were or what else existed. They are
  now a glass pill at the bottom of the designer, icon and label, with the current one
  filled and a spring indicator. Interior and Exterior stay disabled until a room is
  drawn. `DesignerState` gained one `onModeChange(mode)` in place of
  `onEnterDesign` / `onEditFloorPlan` / `onToggleExterior`, and it flushes whatever the
  mode being left owns.
- **Glass chrome.** `ui/theme/Glass.kt` — tint, a rim lit from the top and a soft shadow, as
  a `GlassTokens` set plus `Modifier.glass`. This is the fallback path; the entry above covers
  the case where there is a real backdrop to blur.

  The edge is what carries it: the rim runs bright along the top, fades out by the
  middle and returns half-strength at the bottom — the way light catches both edges of
  a real pane — with a sheen washed across the top. Transparency alone only pays off
  over a textured backdrop, and over a viewport the backdrop is usually flat, while it
  costs legibility everywhere else.
- **A theme switch in Settings.** Auto / Light / Dark, kept in SharedPreferences and held
  in Compose state so the whole tree repaints the moment it changes. A plain on/off toggle
  could not express "follow the system", so it is a three-way segmented control. It started as a sheet on Home and is now a tab of its own.
- **One control vocabulary, everywhere.** Three different Material components were being used
  to say "pick one of these" — `FilterChip` rows in the panels, `ScrollableTabRow` in the
  sheets, and a hand-rolled row in Settings. They are now one `SegmentedPills` for small fixed
  sets and one `ChoiceChip` for rows that scroll, with `ValueChip` for a number you tap to edit
  and `PanelIconButton` for close/delete. Controls that sit *on* a glass pane are tonal rather
  than glass: glass inside glass goes muddy and flattens the hierarchy.
- **One palette for the whole app.** Light means light everywhere, dark means dark
  everywhere — including the 3D viewport, whose background used to be a fixed dark brown
  regardless of theme. System bar icons follow the in-app choice, not the OS setting.

### Changed

- **The toolchain moved a long way**: Gradle 8.0 → 9.5, AGP 8.1.2 → 9.3.1, Kotlin 1.9.20 → 2.4.10,
  Compose BOM 2024.06 → 2026.08, compileSdk/targetSdk 36 → 37, Room 2.6.1 → 2.8.5, kapt → KSP, and
  **minSdk 26 → 31**. AGP 9 carries Kotlin itself, so the `kotlin.android` plugin is gone and its
  options moved to `kotlin { compilerOptions { } }`. Filament 1.49.1 came through unchanged.
- The room list moved from `ui/screen/home` to `ui/screen/project`; `ui/screen/home` is now the
  landing.
- **Actions cut from four surfaces to two.** The top bar went from five controls to a
  round back button: mode switching moved to the tab bar, surfaces to the action
  clusters, and **Save is gone** because the plan auto-saves shortly after the last edit. `FloorPlanToolbar` — a full-width opaque bar holding four chips and four
  buttons, taking permanent height off the canvas — is deleted; in its place are
  floating clusters at the vertical centre of each edge, with the four placement tools
  folded into one speed-dial that wears the armed tool's own icon while collapsed.
  Buttons now carry three weights (primary / selected / plain) so a toggle like
  snap-to-grid is no longer the loudest control on the screen.
- **Control panels float.** All six are inset, fully rounded glass sitting above the tab
  bar instead of full-width slabs pinned to the bottom. They no longer collide with the
  primary action, so the five `selectedX == null` conditions that used to hide the FAB
  are gone.
- **Home leads with the content.** The title scrolls with the list rather than sitting in
  a pinned `TopAppBar`, and the room cards got a larger thumbnail and more air.

### Fixed

- **The "New room" button sat under the tab bar.** The Scaffold places its floating action button at
  the bottom of the window and knows nothing about a tab bar that floats over the content, so the two
  overlapped on the Projects tab.

- **The plan editor framed every plan as if it were 6 m wide.** `scale = width * 0.8 / 600`
  was a constant, so anything larger opened half off-screen — a 11.6 m townhouse showed
  about two-thirds of itself. It now measures the plan's own bounds and centres them,
  leaving room for the dimension labels drawn outside the polygon. It runs from an effect
  rather than `onSizeChanged` because the plan arrives from the database a frame or two
  after first layout.
- **Two hint chips fought for the top of the screen.** `FloorPlanCanvas` drew its own
  room-count pill with no status-bar inset, which rode up into the status bar once the
  canvas went full-bleed. It is deleted; the designer's single hint chip absorbed the
  area readout.
- **Auto-save ran a deep comparison of the whole plan on every frame of a drag.**
  `snapshotFlow { floorPlan }` compares with `equals`, and both the plan and the furniture list
  are structures of lists, so each frame walked all of it. They are now watched through
  revision counters bumped in the setters — an Int compare — and the coalescing delay dropped
  from 400 ms to 150 ms. The delay stays because it is what folds a whole drag into a single
  write; without it a gesture would write the plan to SQLite sixty times a second.
- **Back left the designer while something was still selected.** The handler covered furniture
  and openings only, so Back with a stair, balcony or wall selected — or a placement tool armed —
  dropped you out of the screen instead of clearing the selection.
- **The room card was tinted red at rest.** `SwipeToDismissBox` painted its delete background
  under every card, which showed through once the cards became glass. It is only drawn while
  the card is actually being swiped.
- **Back could drop the last edit.** Auto-save coalesces and there is no Save
  button to fall back on any more, so `onBack` now flushes the plan and the furniture
  before it pops.

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
- **A roof that covers the terrace.** One toggle in the exterior panel carries the top
  roof out to the storey below's outline, so a set-back upper floor stops leaving its
  terrace open to the sky and becomes a covered loggia — the deep front porch of a mái
  Thái house. Columns go in wherever the overhang has no wall under it, every four
  metres along the edge.
- **Walls you can open up.** Tap a wall in the plan and it becomes selectable — **Full**
  as before, **Half** at counter height for a kitchen divider (open above, joinery
  dropped, capped with a coping), or **Open**, which builds no wall at all and stands a
  column at each end to carry the storey above. Columns are the catalogue's own
  `q_column_*` models, so they are the same object you can already place by hand, and
  the round/square/short choice is in the panel. Stored per node pair like a door, so
  reshaping the plan carries it; a wall back to Full stores nothing.
- **A balcony can span a whole frontage** — the width cap went from 6 m to 20 m.
- **Balconies.** A fourth placement tool: tap a wall and a slab hangs off the outside
  of it, railed on the three open sides, with width and projection editable. It only
  takes on a wall a single room uses, because one on an interior wall would hang into
  the next room. Pinned to the node pair like a door, so reshaping the plan carries it.
  Put one on an upper storey with a door beside it and the door opens onto it.
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

- **The ground ran out before the camera did.** The lawn was sized from the house —
  16 m for a plan the camera can back 60 m away from — so zooming out always found the
  edge of the world. It is sized from the orbit's own reach now, which costs nothing:
  the ground is one quad whatever its size.
- **A balcony slab was 10 cm bigger than its own railing on every side**, and poked back
  through the wall it hangs on. `buildFloorMesh` always grew the polygon by a wall
  thickness — right for a room floor, which has to run under its walls, wrong for
  everything else. It is a parameter now, and the roof's eaves are exactly the number
  the panel shows rather than that plus 10 cm.
- **The 3D view built one draw call per baluster.** A two-storey plan with one U flight
  came to 177 renderables inside and 282 outside, most of them posts. Boxes that share a
  material are batched into one mesh with the transform baked into the vertices: **80 and
  116**. Posts also sit every 40 cm now instead of on every tread.
- **The viewport's rebuild check rebuilt a multi-kilobyte string on every recomposition**,
  concatenating every polygon, opening, stair and surface. It hashes instead — which also
  fixes editing a balcony not redrawing, since balconies were never in that string.
- **Home re-parsed every room's plan whenever any furniture moved**, and the isometric
  thumbnail redid its edge census inside the draw lambda, on every scroll frame. Both are
  cached on the thing they actually depend on.
- **`roofMassCount` and `roofCanPitch` re-ran the whole outline walk on every
  recomposition** of the roof panel. Both are `derivedStateOf` now.
- **A flat roof's surface sat a slab's thickness above the storey it belongs to.** A
  balcony hung off an upper wall was then buried in its own roof, with only the top of
  its rail showing. The slab now hangs below the walking surface instead of standing
  on it, so a terrace and the storey above it share one level.
- **Tapping a wall in the plan dropped a stray node** instead of doing anything useful.
  It selects the wall now.
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
