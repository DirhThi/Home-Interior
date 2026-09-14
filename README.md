# Budget Interiors

An Android app for laying out a home and seeing it in 3D. Draw a multi-room
floor plan to scale, hang doors and windows on the walls, stack a second storey
with a staircase between them, furnish it from a catalogue of 126 models, and
look around the result in a real-time 3D viewport.

Kotlin + Jetpack Compose + Google Filament. Single module, no DI, no backend —
everything is on-device.

> **Status:** pre-release and single-developer. `versionCode 1`, never shipped,
> no tests, and the database is pinned at v1 with a destructive fallback — a
> schema change wipes it rather than migrating. See
> [`docs/status.md`](docs/status.md) for an honest account of what has and has
> not been verified.

## What it does

**Floor plan.** Tap corners to lay out rooms. Nodes are shared between rooms, so
two adjacent rooms genuinely share one wall rather than each building their own.
Grid snap, undo, drag a node to reshape, wall lengths drawn on every edge. Four
sample plans ship with the app: studio, two-bedroom, L-shaped, townhouse.

**Doors and windows.** Placed from the toolbar, or by dragging a door model
within 20 cm of a wall in the 3D view. An opening belongs to the *wall*, not to a
room, so one door on a party wall opens both sides. Width, leaf style, hide-leaf
and swing-open are per opening, and the hole cut in the wall is derived from the
model as actually fitted.

**Two storeys.** A level switcher drives both editors. The 2D plan draws the
selected storey with the one below as a dashed guide; the 3D view stacks them
with a floor slab between. Wall preset, floor preset and paint are stored per
storey.

**Stairs.** Straight, L-shaped and U-shaped. A flight is described as straight
runs plus flat landings, cuts its own opening in the slab above, and scales
itself to fit inside one room up there. See [`docs/stairs.md`](docs/stairs.md).

**Furnishing.** 126 items in six groups — seating & tables, bedroom, kitchen,
bathroom, decor & lighting, architecture. Drag to place with real rules: pushed
out of other furniture, snapped to walls within 8 cm, small items resting on
table tops. Scale, rotation, height and colour per item, auto-saved.

**Materials.** 11 wall presets and 14 floor presets over textured materials,
plus custom wall paint and whole-room palettes.

## Build

Requires **JDK 17** and Android SDK **API 26–36**. `local.properties` is
gitignored; create it with your SDK path:

```
sdk.dir=/Users/yourname/Library/Android/sdk
```

```bash
./gradlew assembleDebug     # what CI builds
./gradlew assembleRelease
./gradlew clean
```

CI runs `assembleDebug` on push to `main` / `master` / `develop`
(`.github/workflows/build.yml`).

`./gradlew test` passes trivially — `test/` and `androidTest/` are empty though
JUnit, Espresso and Compose-test are wired up.

## Tech stack

| Layer | Technology |
|---|---|
| UI | Jetpack Compose, Material 3 (BOM 2024.06.00) |
| 3D | Google Filament + gltfio |
| Navigation | Compose Navigation — two routes, no bottom nav |
| Database | Room (kapt, **version 1**, destructive fallback) |
| Serialization | kotlinx.serialization — the floor plan is JSON in a TEXT column |
| Architecture | one package per screen: Screen / ViewModel / state / view |
| Build | Gradle 8.0, AGP 8.1.2, Kotlin 1.9.20, Compose compiler 1.5.4 |

There is **no** AR, no camera capture, no DI framework and no network layer.

## Project structure

```
app/src/main/java/com/interiordesign3d/
├── MainActivity.kt
├── common/base/                  BaseScreen, BaseViewModel, Navigator, screen state
├── data/
│   ├── catalog/FurnitureCatalog.kt   126 items + wall/floor presets + credits
│   ├── models/Models.kt              entities, FloorPlan, Stair, WallOpening
│   ├── plans/                        sample-plan loading
│   └── repository/Database.kt        Room DB + DAOs
└── ui/
    ├── Navigation.kt             NavHost, Screen routes, Navigator adapter
    ├── properties/               shared UI helpers (number dialog, layouts, colour parsing)
    ├── theme/                    Clay-on-white palette, type, accents
    └── screen/
        ├── home/                 room list, swipe-to-delete with undo, samples
        └── designer/             the core feature
            ├── state/            DesignerState
            └── view/
                ├── viewport/FloorPlanCanvas.kt        2D editor
                └── viewport/FilamentRoomViewport.kt   3D renderer
```

`assets/` holds `models/*.glb` (furniture and `mat_*` material quads),
`previews/*.webp` and `plans/*.json`.

## Docs

- [`CLAUDE.md`](CLAUDE.md) — architecture, conventions and the rules that are
  easy to break
- [`CHANGELOG.md`](CHANGELOG.md) — what has changed, and the rule that every
  visible change gets an entry
- [`docs/status.md`](docs/status.md) — where the project stands, and what is
  fragile
- [`docs/backlog.md`](docs/backlog.md) — everything still open
- [`docs/stairs.md`](docs/stairs.md), [`docs/exterior.md`](docs/exterior.md) —
  specs

## Credits

3D models: **Quaternius** (quaternius.com) — Ultimate House Interior Pack and
Furniture Pack, CC0.
Textures: **ambientCG.com**, CC0.
Both are also credited in-app from the ⓘ dialog on Home.

Typeface: Plus Jakarta Sans (SIL Open Font License), shipped as a single
variable font.

## License

No `LICENSE` file is checked in yet — add one before sharing this repo. The
bundled models and textures are CC0 and carry no restriction either way.
