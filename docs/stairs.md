# Stairs

How a flight is described, what the 2D and 3D each do with it, and the two
things still missing. Everything here is procedural — there is no stair GLB.

## The model

`Stair` — `data/models/Models.kt:75`

| Field | Default | Meaning |
|---|---|---|
| `level` | 0 | the storey it rises **from**; it always lands on `level + 1` |
| `x`, `y` | — | centre of the bounding box, plan cm |
| `shape` | `STRAIGHT` | `STRAIGHT` · `L_SHAPED` · `U_SHAPED` |
| `widthCm` | 100 | width of **one** run |
| `lengthCm` | per shape | box length, along the first run — `Stair.defaultLengthCm` |
| `legCm` | 200 | L only: the second run |
| `wellCm` | 20 | U only: the gap between the two runs |
| `rotationDeg` | 0 | about the box centre |

Height is deliberately **not** a field: a flight always spans exactly one
storey, so it is derived from the ceiling height plus the floor slab.

Derived geometry, all in the same file:

- `boxWidth` — `w` / `w + leg` / `2w + well` by shape; `boxLength` is `lengthCm`.
- `runs()` — the straight sections, as plan-coordinate segment pairs. Steps
  belong to these and nothing else.
- `landings()` — the flat platforms between runs, as
  `(centre, sizeAlongU, sizeAlongV)`. Empty for `STRAIGHT`; `w × w` at the turn
  for L; `boxWidth × w` across the head for U.
- `footprint(margin)` — the true outline (concave for L and U). **2D only.**
- `wellOpening(margin)` — the bounding rectangle. This is what gets cut out of
  the slab above. It is a rectangle on purpose: a real stairwell is, and a
  concave L/U outline made the ear clipper give up, which showed as a ragged
  hole.
- `toPlan(local)` — box frame → plan frame.

`FloorPlan.fitStair` (`Models.kt:260`) forces a flight to fit inside one room on
the storey above: it scales `widthCm`, `lengthCm`, `legCm` and `wellCm` by a
single factor so proportions hold, then clamps the centre. Floors are 70 cm /
150 cm (`MIN_STAIR_WIDTH_CM`, `MIN_STAIR_LENGTH_CM`). It runs on **every** stair
mutation — see `updateStairById` in `DesignerViewModel`.

## What the 3D does

`FilamentRoomViewport.kt:481`

```
rise  = ceilingHeight + FLOOR_SLAB_M          // 2.70 + 0.05 m
count = round(rise / 0.17).coerceIn(10, 28)   // 16 steps
riser = rise / count                          // 17.2 cm
```

Steps are shared out across `runs()` in proportion to each run's length; the
last run takes whatever is left so the total is exactly `count` and the top step
lands flush with the upper floor's surface. After every run but the last, one
flat box is dropped from `landings()`: `riser` thick, its top level with the
last step of the run feeding it, so the first step of the next run is exactly
one riser above it.

That split is the whole point. Running steps continuously around a turn made the
corner treads fan out with gaps between them — the bug this replaced.

## What the 2D does

`FloorPlanCanvas.kt:490` draws `footprint()` as the outline and treads **per
run**, leaving the landing square blank. That is how a turning flight reads on a
real drawing, and it falls out of using the same `runs()` the 3D uses.

## Proportions — done

The riser was always right; the run was not. Fixed by giving each shape its own
default depth, since a turning flight gets its second run for free:

| Shape | Default | Going | Tread at a 2.70 m storey |
|---|---|---|---|
| Straight | `L` 400 | 400 | 25.0 cm |
| L | `L` 300, `leg` 200 | 400 | 25.0 cm |
| U | `L` 300, `well` 20 | 400 | 25.0 cm |

`Stair.stepCount(riseCm)` and `Stair.treadCm(riseCm)` now live on the model, so the
viewport and the panel cannot disagree about how many steps a flight has — the
viewport used to carry its own `0.17f` and `coerceIn(10, 28)`.

`legCm` and `wellCm` are editable in the panel (they were not), shown only for the
shape that uses them. And when the flight ends up shallow anyway — `fitStair`
shrinks it into a small room — the panel says so rather than quietly producing a
ladder: *"Steps are only 21 cm deep"*, below `MIN_COMFORTABLE_TREAD_CM`.

Switching shape carries the new default across only when the length is still the
old shape's default; a hand-set length is the user's number and survives.

### The original numbers, for the record

The riser is already right. The run is not.

With a 2.70 m ceiling the engine picks 16 risers at 17.2 cm, which is squarely
inside the 16–19 cm a stair should use. But 16 steps need roughly
16 × 25 = 400 cm of going, and the defaults give far less:

| Shape | Total going | Tread at the defaults | Length for a 25 cm tread |
|---|---|---|---|
| Straight | `L` | **15.0 cm** (L 240) | L ≈ 400 |
| L | `(L − W) + leg` = 300 | **18.8 cm** (L 240, leg 160) | L 300 + leg 200 |
| U | `2 × (L − W)` = 280 | **17.5 cm** (L 240, W 100) | **L 300** (W 100) |

**Not** a fix, and still not: adding a step-count control. Rise is locked to one
storey, so fewer steps only means a taller riser. Length is the honest lever.

## Handrails — done

Built from boxes, no new asset, tinted with the Stairs preset so a handrail follows
whatever timber the flight is set to.

- **Raking rail**, one per side of every run: a single box along the nosing line,
  tilted by `atan(rise / going)`.
- **Balusters** standing on every nosing, `RAIL_H_M` tall, so rail and baluster meet
  exactly whatever the pitch — both are derived from the same nosing line.
- **Landing guard** on the sides no run arrives at. `Stair.landingRails()` derives
  those sides rather than hard-coding them per shape: it takes the landing rectangle
  and drops any edge a run endpoint touches. An L frees two sides, a U frees three,
  and a new shape would get its rails for nothing.

The blocker below is gone: `buildBox` now takes `pitchDeg`, which tilts a box about
its own length. **A flat or single-pitch roof can use the same parameter** — see
[exterior.md](exterior.md) t7-3.

Not there yet: no rail where a flight meets the floor above, so the stairwell itself
is still an unguarded hole in the upper slab.

### How it was blocked

No new asset needed. `runs()` and `landings()` already give the centre lines,
and everything can be boxes tinted with the stair material (`stairMi`), so a
handrail inherits the Stairs preset for free.

Three pieces, in increasing difficulty:

1. **Balusters** — one 4 × 4 cm box per step at the outer edge of each run,
   rising ~85 cm from that step's top. Pure `buildBox`, works today.
2. **Guarding around the stairwell** — the opening cut in the slab above is
   `wellOpening()`, a rectangle, so this is four horizontal rails plus posts.
   Also pure `buildBox`. Skip any side that a wall already closes off.
3. **The raking rail itself** — blocked.

### Why the raking rail was blocked

`buildBox` (`FilamentRoomViewport.kt:772`) builds its transform as a pure
Y-rotation:

```kotlin
val m = floatArrayOf(c, 0f, -sn, 0f,  0f, 1f, 0f, 0f,  sn, 0f, c, 0f,  px, py, pz, 1f)
```

There is nowhere to put a pitch, so a box cannot be tilted to follow a flight.
Options:

- Add a `pitchDeg` parameter and pre-multiply a rotation about the box's local X
  before the Y-rotation. Smallest change, and it lands exactly where a flat or
  single-pitch **roof** needs it too (see [exterior.md](exterior.md) t7-3), so
  it pays for itself twice.
- Or write a dedicated `buildRail(fromPoint, toPoint, ...)` that takes two
  endpoints at different heights and derives the transform. Cleaner call site,
  more code.

Prefer the first. Do it once, use it for the rail and the roof.

Rails on a landing are horizontal, so they need none of this — a landing rail
and the stairwell guard can ship before the raking rail does.

Out of scope: spiral stairs. A helical flight is not expressible as straight
runs plus landings and would need its own model and its own hole shape.
