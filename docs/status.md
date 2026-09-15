# Where the project stands

Snapshot as of **2026-09-14**, commit `dbf5500`.

For *how the code is laid out* read [CLAUDE.md](../CLAUDE.md). For *what is left*
read [backlog.md](backlog.md). This file is the honest middle: what works today,
how well it has actually been checked, and what is fragile.

| | |
|---|---|
| Version | `versionName 1.0`, `versionCode 1`, never released, no git tags |
| Build | `./gradlew assembleDebug` green |
| Database | Room v2, destructive fallback, no migrations by design (bump the version on every schema change) |
| Code | 45 Kotlin files, ~6,800 lines |
| Assets | 7.6 MB — 160 GLB, 159 WebP previews, 4 sample plans |
| Tests | none |

---

## What the app does today

**Home.** The room list draws each plan as a small axonometric doll's house —
walls, doors, windows, furniture, storeys stacked — from the plan itself, with no
render and no cache behind it.

**Draw a plan.** Tap corners to lay out rooms; nodes are shared so adjacent
rooms genuinely share an edge rather than each owning a wall. Grid snap, undo,
drag a node to reshape. Wall lengths are drawn on each edge, de-duplicated and
pushed clear of the line. Four sample plans load from Home — studio (24.4 m²),
two-bedroom (56.3 m², 4 shared edges), L-shaped (41.7 m²), townhouse (60.2 m²,
7 shared edges).

**Doors and windows.** Placed from the toolbar or by dragging a door prop within
20 cm of a wall in 3D. They belong to the wall, not the room — keyed by node
pair — so one door on a party wall opens both sides. Selectable and editable
from either editor, including from the 3D view by tapping the leaf. Width,
style, hide-the-leaf and swing-open are all per opening, and the wall cut is
derived from the model as actually fitted.

**Two storeys.** A level switcher drives both editors. The 2D plan shows the
selected storey with the one below as a dashed guide; the 3D view stacks them
with a floor slab between. Wall preset, floor preset and wall paint are stored
per storey.

**Stairs.** Straight, L and U. Place one and it cuts its own opening in the slab
above and forces itself to fit inside a single room up there. Width, length,
rotation and shape are editable; the material is its own preset, separate from
walls and floors. Details in [stairs.md](stairs.md).

**Balconies.** Tap an outside wall and a railed slab hangs off it, width and
projection editable. Interior walls are refused — a balcony there would hang into the
next room. On an upper storey it reads either way: over open air it cantilevers with a
visible soffit, over the storey below's flat roof it sits flush on the terrace, and a
door beside it opens onto it.

**Furnish.** 145 Quaternius CC0 models in a tabbed sheet. Drag to place with
real rules — pushed out of other furniture, snapped to walls within 8 cm,
stacked on surfaces. Scale, rotation, height and colour per item. Auto-saved
with a 400 ms debounce.

**Materials.** 34 `mat_*.glb` textured quads behind 15 wall presets, 16 floor
presets, 11 roof and 7 ground presets, plus custom wall paint and whole-room
palettes. `tools/make_material.py` adds more from ambientCG.

---

## How well this has been checked

Be careful reading the above as "done" — here is what that word is worth.

**Verified on the emulator, by me, this session.** L and U stairs meeting their
landings, the stairwell opening lining up with the flight below, two storeys
stacking without z-fighting, a door on a shared wall opening both rooms, the
palette's measured contrast, the number dialog committing what was typed.

**Not verified on a real device at all.** This is the gap that matters. CLAUDE.md
has said from the start that Filament rendering must be checked on hardware
because the emulator GPU differs, and it has not been. Every 3D claim above rests
on an emulator screenshot. [backlog.md § Verify on a device](backlog.md#verify-on-a-device)
lists the six checks worth doing first.

**No automated tests exist.** `test/` and `androidTest/` are empty though JUnit,
Espresso and Compose-test are wired up, so `./gradlew test` passes without
asserting anything. Nothing above is protected against regression.

---

## What is fragile

**`FilamentRoomViewport.kt` — 1,287 lines.** Engine lifecycle, procedural
geometry, ear-clipping triangulation, hole bridging, pointer picking and the
orbit camera all in one file. It is the riskiest code in the app; most bugs this
session came from it, and most were geometry that looked fine until viewed from
a second angle. Change it deliberately, verify visually from more than one
camera position.

**`FloorPlanCanvas.kt` — 808 lines.** All 2D hit-testing and pointer logic.
Second-riskiest for the same reason: no tests, and correctness is visual.

**`buildBox` only rotates about Y.** Nothing can be tilted, which is why there
are no handrails and no pitched roof. One shared fix unblocks both — see
[stairs.md § Why 3 is blocked](stairs.md#why-3-is-blocked).

**Filament has no fallback.** The legacy Canvas renderer was deleted in
`7c67454`. If Filament fails to initialise on a device, the design view shows
nothing — there is no second path and no error state.

**Draw calls are the budget, not triangles.** Every `buildBox` is its own Filament
renderable, so anything repeated — balusters, posts, parapet segments — has to go
through `buildBoxes`, which bakes the transform into the vertices and emits one mesh.
Built one at a time they came to 177 renderables for a two-storey plan; batched, 80.
Measured on an emulator, where it still ran at 60 fps — a phone GPU is less forgiving.

**Single-sided roof faces.** A roof plane exists only if its triangle winding faces
the camera, and nothing in the code will warn you — a face wound the wrong way just
is not there. `buildFace` winds every face away from the mass centre for that reason;
keep new roof geometry going through it.

**Stair treads are ~17.5 cm at the defaults** where ~25 cm is normal. The riser
is right; the run is too short. Workaround and fix in
[stairs.md § Proportions](stairs.md#open-proportions).

---

## Shipping blockers, if that ever comes up

Not on the roadmap, but none of this can ship without them:

- `res/mipmap-*/ic_launcher.png` are 14-byte placeholders, not images.
- ProGuard is off for release (`minifyEnabled false`).
- No linter — `kotlin.code.style=official` only, no detekt or ktlint.
- No `LICENSE` file, though the README has always claimed MIT.
- A destructive-fallback database is correct *now* and wrong the day someone
  other than you installs the app.
