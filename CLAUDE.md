# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Clean build
./gradlew clean

# Sync dependencies
./gradlew --refresh-dependencies
```

**Requirements:** Android Studio Hedgehog or newer, JDK 17+, Android SDK with API 26–36 installed.  
`local.properties` is gitignored — create it locally with `sdk.dir=<path>`.

## Architecture

**Pattern:** Single-module Android app. Screens access the Room database directly (no ViewModel layer currently — the ViewModels listed in the original plan were not implemented).

**Layer breakdown:**

- `data/models/Models.kt` — All data classes: `FurnitureItem`, `DesignRoom`, `PlacedFurniture`, `WallPoint`, `WallOpening`, `FloorPlan`, `ColorPalette`; enums `FurnitureCategory`, `FloorMaterial`, `DesignStyle`, `OpeningType`
- `data/repository/Database.kt` — Room database (`AppDatabase`, version 5) with `RoomDao` and `PlacedFurnitureDao`; migrations 1→5 are all present inline. Type converters handle `List<String>` and `FloorMaterial`.
- `data/repository/FurnitureRepository.kt` — Mock furniture catalog with search/filter
- `data/repository/ColorPaletteRepository` (referenced in `OtherScreens.kt`) — Provides color palettes by `DesignStyle`
- `ui/screens/HomeScreen.kt` — Room list with swipe-to-delete and create-new FAB; accesses `AppDatabase.getInstance(context)` directly
- `ui/screens/RoomDesignerScreen.kt` — 3D room editor using Google Filament and ARCore
- `ui/screens/OtherScreens.kt` — `ColorPickerScreen` (wall colors, floor materials, palettes via tabs)
- `ui/Navigation.kt` — `Screen` sealed class with 3 routes: `Home`, `RoomDesigner/{roomId}`, `ColorPicker/{roomId}`; no bottom nav
- `ui/theme/Theme.kt` — Material 3 theming

**DB singleton:** `AppDatabase.getInstance(context)` — double-checked locking, initialized in each screen via `LocalContext.current`.

**Key dependencies:** Google Filament (v1.49.1) + ARCore (1.41.0) for 3D/AR (require physical device); CameraX (1.3.1); Coil (2.5.0) for image loading; kotlinx-serialization-json for `FloorPlan`/model JSON stored in Room columns; Gson (2.10.1); DataStore-preferences; accompanist-permissions; skydoves/colorpicker-compose.

## Key Constraints

- **Min SDK 26** (Android 8.0) — guard any newer APIs with version checks
- **Global opt-ins** are set in `build.gradle` via `freeCompilerArgs`: `ExperimentalMaterial3Api`, `ExperimentalFoundationApi`, `ExperimentalAnimationApi` — do not add redundant `@OptIn` annotations in individual files
- **No test files** — test infrastructure is wired in `build.gradle` but `/test` and `/androidTest` dirs are empty
- **No linter** — `kotlin.code.style=official` is set; no detekt/ktlint rules present
- **ProGuard disabled** for release builds (`minifyEnabled false`)
- **FloorPlan** is stored as JSON string in `DesignRoom.floorPlanJson`; deserialize with `kotlinx.serialization.json.Json.decodeFromString<FloorPlan>()`
- **DB schema**: Room version 5; always add a migration when changing entities — never use `fallbackToDestructiveMigration`
