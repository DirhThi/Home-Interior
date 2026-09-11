# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

InteriorDesign3D (`com.interiordesign3d`) — a Kotlin + Jetpack Compose interior design app: draw a multi-room floor plan, place furniture, and view it in a pseudo-3D viewport.

## Build Commands

```bash
# Build debug APK (what CI runs — see .github/workflows/build.yml)
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run unit tests / instrumented tests (NOTE: no test files exist yet — see below)
./gradlew test
./gradlew connectedAndroidTest

# Clean build
./gradlew clean
```

**Toolchain:** Gradle 8.0, AGP 8.1.2, Kotlin 1.9.20, Compose compiler 1.5.4 (BOM 2024.06.00), **JDK 17**. Room uses **kapt** (not KSP). Android SDK API 26–36 required.
`local.properties` is gitignored — create it locally with `sdk.dir=<path>`.

## Architecture

**Pattern:** Single-module app. Screens read/write the Room database **directly** via `AppDatabase.getInstance(context)` — there is **no ViewModel layer** and **no DI**. State is held in composables with `remember`/`mutableStateOf`. The `viewmodel/` package and MVVM/StateFlow setup described in the README were never implemented; `InteriorDesignApp` (the `Application` class in `MainActivity.kt`) is an empty placeholder.

> ⚠️ **The README is partly wrong.** There is no AR and no camera capture (`ar:core`, CameraX are dead deps). The real 3D view **is** Google Filament (`FilamentRoomViewport.kt`); the older Canvas renderer (`Room3DViewport.kt`) is still reachable via a toggle.

### 3D viewport (`FilamentRoomViewport.kt`)

- `RoomScene` owns Engine/Scene/View, a `Choreographer` loop that only renders when `dirty`, orbit camera (drag = azimuth/elevation, pinch = zoom), lifecycle pause/resume.
- **Walls, baseboards, corner posts, door lintels, window sill/header are procedural boxes** (`buildBox`) with UVs in metres / `tileM`; the floor is a triangulated polygon (`buildFloorMesh`). Materials come from `assets/models/mat_*.glb` (a textured quad; the asset stays out of the scene, only its `MaterialInstance` is used via `materialOf(slot, model, colorHex)`; tint = `baseColorFactor`). Textures are ambientCG CC0 JPEGs (512 px) embedded in the GLB.
- Door/window openings: a `q_door*` prop dragged within 20 cm of a wall becomes a real `WallOpening` (`tryDropDoorOnWall` → `onDropOpening`); the leaf model is `WallOpening.style`. Windows get a `q_window_small/large` frame.
- Furniture: gltfio assets from `assets/models/<key>.glb`, scaled by `PlacedFurniture.scale × CatalogItem.unitScale` (Quaternius packs are authored at 2× → `unitScale 0.5`). Placement rules live in `resolveDrag` (push out of furniture, snap ≤ 8 cm to walls, stacking on `surface` items).

### Furniture catalog (`FurnitureCatalog.kt`)

`FURNITURE_CATALOG` (groups of `CatalogItem`) is the single source of truth: `key` = GLB + WebP preview file name and `PlacedFurniture.furnitureId`; `mount` (FLOOR/WALL/CEILING), `surface`, `unitScale`. All models are **Quaternius CC0** (`q_` Ultimate House Interior Pack incl. 26 converted from the OBJ release, `qf_` Furniture Pack). `WALL_PRESETS` / `FLOOR_PRESETS` map to `mat_*` materials. `MODEL_CREDITS` is shown from the Home ⓘ dialog. Items whose key is not in the catalog are dropped on load.

Asset pipeline (no Blender): Python in the session scratchpad converted OBJ→GLB, rendered flat-shaded previews, and wrote `mat_*.glb` from JPEGs; keep previews as **WebP**.

### Navigation (`ui/Navigation.kt`)

Plain Compose Navigation (`androidx.navigation:navigation-compose`), `Screen` sealed class, 3 routes, no bottom nav:
`Home` → `room_designer/{roomId}` → `color_picker/{roomId}`.

### Data layer (`data/`)

- `models/Models.kt` — All data classes & enums: `FurnitureItem`, `DesignRoom` (entity, table `rooms`), `PlacedFurniture` (entity, table `placed_furniture`, `roomId` FK), `WallPoint`, `WallOpening`, `FloorPlan`, `ColorPalette`; enums `FurnitureCategory`, `FloorMaterial`, `DesignStyle`, `OpeningType`.
- `repository/Database.kt` — `AppDatabase` (DB name `interior_design_db`, **version 5**), DAOs `RoomDao` + `PlacedFurnitureDao`. Migrations `MIGRATION_1_2 … MIGRATION_4_5` are defined inline and all registered. `Converters` handle `List<String>` and `FloorMaterial`.
- `repository/FurnitureRepository.kt` — Hardcoded furniture catalog (search/filter) **and** `ColorPaletteRepository` (object with static palettes by `DesignStyle`). Both live in this one file despite the name.

**Floor-plan model:** A `FloorPlan` is `nodes: List<WallPoint>` (shared point pool, cm) + `rooms: List<List<Int>>` (each room = polygon of node indices, so adjacent rooms share edges) + `openings: List<WallOpening>` (doors/windows on a room edge, parameterized by `t∈[0,1]` along the edge). It is **serialized to a JSON string in `DesignRoom.floorPlanJson`** with `kotlinx.serialization` — it is *not* a Room entity.

### RoomDesigner (the core feature, split across `ui/screens/`)

`RoomDesignerScreen.kt` is the orchestrator. It owns furniture as a `mutableListOf<PlacedFurniture>` and the `FloorPlan`, and toggles between two `EditorMode`s (`RoomDesignerModels.kt`):

- **`DRAW_WALLS`** → `FloorPlanCanvas.kt` — 2D top-down editor: place nodes, form room polygons, add/drag/resize wall openings, drag furniture footprints. All hit-testing & pointer logic here.
- **`DESIGN`** → `FilamentRoomViewport.kt` (default, `use3DEngine`) or `Room3DViewport.kt` (legacy Canvas renderer). Furniture moves/selection/door drops emit callbacks back up to `RoomDesignerScreen`, which auto-saves furniture (debounced) to Room.

Supporting files:
- `RoomDesignerModels.kt` — enums (`EditorMode`, `ViewMode`, `PlacementTool`, …), `ROOM_PALETTES`, hit-test helper classes. Data only.
- `RoomDesignerUtils.kt` — pure geometry: grid snap, polygon area, color parse, `findNearestWall` (shared by 2D canvas and 3D viewport).
- `FurnitureControlPanel.kt` — bottom panel shown when furniture is selected in DESIGN mode (scale, colour swatches, wall-mount toggle, height, rotation). Callback-only.
- `FurnitureSheets.kt` — `AddFurnitureSheet` (catalog groups + WebP previews), `SurfaceSheet` (wall/floor presets with swatches, shadows, auto-hide walls), `AssetImage`.

**Furniture state flow:** owned by `RoomDesignerScreen` → mutated via callbacks from viewport/canvas/panel → auto-saved (400 ms debounce) and on explicit Save; loaded on screen open from `PlacedFurnitureDao`.

Other screens: `HomeScreen.kt` (room list, swipe-to-delete, FAB to create) and `OtherScreens.kt` (`ColorPickerScreen` — wall color / floor material / palette tabs). `ui/theme/Theme.kt` holds the Material 3 theme (`InteriorColors`, warm-neutral palette).

## Key Constraints

- **Min SDK 26** — guard newer APIs with version checks.
- **Global opt-ins** in `build.gradle` `freeCompilerArgs`: `ExperimentalMaterial3Api`, `ExperimentalFoundationApi`, `ExperimentalAnimationApi` — **do not** add redundant `@OptIn` in files.
- **DB schema:** Room version 5; always add a migration when changing entities — **never** `fallbackToDestructiveMigration`.
- **`FloorPlan`** ↔ JSON via `kotlinx.serialization` (not Gson, despite Gson being a dependency).
- **No tests** — `test`/`androidTest` dirs are empty though JUnit/Espresso/Compose-test deps are wired. `./gradlew test` passes trivially.
- **No linter** (`kotlin.code.style=official` only; no detekt/ktlint).
- **ProGuard disabled** for release (`minifyEnabled false`).
- **Unused deps:** ARCore, CameraX are declared but dead. Filament/gltfio are live — verify rendering changes on a real device (the emulator GPU differs).
- **Images:** previews in `assets/previews` are WebP; `res/mipmap-*/ic_launcher.png` are 14-byte placeholders, not real images.
