# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

InteriorDesign3D (`com.interiordesign3d`) — a Kotlin + Jetpack Compose interior design app: draw a multi-room floor plan, place furniture, and view it in a pseudo-3D viewport.

## Docs

Longer-form notes live in `docs/`, not here:

- [`docs/status.md`](docs/status.md) — what the app does today, how well it has actually been verified, what is fragile
- [`docs/backlog.md`](docs/backlog.md) — everything still open, and why two roadmap tasks should be dropped
- [`docs/stairs.md`](docs/stairs.md) — how a flight is described, plus the tread-depth and handrail gaps
- [`docs/exterior.md`](docs/exterior.md) — how ground, roofs, parapets and balconies were built, and what was left out

## Working agreement

Two rules that otherwise live only in one machine's session memory, so they are written down here instead:

- **Never run `git commit` unprompted.** Finish the code, get `./gradlew assembleDebug` green, report — and leave the change in the working tree. The developer commits, or asks for it explicitly.
- **When asked to commit: one short line saying what the change does.** No body, no bullet list, and no `Co-Authored-By` or other attribution trailer. Same for PR descriptions.

On-device verification is theirs, not yours — build to prove it compiles, then hand off.

## Changelog — required

**Every code change that a user could notice gets an entry in [`CHANGELOG.md`](CHANGELOG.md), in the same commit as the code.** Add it under `## [Unreleased]` in the right group (Added / Changed / Fixed / Removed), one line, same voice as the commit titles, with the short hash once the commit exists. Skip only invisible refactors, formatting and doc-only commits. The file's own "How to keep this file" section is the reference.

## Build Commands

```bash
# Build debug APK — there is no CI, this is the only thing that checks a build
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run unit tests / instrumented tests (NOTE: no test files exist yet — see below)
./gradlew test
./gradlew connectedAndroidTest

# Clean build
./gradlew clean
```

**Toolchain:** Gradle 9.5, AGP 9.3.1, Kotlin 2.4.10, Compose BOM 2026.08.00, **JDK 17**. Room uses **KSP**. Android SDK API 31–37 required.
AGP 9 carries Kotlin itself — there is no `org.jetbrains.kotlin.android` plugin, and compiler options live in a top-level `kotlin { compilerOptions { } }` block, not `kotlinOptions`.
`local.properties` is gitignored — create it locally with `sdk.dir=<path>`.

## Architecture

**Pattern:** Single-module app, one package per screen, modelled on the A045_ByteClean base:

```
ui/screen/<feature>/
├── <Feature>Screen.kt       // thin: builds the ViewModel, wires snackbar + back, renders Content
├── <Feature>ViewModel.kt    // : BaseViewModel — owns DB access; screenState = object : <Feature>State() { override fun onX() }
├── state/<Feature>State.kt  // @Stable, mutableStateOf fields + `open fun` callbacks defaulting to no-op
└── view/<Feature>Content.kt // pure UI; takes only the State, so it stays previewable
```

`common/base/` holds `BaseScreen` (Scaffold + loading + snackbar), `BaseViewModel` (takes the `NavBackStack`, one-shot `UiMessage`s), `BaseScreenState` and `CollectMessages`.

**DI is Koin.** `di/Modules.kt` registers every ViewModel with `viewModelOf`; `InteriorDesignApp` (in `MainActivity.kt`) calls `startKoin` and `AppPrefs.init`. Screens resolve with `koinViewModel(parameters = { parametersOf(backStack, …) })`. The old `Navigator` interface and `rememberScreenViewModel` stand-in are gone.

**ViewModels own the database.** Composables never touch `AppDatabase` — that is the rule the refactor established; don't reintroduce direct DB reads in a `@Composable`.

There is no AR and no camera capture. ARCore, CameraX, Coil, colorpicker-compose, accompanist, Gson and DataStore were all declared but referenced by zero files, and have been removed from `build.gradle`. The real 3D view **is** Google Filament (`FilamentRoomViewport.kt`). The README used to claim otherwise and was rewritten to match the code — keep it that way.

### 3D viewport (`ui/screen/designer/view/viewport/FilamentRoomViewport.kt`)

- `RoomScene` owns Engine/Scene/View, a `Choreographer` loop that only renders when `dirty`, orbit camera (drag = azimuth/elevation, pinch = zoom), lifecycle pause/resume.
- **Walls, baseboards, corner posts, door lintels, window sill/header, stairs and handrails are procedural boxes** (`buildBox`, whose `pitchDeg` tilts a box about its own length — that is how a raking handrail is built, and what a sloped roof would use) with UVs in metres / `tileM`; the floor is a triangulated polygon (`buildFloorMesh`). Materials come from `assets/models/mat_*.glb` (a textured quad; the asset stays out of the scene, only its `MaterialInstance` is used via `materialOf(slot, model, colorHex)`; tint = `baseColorFactor`). Textures are ambientCG CC0 JPEGs (512 px) embedded in the GLB.
- Door/window openings: a `q_door*` prop dragged within 20 cm of a wall becomes a real `WallOpening` (`tryDropDoorOnWall` → `onDropOpening`); the leaf model is `WallOpening.style`. Windows get a `q_window_small/large` frame.
- Furniture: gltfio assets from `assets/models/<key>.glb`, scaled by `PlacedFurniture.scale × CatalogItem.unitScale` (Quaternius packs are authored at 2× → `unitScale 0.5`). Placement rules live in `resolveDrag` (push out of furniture, snap ≤ 8 cm to walls, stacking on `surface` items).

### Furniture catalog (`data/catalog/FurnitureCatalog.kt`)

`FURNITURE_CATALOG` (groups of `CatalogItem`) is the single source of truth: `key` = GLB + WebP preview file name and `PlacedFurniture.furnitureId`; `mount` (FLOOR/WALL/CEILING), `surface`, `unitScale`. All models are **Quaternius CC0** (`q_` Ultimate House Interior Pack incl. 26 converted from the OBJ release, `qf_` Furniture Pack). `WALL_PRESETS` / `FLOOR_PRESETS` map to `mat_*` materials. `MODEL_CREDITS` is shown from the Home ⓘ dialog. Items whose key is not in the catalog are dropped on load.

Asset pipeline (no Blender): Python in the session scratchpad converted OBJ→GLB, rendered flat-shaded previews, and wrote `mat_*.glb` from JPEGs; keep previews as **WebP**.

### Navigation (`ui/navigation/`)

**Navigation3** (`NavDisplay` / `NavKey` / `rememberNavBackStack`), not Nav2. `Dest` is the root back stack — `ScrMain`, `ScrDesigner`, `ScrCatalogue`, `ScrCatalogueItem`; `DestMain` is the tabs' own stack inside `MainScreen` — `ScrHome`, `ScrProject`, `ScrSettings`.

The designer and the catalogue are **pushed over the shell**, not tabs: they take the whole screen, and the designer already carries its own Plan/Interior/Exterior bar. `NavigationUtil` holds the back-stack edits, every one of which refuses to empty the stack (`NavDisplay` crashes on an empty one).
The old `color_picker/{roomId}` route and `ColorPickerScreen` are **gone** — they never wrote to the database (Apply just popped the back stack). Wall colour, floor material and palettes now live in `SurfaceSheet` inside the designer and persist for real.

### First-open funnel (`ui/screen/{language,onboard,select}/`)

Splash → Language → Onboard → Select → Main, built on the A045_ByteClean pattern so a real ad SDK
can be dropped in later without restructuring these screens.

- **Base/Alt is not an A/B test.** Language and Select each have a `normal/XScreen.kt` and an
  `alternative/XAltScreen.kt` rendering the *same* `XContent`; picking anything on the base screen
  pushes the Alt one on a private child `NavDisplay` (`XNavigation.kt`, `DestX` in `ui/navigation/`)
  with every transition set to `EnterTransition.None` — so the user never sees it happen. The point
  is a second screen identity (`TrackingScreen`) and a second ad slot (`AdSlot`) per step, not a
  second UI. Settings' language picker (`language/setting/`) is a separate, plain single screen —
  it never needed either.
- **Onboard is slot-driven, not a fixed page array.** `OnboardConfig.DEFAULT` (`data/onboard/`) is a
  local list today; `OnboardViewModel` turns it into `OnboardSlot.Page`/`OnboardSlot.Ad`, and the
  pager in `OnboardContent.kt` just renders whatever it gets. Inserting an ad page later is a config
  change, not a pager change.
- **`ads/AdsManager.kt` and `ads/TrackingEvent.kt` are the only two files a real SDK touches.** Every
  screen calls into one of them (or the `AdSlot`/`TrackingScreen`/`GeneralBackHandler` composables in
  `ui/properties/AdsUi.kt`) instead of doing anything ad/analytics-shaped itself; every call is a
  no-op today.

### Glass (`ui/theme/Glass.kt`)

`Modifier.glass` has two paths. With a `LocalGlassBackdrop` and `RenderEffect` (API 31+) it uses `io.github.kyant0:backdrop` for a **real** backdrop blur; otherwise it falls back to tint, sheen and a lit rim. A screen supplies a backdrop only where the content behind is Compose — the 2D plan canvas does, the Filament viewport cannot: a `SurfaceView` is composited on its own layer and its pixels never reach this draw pass, at any API level.

### Data layer (`data/`)

- `models/Models.kt` — All data classes & enums: `FurnitureItem`, `DesignRoom` (entity, table `rooms`), `PlacedFurniture` (entity, table `placed_furniture`, `roomId` FK), `WallPoint`, `WallOpening`, `FloorPlan`, `ColorPalette`; enums `FurnitureCategory`, `FloorMaterial`, `DesignStyle`, `OpeningType`.
- `repository/Database.kt` — `AppDatabase` (DB name `interior_design_db`, **version 2**, destructive fallback), DAOs `RoomDao` + `PlacedFurnitureDao`. No migrations and no `TypeConverters` — every column is a primitive or a String.
- `repository/FurnitureRepository.kt` — Hardcoded furniture catalog (search/filter) **and** `ColorPaletteRepository` (object with static palettes by `DesignStyle`). Both live in this one file despite the name.

**Floor-plan model:** A `FloorPlan` is `nodes: List<WallPoint>` (shared point pool, cm) + `rooms: List<List<Int>>` (each room = polygon of node indices, so adjacent rooms share edges) + `openings: List<WallOpening>` (doors/windows on a room edge, parameterized by `t∈[0,1]` along the edge). It is **serialized to a JSON string in `DesignRoom.floorPlanJson`** with `kotlinx.serialization` — it is *not* a Room entity.

It also carries the storeys: `roomLevels` (parallel to `rooms`), `stairs: List<Stair>` (a flight rising from `level` to the one above, straight / L / U — see [`docs/stairs.md`](docs/stairs.md)) and `levelSurfaces: List<LevelSurface>` (wall preset, floor preset and wall paint, **one per storey**). Wall and floor finish therefore lives on the *plan*, not the entity: `DesignRoom.wallColor`, `floorColor`, `wallPresetIdx` and `floorPresetIdx` are leftovers nothing writes any more, and `RoomCard` still reads two of them — see [`docs/backlog.md`](docs/backlog.md).

### Designer (the core feature, `ui/screen/designer/`)

`DesignerViewModel` owns everything: the `FloorPlan`, `placedFurniture`, the surface picks, and all DB reads/writes. `DesignerContent` only renders `DesignerState` and calls its callbacks; it toggles between two `EditorMode`s (`DesignerModels.kt`):

- **`DRAW_WALLS`** → `view/viewport/FloorPlanCanvas.kt` — 2D top-down editor: place nodes, form room polygons, add/drag/resize wall openings, drag furniture footprints. All hit-testing & pointer logic here.
- **`DESIGN`** → `view/viewport/FilamentRoomViewport.kt`. Furniture moves/selection/door drops call back into `DesignerState`, which the ViewModel auto-saves (debounced). The old Canvas renderer (`Room3DViewport.kt`), its toggle and the `ViewMode` enum were **deleted** — Filament is the only renderer, so there is no fallback if it fails on a device.

The two viewport files are **carried over untouched** apart from their package line — they are the riskiest code in the app (Filament lifecycle, pointer maths). Change them deliberately, not as collateral.

Supporting files:
- `DesignerModels.kt` — enums (`EditorMode`, `ViewMode`, `PlacementTool`, …), `ROOM_PALETTES`, hit-test helper classes. Data only.
- `DesignerUtils.kt` — pure geometry: grid snap, polygon area, `findNearestWall` (shared by 2D canvas and 3D viewport).
- `view/DesignerTopBar.kt` (back button + hint chip), `view/DesignerActions.kt` (floating clusters), `view/ModeTabBar.kt` — screen chrome. `FloorPlanToolbar.kt` is gone.
- `view/FurnitureControlPanel.kt` — panel shown when furniture is selected in DESIGN mode; capped at 320 dp so it stops covering the viewport.
- `view/AddFurnitureSheet.kt`, `view/SurfaceSheet.kt` — bottom sheets. `SurfaceSheet` has four tabs: wall preset, floor preset, wall paint, palettes.

**Furniture state flow:** `DesignerState.placedFurniture` → mutated through state callbacks → auto-saved by watching `furnitureRevision`, a counter bumped in the setter. The plan works the same way through `planRevision`. **Do not watch the values themselves** — both are structures of lists, so `snapshotFlow` would run a deep `equals` on every frame of a drag. The 150 ms delay is coalescing, not laziness: it folds a whole gesture into one write. `onBack` flushes, because there is no Save button.

Other screens: `ui/screen/home/` (the landing — two cards), `ui/screen/project/` (room list, swipe-to-delete with snackbar undo), `ui/screen/settings/`, `ui/screen/catalogue/` (+ `item/`, which renders one model in its own small Filament scene).

### Theme (`ui/theme/`)

- `Color.kt` — the **Clay on white** ramps (`InteriorColors`: clay primary, neutral-grey secondary, teal accent, white/near-black surfaces) plus `InteriorAccents`, a `staticCompositionLocalOf` for the door/window and 2D-canvas colours Material has no slot for. **Don't hardcode colours in a composable**; add a token here. `FloorPlanCanvas` used to paint a fixed `#12121F` ground with `Color.White` strokes, which ignored light mode entirely — it now reads `LocalInteriorAccents`. Wall lengths are drawn there too, in the accent colour, pushed to the outside of each room.
- `Type.kt` — `AppFont` is Plus Jakarta Sans, one **variable** TTF in `res/font/`; Compose derives each weight from the `wght` axis.
- `Theme.kt` — full light *and* dark `ColorScheme`s. Both define every slot, including `tertiary`, `error` and the `surfaceContainer*` family; leaving one out silently falls back to the default M3 purple. **Every value here must be a `C.` token** — hardcoding a hex in this file survives palette swaps and silently tints one mode (a moss-green `#141E19` did exactly that to dark mode).
- The ground is **pure white**, so surfaces cannot separate by value. Cards, sheets, the toolbar and the furniture tiles separate with a 1 dp `outlineVariant` border; the contrast in the app comes from the near-black 3D viewport, not from a darkened background.
- Strings live in `res/values/strings.xml` and the UI is English. Catalog item labels are plain strings in `FurnitureCatalog.kt`, not string resources.

## Key Constraints

- **Min SDK 31** — Android 12. That is the line where `RenderEffect` works, which is what lets the glass blur for real; AGSL (refraction) would need 33.
- **Global opt-ins** in `build.gradle` `freeCompilerArgs`: `ExperimentalMaterial3Api`, `ExperimentalFoundationApi`, `ExperimentalAnimationApi` — **do not** add redundant `@OptIn` in files.
- **DB schema:** no `Migration` objects, ever — this is a single-developer app with no installed users, so a schema change wipes the database. But you **must bump `version`** in `@Database` on every schema change. `fallbackToDestructiveMigration()` only fires when the version moves; leave it unchanged and Room compares a schema hash instead, finds a mismatch and throws *"Room cannot verify the data integrity"* — a crash on launch for anyone holding the older database (verified on an emulator, 2026-09-15). The version is a schema fingerprint here, not a migration count.
- **`FloorPlan`** ↔ JSON via `kotlinx.serialization` (not Gson, despite Gson being a dependency).
- **No tests** — `test`/`androidTest` dirs are empty though JUnit/Espresso/Compose-test deps are wired. `./gradlew test` passes trivially.
- **No linter** (`kotlin.code.style=official` only; no detekt/ktlint).
- **ProGuard disabled** for release (`minifyEnabled false`).
- **Filament/gltfio are live** — verify rendering changes on a real device (the emulator GPU differs).
- **Images:** previews in `assets/previews` are WebP; `res/mipmap-*/ic_launcher.png` are 14-byte placeholders, not real images.
- **No emoji as icons** — use `Icons.Outlined.*`. The toolbar and floor-material list used to use 🚪🪟🪵; they don't any more.
- **Touch targets ≥ 48 dp** — `ui/properties/MinTouchTarget`. Draw a swatch smaller if you like, but keep the tappable box at 48 dp.
