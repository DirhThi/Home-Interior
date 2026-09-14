# Step 7 — Exterior

The last feature block on the roadmap, and the only one not started. Six tasks.
Today the app renders a house you can only ever stand *inside*: exterior walls
are hidden the moment the camera faces them, there is no ground, no roof, and
every material in the catalogue is an interior finish.

## What already exists that helps

| Thing | Where | Why it matters |
|---|---|---|
| `WallSeg.exterior` | `FilamentRoomViewport.kt:159`, set at `:393` from `edgeUses.size == 1` | which walls are on the outside is already known per segment |
| `outset(poly, cm)` | `FilamentRoomViewport.kt:835` | offsets a polygon outward; the plot boundary and the roof overhang both want it |
| `buildFloorMesh` | `FilamentRoomViewport.kt:796` | triangulated polygon with holes — a ground plane is one call |
| Solid-colour skybox | `FilamentRoomViewport.kt:214` | already driven by `LocalInteriorAccents.viewportBackground` |
| `setAutoHideWalls(on)` | `FilamentRoomViewport.kt:1032` | the switch an outside camera has to flip |
| `mat_bricks059`, `mat_concrete016/034`, `mat_plaster001`, `mat_pavingstones070` | `assets/models/` | brick, render, concrete and paving are **already shipped** |

---

## t7-1 — Outer shell from the union boundary

`exterior` per segment is not the same as an ordered ring, and a shell, a roof
edge and an overhang all need the ring.

Build it from the edges that exactly one room uses:

1. Collect every edge with `edgeUses.size == 1` — `edgeKey` at
   `FilamentRoomViewport.kt:548` already normalises the node pair.
2. In a well-formed plan each boundary node has exactly two such edges, so walk
   from any node until you return to it.
3. Wind the result consistently (`signedArea` is already in the file) so
   `outset` pushes outward rather than inward.

Traps, all of which a user can draw today:

- **Two rings.** Two rooms that share no edge give two separate boundaries. Walk
  until every boundary edge is consumed and return a `List<List<WallPoint>>`,
  not one polygon.
- **A node with four boundary edges** — two rooms touching only at a corner. The
  walk has to pick the turn that keeps the ring, not just "the other edge".
- **A courtyard** — a room-shaped hole inside the ring. That is an inner ring,
  wound the other way, and it is a hole for the roof and the ground.

Put this on `FloorPlan` next to `roomsOnLevel`, not in the viewport: the 2D
canvas will want it too, and it is pure geometry with no Filament in it.

## t7-2 — Ground plane and plot boundary

Cheapest useful version: one large quad at `y = 0` under the whole stack, its
own material slot, extending well past the house so the horizon never shows a
cut edge. `buildFloorMesh` with a square polygon does it.

The plot boundary is `outset(outerRing, plotMarginCm)` drawn as a second, darker
quad on top of the ground with the house ring as a hole — `buildFloorMesh`
already takes holes, and this is what it is for.

Watch the shadow map: a ground plane much larger than the house will blow out
the shadow cascade and make interior shadows mushy. Keep it to a few times the
house span rather than a kilometre.

## t7-3 — Flat or single-pitch roof

**Blocked on the same thing handrails are.** `buildBox`
(`FilamentRoomViewport.kt:772`) builds a pure Y-rotation transform, so a slab
cannot be tilted. Add a `pitchDeg` parameter there and both this and the raking
handrail unblock — see [stairs.md § Why 3 is blocked](stairs.md#why-3-is-blocked).

Then:

- **Flat**: extrude the outer ring at `levelCount × (h + slab)`, outset by the
  eaves overhang. One `buildFloorMesh` for the deck, one thin box ring for the
  fascia.
- **Single pitch**: same deck, tilted about one edge of the ring's bounding box,
  with a triangular gable filling each side wall. The gable is not a box — it
  needs a small custom mesh or a triangulated polygon.

Hipped and gabled roofs are their own project: they need a straight-skeleton
solve over the ring, which is a lot more than this step is worth. Say so in the
UI rather than half-doing it.

## t7-4 — Exterior material presets

Reuse `SurfaceSheet` as-is. Add `EXTERIOR_PRESETS` and `ROOF_PRESETS` to
`FurnitureCatalog.kt` beside `WALL_PRESETS` / `FLOOR_PRESETS`, and store the
picks on `LevelSurface`'s sibling — a new `ExteriorSurface` on `FloorPlan`,
since exterior finish is per building, not per storey.

Most of the materials already exist: brick, render, plaster, concrete and paving
stones are in `assets/models/`. Only a roof covering is genuinely missing.

## t7-5 — Outside camera mode

`autoHideWalls` (`FilamentRoomViewport.kt:1067`) hides any exterior wall that
faces the camera or that the camera is outside of. That is exactly right for
looking in and exactly wrong for looking at the house, so an outside mode has to
call `setAutoHideWalls(false)` and restore the user's setting on the way back.

Two more things the mode has to change:

- **Camera framing.** `structSig`'s reframe (`:297`) frames one storey stack
  from fairly close in. An exterior view wants a larger radius and a lower
  elevation. Keep them as separate saved orbits so switching back does not lose
  the interior viewpoint.
- **Level switching.** Inside, the viewport draws storeys `0..activeLevel`.
  Outside it should always draw all of them, or the house is missing its top.

Make it a third `EditorMode` rather than a flag on `DESIGN` — the top bar, the
FAB and the furniture sheet all mean something different out there.

## t7-6 — Asset budget

Where it stands now:

```
assets/models     6.0 MB    of which mat_*.glb   1.1 MB across 19 files
assets/previews   640 KB
assets total      6.7 MB
```

A `mat_*.glb` is a textured quad with one 512 px CC0 JPEG inside — ~60 KB each,
the largest is 145 KB. So exterior finishes are close to free: a roof tile, a
metal sheet and a grass/gravel ground is three new files, ~200 KB.

What would *not* be free is exterior props — trees, fences, cars. Those are real
meshes and the interior pack is already 5 MB. Decide before importing any:
either skip props entirely for this step, or set a hard ceiling (say 1 MB) and
pick within it.
