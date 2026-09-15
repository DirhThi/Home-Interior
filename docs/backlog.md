# Backlog

Everything still open on InteriorDesign3D, in the order it is worth doing.

The planning artifact ("From One Room to a House") tracks 37 tasks across 7
steps; 23 are ticked. This file is the part that is *not* done, plus three
debts that grew out of the stair work and were never on that list.

| Group | Count | Who |
|---|---|---|
| [Debt](#debt) — fallout from work already landed | 1 | me |
| [Step 7 — Exterior](exterior.md) | 6 | me |
| [Verify on a device](#verify-on-a-device) | 6 | you |
| [Drop](#drop-these-two) | 2 | — |

---

## Debt

### ~~1. The Home card paints colours nothing writes any more~~ — done

**Where:** `ui/screen/home/view/RoomCard.kt:74-75`, `data/models/Models.kt:344-352`

Since `dbf5500` the wall and floor finish lives on the plan
(`FloorPlan.levelSurfaces`, one `LevelSurface` per storey) and
`DesignerViewModel.persistSurfaces()` only writes `stairPresetIdx`,
`shadowsEnabled` and `autoHideWalls` back to the entity. Four `DesignRoom`
columns are now write-dead:

```
wallColor        floorColor        wallPresetIdx        floorPresetIdx
```

`RoomCard` still reads two of them, so **every** card on Home renders the same
`#F5F0EB` wall over a `#C4A882` floor regardless of what the room actually
looks like. It is not a crash, it is a thumbnail that stopped telling the truth.

**Done** — the card now reads the plan instead: `HomeViewModel` decodes
`floorPlanJson` into a `RoomListItem`, the thumbnail is `PlanThumbnail` drawing the
real outline, and the two text lines are room count + area and storeys + ceiling.
Nine dead columns, the `FloorMaterial` enum and both `TypeConverter`s went with it.

What it took, for the record:

1. Decode `floorPlanJson` in `HomeViewModel` — not in the composable; the
   architecture rule is that composables never touch the DB or its payloads —
   and expose the two resolved hex strings on `HomeState`:

   ```kotlin
   val surface  = plan.surfaceOf(0)
   val wallHex  = surface.wallColor.ifBlank { WALL_PRESETS[surface.wallPresetIdx].colorHex }
   val floorHex = FLOOR_PRESETS[surface.floorPresetIdx].colorHex
   ```
   Coerce both indices into their preset list's `indices` first; a plan written
   by a future build can carry an index this build does not have.

2. Point `RoomCard` at those instead of `room.wallColor` / `room.floorColor`.

3. Delete the four columns from `DesignRoom`. The database is pinned at v1 with
   `fallbackToDestructiveMigration()`, so this costs nothing but a wipe — do
   **not** add a `Migration`.

4. While in there: `RoomCard.kt:113` prints `room.floorMaterial.displayName`,
   but nothing can change `floorMaterial` since `ColorPickerScreen` was deleted,
   so it says "Hardwood" on every card forever. Either derive the label from the
   floor preset or drop the line and the `FloorMaterial` enum with it.

Storey 0 is the right storey to show: it is the one a plan always has.

### ~~2. Stair treads are too shallow at the default length~~ — done

**Done** — each shape now has its own default depth (straight 400, L 300 + leg
200, U 300), which puts every shape at a 25 cm tread for a 2.70 m storey. `legCm`
and `wellCm` are editable, and the panel warns when a flight ends up shallower
than 22 cm because `fitStair` squeezed it into a small room. See
[stairs.md § Proportions](stairs.md#proportions--done).

### 3. No handrails

See [stairs.md § Handrails](stairs.md#open-handrails). No new GLB needed; the
blocker is that `buildBox` can only rotate about Y, so a raking rail cannot be
expressed yet. A pitched roof needs the same fix, so do it once for both.

---

## Step 7 — Exterior

Six tasks, none started, the only feature block left on the roadmap. Full
write-up in [exterior.md](exterior.md).

---

## Verify on a device

These are yours — the emulator GPU is not the one that matters for Filament.

| # | What to check |
|---|---|
| t1-5 | Open the 2-bedroom sample in 3D; the doubled party wall should be plainly visible. If it is not, the fixture is wrong. |
| t2-5 | The 2-bedroom sample shows one 10 cm party wall, no z-fighting, auto-hide still correct. |
| t3-6 | Place a door on a shared wall, then orbit through it — both rooms open, from either side. |
| t4-5 | A door at 80 cm and at 140 cm both keep their proportions. |
| t5-3 | Change the ceiling height — walls, door lintels and window headers all follow. |
| t6-6 | 2 storeys × 4 rooms stays smooth. |

Worth adding to that list, since it postdates the roadmap: an L and a U stair
at several rotations, on a plan where the room above is *smaller* than the
flight, so `FloorPlan.fitStair` has to scale it down.

---

## Drop these two

- **t3-7** — "an app upgraded from v6 keeps every existing door and window in
  place". Meaningless now: the database is v1 with a destructive fallback, there
  is no upgrade path to keep anything across.
- **t6-1** — "make `rebuildStructure` incremental or per-level *before* anything
  else here". It was written as a gate and is not one: `structSig`
  (`FilamentRoomViewport.kt:297`) already skips the rebuild unless the structure
  actually changed. Keep it as an optimisation to reach for *if* a two-storey
  plan stutters on a real device (t6-6), not as a prerequisite.

---

## Found while fixing debt 1

### Drawing a plan and pressing Back throws it away

`persistPlan()` runs only from **Save** and from entering Design mode
(`DesignerViewModel.kt:48`, `:63`). `onBack()` just pops. Draw a room, press the
system Back button, and the work is gone with no warning — I lost a storey to it
while testing. One-line fix (persist, then pop), but back-button behaviour is worth
a deliberate decision rather than a drive-by change.

### `DesignRoom.heightCm` is the last summary column left

It is genuinely maintained (`DesignerViewModel.kt:311`), so it is not stale. But
ceiling height is a property of a storey, not of a building — it belongs on
`LevelSurface` next to the wall and floor presets, and then the entity holds
nothing but identity, timestamps and three viewer toggles.

## Dead code noticed while writing this

- `Stair.centreLinePlan()` (`Models.kt:204`) — nothing calls it since the 2D
  canvas moved to `runs()`.
- `Stair.centreLine()` is only reachable through `centreLinePlan()`.
