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

> ⚠️ **The README is aspirational and partly wrong.** It advertises a "Filament 3D engine" and "ARCore AR placement." Neither is used. `filament-android`, `gltfio`, `camera`, and `ar:core` are declared in `build.gradle` but **never imported anywhere in the source**. All "3D" is hand-rolled isometric/perspective **projection math drawn on a Compose `Canvas`**. There is no AR, no real 3D engine, no camera capture. Treat README feature claims with suspicion; trust the code.

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
- **`DESIGN`** → `Room3DViewport.kt` — pseudo-3D Canvas renderer (azimuth/elevation/zoom camera, projects rooms + walls + openings + furniture boxes). Furniture moves emit callbacks back up to `RoomDesignerScreen`.

Supporting files:
- `RoomDesignerModels.kt` — enums (`EditorMode`, `ViewMode`, `PlacementTool`, …), `ROOM_PALETTES`, hit-test helper classes. Data only.
- `RoomDesignerUtils.kt` — pure geometry: grid snap, polygon area, color parse, `findNearestWall` (shared by 2D canvas and 3D viewport).
- `FurnitureControlPanel.kt` — bottom panel shown when furniture is selected in DESIGN mode (rotate/scale/dimension sliders, wall-mount toggle). Callback-only.
- `FurnitureSheets.kt` — modal sheet to add furniture (category picker, Canvas preview, `CATEGORY_DEFAULTS`). Shares `DimSlider` with the control panel.

**Furniture state flow:** owned by `RoomDesignerScreen` → mutated via callbacks from viewport/canvas/panel → **persisted only on explicit Save** (loaded on screen open from `PlacedFurnitureDao`).

Other screens: `HomeScreen.kt` (room list, swipe-to-delete, FAB to create) and `OtherScreens.kt` (`ColorPickerScreen` — wall color / floor material / palette tabs). `ui/theme/Theme.kt` holds the Material 3 theme (`InteriorColors`, warm-neutral palette).

## Key Constraints

- **Min SDK 26** — guard newer APIs with version checks.
- **Global opt-ins** in `build.gradle` `freeCompilerArgs`: `ExperimentalMaterial3Api`, `ExperimentalFoundationApi`, `ExperimentalAnimationApi` — **do not** add redundant `@OptIn` in files.
- **DB schema:** Room version 5; always add a migration when changing entities — **never** `fallbackToDestructiveMigration`.
- **`FloorPlan`** ↔ JSON via `kotlinx.serialization` (not Gson, despite Gson being a dependency).
- **No tests** — `test`/`androidTest` dirs are empty though JUnit/Espresso/Compose-test deps are wired. `./gradlew test` passes trivially.
- **No linter** (`kotlin.code.style=official` only; no detekt/ktlint).
- **ProGuard disabled** for release (`minifyEnabled false`).
- **Unused deps:** Filament, gltfio, ARCore, CameraX are declared but dead. Don't assume they work before wiring them up; verify on a real device.
