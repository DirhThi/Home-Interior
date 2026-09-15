# Step 7 — Exterior

**Done**, except that the roof is flat only — single-pitch is still open, and
`buildBox`'s `pitchDeg` is what it needs. The rest of this file is the record of how
it was built and what was deliberately left out.

Six tasks.
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

## t7-1 — Outer shell from the union boundary — done

`exterior` per segment is not the same as an ordered ring, and a shell, a roof
edge and an overhang all need the ring.

Build it from the edges that exactly one room uses:

1. Collect every edge with `edgeUses.size == 1` — `edgeKey` at
   `FilamentRoomViewport.kt:548` already normalises the node pair.
2. In a well-formed plan each boundary node has exactly two such edges, so walk
   from any node until you return to it.
3. Wind the result consistently (`signedArea` is already in the file) so
   `outset` pushes outward rather than inward.

Built as `FloorPlan.outlineRings(level)`. Each room's ring is normalised to
counter-clockwise first, so the edges only one room uses already point the same way
round the outside and walking them needs no geometry beyond following the chain. A
pinch point — two rooms meeting at a single corner — is resolved by taking the
sharpest left turn. Checked against all four sample plans (ring area matches the
stated floor area exactly) and against every trap below.

Traps, all of which a user can draw today:

- **Two rings.** Two rooms that share no edge give two separate boundaries. Walk
  until every boundary edge is consumed and return a `List<List<WallPoint>>`,
  not one polygon.
- **A node with four boundary edges** — two rooms touching only at a corner. The
  walk has to pick the turn that keeps the ring, not just "the other edge".
- **A courtyard** — a room-shaped hole inside the ring. That is an inner ring,
  wound the other way, and it is a hole for the roof and the ground. Verified: a
  donut of six rooms returns the 54 m² outer ring counter-clockwise and the 9 m²
  courtyard clockwise, so `filter { signedArea(it) > 0f }` picks out the outsides.

Put this on `FloorPlan` next to `roomsOnLevel`, not in the viewport: the 2D
canvas will want it too, and it is pure geometry with no Filament in it.

## t7-2 — Ground plane and plot boundary — done

Cheapest useful version: one large quad at `y = 0` under the whole stack, its
own material slot, extending well past the house so the horizon never shows a
cut edge. `buildFloorMesh` with a square polygon does it.

The plot boundary is `outset(outerRing, plotMarginCm)` drawn as a second, darker
quad on top of the ground with the house ring as a hole — `buildFloorMesh`
already takes holes, and this is what it is for.

Watch the shadow map: a ground plane much larger than the house will blow out
the shadow cascade and make interior shadows mushy. Keep it to a few times the
house span rather than a kilometre.

## t7-3 — Flat or single-pitch roof — flat done

**No longer blocked.** `buildBox` takes a `pitchDeg` that tilts a box about its own
length — added for the raking handrail, and exactly what a sloped slab needs.

Then:

- **Flat**: extrude the outer ring at `levelCount × (h + slab)`, outset by the
  eaves overhang. One `buildFloorMesh` for the deck, one thin box ring for the
  fascia.
- **Single pitch**: same deck, tilted about one edge of the ring's bounding box,
  with a triangular gable filling each side wall. The gable is not a box — it
  needs a small custom mesh or a triangulated polygon.

**Built: flat only.** One roof per storey, each cut with the storey above as a hole
— without the hole a smaller upper floor reads as a lid on a lid instead of a box
standing on a terrace, and the two slabs z-fight. `buildFloorMesh` already takes
holes, so this costs nothing. Single-pitch is still open, and `pitchDeg` is now
there for it.

Hipped and gabled roofs are their own project: they need a straight-skeleton
solve over the ring, which is a lot more than this step is worth. Say so in the
UI rather than half-doing it.

## t7-4 — Exterior material presets — done

`ROOF_PRESETS` (5) and `GROUND_PRESETS` (6) sit beside `WALL_PRESETS` /
`FLOOR_PRESETS`, and the picks live on `ExteriorSurface` in the plan — per building,
not per storey. `SurfaceSheet` gained an **Outside** tab holding both rows.

**Not built: a separate exterior wall finish.** An exterior wall is the same
procedural box as the interior one, and a box carries a single material, so an
outside face different from the inside needs a second skin of boxes over every
exterior segment. That is a real chunk of work and it was not part of this step —
outside walls take the storey's wall preset, like the inside.

The field beyond the plot is fixed turf rather than a preset: it is scenery, not a
finish anyone would pick.

## t7-5 — Outside camera mode — done

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

## t7-6 — Asset budget — done, nothing added

Where it stands now:

```
assets/models     6.0 MB    of which mat_*.glb   1.1 MB across 19 files
assets/previews   640 KB
assets total      6.7 MB
```

**Nothing was added.** Every roof and ground preset is a tint over a `mat_*.glb`
already in the build — brick for clay tile, concrete for slate and gravel, paving
stones, plaster, timber. The figures above are the figures now.

**Decision on exterior props** (trees, fences, cars): skipped, not deferred. They
are real meshes, the interior pack is already 5 MB of the 6.7, and a house that sits
on a plot with a roof reads fine without them. Revisit only with a hard ceiling —
1 MB — and pick within it.
