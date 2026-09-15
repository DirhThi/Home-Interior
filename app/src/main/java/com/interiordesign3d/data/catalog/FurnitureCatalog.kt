package com.interiordesign3d.data.catalog

// ─── Furniture catalog (Quaternius q_ House pack + qf_ Furniture pack) ─────────────────────────────────
// key = glb file name in assets/models (and PlacedFurniture.furnitureId).
// preview = webp in assets/previews. Models are rendered at natural size × unitScale × `scale`. mount decides floor / wall (auto wall-mount) / ceiling.

enum class MountType { FLOOR, WALL, CEILING }

data class CatalogItem(
    val key: String,
    val label: String,
    val mount: MountType = MountType.FLOOR,
    val wallHeightCm: Float = 120f,   // default mount height for WALL items
    val surface: Boolean = false,      // small items dropped over it rest on its top
    val unitScale: Float = if (key.startsWith("q_")) 0.5f else 1f,  // model units → metres (Quaternius packs are 2×)
) {
    val wallMounted get() = mount == MountType.WALL
    val preview get() = "previews/$key.webp"
    val model get() = "models/$key.glb"
}

data class CatalogGroup(val title: String, val items: List<CatalogItem>)

private fun fi(key: String, label: String, mount: MountType = MountType.FLOOR, h: Float = 120f, surface: Boolean = false, s: Float? = null) =
    if (s == null) CatalogItem(key, label, mount, h, surface) else CatalogItem(key, label, mount, h, surface, s)

val FURNITURE_CATALOG: List<CatalogGroup> = listOf(
    CatalogGroup("Seating & tables", listOf(
        fi("q_chair_2", "Chair 3"),
        fi("q_chair_3", "Chair 4"),
        fi("q_couch_large2", "Large sofa 2"),
        fi("q_couch_large3", "Large sofa 3"),
        fi("qf_armchair", "Classic armchair", s = 0.5f),
        fi("qf_chair_wood", "Classic wooden chair"),
        fi("qf_sofa_brown", "Brown leather sofa", s = 0.5f),
        fi("qf_sofa_corner", "Black corner sofa", s = 0.5f),
        fi("qf_sofa_navy", "Navy sofa", s = 0.5f),
        fi("qf_stool", "Wooden stool"),
        fi("qf_table", "Classic wooden table", surface = true),
        fi("qf_table_2", "Classic wooden table 2", surface = true),
        fi("qf_desk", "Wooden desk", surface = true),
        fi("qf_office_chair", "Blue office chair"),
        fi("q_chair_rlyhe93nne", "Chair"),
        fi("q_chair", "Chair 2"),
        fi("q_couch_large", "Large sofa"),
        fi("q_couch_medium_mwgq94zhdz", "Medium sofa"),
        fi("q_couch_medium", "Medium sofa 2"),
        fi("q_couch_small_x9msj0gtb5", "Small sofa"),
        fi("q_couch_small", "Small sofa 2"),
        fi("q_l_couch", "L-shaped sofa"),
        fi("q_stool", "Stool"),
        fi("q_table_round_large", "Large round table", surface = true),
        fi("q_table_round_small_57w671wvs2", "Small round table", surface = true),
        fi("q_table_round_small", "Small round table 2", surface = true),
    )),
    CatalogGroup("Bedroom", listOf(
        fi("q_bookshelf", "Tall bookshelf"),
        fi("q_shelf_1", "Standing shelf"),
        fi("q_shelf_2", "Standing shelf 2"),
        fi("qf_bed_double", "Classic double bed", s = 0.5f),
        fi("qf_bed_twin", "Classic twin bed", s = 0.5f),
        fi("qf_bookcase", "Bookcase", s = 0.5f),
        fi("qf_closet", "Wooden wardrobe", s = 0.5f),
        fi("qf_closet_short", "Low cabinet", surface = true, s = 0.5f),
        fi("qf_night_stand", "Classic nightstand", surface = true),
        fi("q_bed_king", "King bed"),
        fi("q_bed_single", "Single bed"),
        fi("q_bunk_bed", "Bunk bed"),
        fi("q_curtains_double", "Double curtains", MountType.WALL, 140f),
        fi("q_drawer_8xzqezl2w3", "Dresser", surface = true),
        fi("q_drawer_g1h0wnchqf", "Dresser 2", surface = true),
        fi("q_drawer_n3eri89oeo", "Dresser 3", surface = true),
        fi("q_drawer_t4udbyp90c", "Dresser 4", surface = true),
        fi("q_drawer", "Dresser 5", surface = true),
        fi("q_night_stand_08s1j15jcx", "Nightstand", surface = true),
        fi("q_night_stand_7cobkfclnv", "Nightstand 2", surface = true),
        fi("q_night_stand", "Nightstand 3", surface = true),
        fi("q_shelf_large", "Large shelf", surface = true),
        fi("q_shelf_small_tfdguv2rye", "Small shelf", surface = true),
        fi("q_shelf_small", "Small shelf 2", surface = true),
    )),
    CatalogGroup("Kitchen", listOf(
        fi("q_kitchen_cabinet1", "Kitchen cabinet", surface = true),
        fi("q_kitchen_cabinet2", "Kitchen cabinet 2", surface = true),
        fi("q_kitchen_cabinetsmall", "Small kitchen cabinet", surface = true),
        fi("q_kitchen_1drawers", "Kitchen unit, 1 drawer", surface = true),
        fi("q_kitchen_2drawers", "Kitchen unit, 2 drawers", surface = true),
        fi("q_kitchen_3drawers", "Kitchen unit, 3 drawers", surface = true),
        fi("q_kitchen_oven", "Built-in oven", surface = true),
        fi("q_kitchen_fridge", "Fridge"),
        fi("q_kitchen_sink", "Kitchen sink"),
        fi("q_oven", "Oven"),
        fi("q_square_plate", "Square plate"),
        fi("q_trashcan_large", "Large bin"),
        fi("q_trashcan_small_zwytk2smba", "Small bin"),
        fi("q_trashcan_small", "Small bin 2"),
        fi("q_trashcan_xswahu252t", "Bin"),
        fi("q_trashcan", "Bin 2"),
        fi("q_washing_machine", "Washing machine"),
    )),
    CatalogGroup("Bathroom", listOf(
        fi("q_bathroom_toilet2", "Toilet 2"),
        fi("q_bathroom_shower1", "Shower"),
        fi("q_bathroom_mirror1", "Mirror", MountType.WALL, 150f),
        fi("q_bathroom_mirror2", "Mirror 2", MountType.WALL, 150f),
        fi("q_bathroom_sink", "Basin"),
        fi("q_bathroom_toilet_paper", "Toilet paper"),
        fi("q_bathtub", "Bathtub"),
        fi("q_toilet_paper_stack", "Toilet paper stack"),
        fi("q_toilet", "Toilet"),
        fi("q_towel_rack", "Towel rack", MountType.WALL, 140f),
    )),
    CatalogGroup("Decor & lighting", listOf(
        fi("q_carpet_2", "Rug 2"),
        fi("q_curtains_single", "Single curtain", MountType.WALL, 140f),
        fi("q_light_ceiling5", "Ceiling light 5", MountType.CEILING),
        fi("q_light_ceiling6", "Ceiling light 6", MountType.CEILING),
        fi("q_light_icosahedron2", "Faceted pendant 2", MountType.CEILING),
        fi("q_light_stand2", "Standing lamp 2"),
        fi("o_phone_2", "Phone", s = 0.1651f),
        fi("o_boxes", "Cardboard boxes", s = 0.5f),
        fi("q_table_lamp", "Table lamp"),
        fi("q_cactus", "Cactus"),
        fi("q_ceiling_light", "Ceiling light", MountType.CEILING),
        fi("q_dead_houseplant", "Dead houseplant"),
        fi("q_fireplace", "Fireplace"),
        fi("q_houseplant_iblx2jz90o", "Houseplant"),
        fi("q_houseplant_vtjh4irl4w", "Houseplant 2"),
        fi("q_houseplant_bfloqiv5up", "Houseplant 3"),
        fi("q_houseplant_dveij0xnpx", "Houseplant 4"),
        fi("q_houseplant_f6gpjbegg0", "Houseplant 5"),
        fi("q_houseplant", "Houseplant 6"),
        fi("q_lamp", "Lamp"),
        fi("q_light_ceiling_single", "Single ceiling light", MountType.CEILING),
        fi("q_light_ceiling_nnlnaidjih", "Ceiling light 2", MountType.CEILING),
        fi("q_light_ceiling_tooljdo5fi", "Ceiling light 3", MountType.CEILING),
        fi("q_light_ceiling", "Ceiling light 4", MountType.CEILING),
        fi("q_light_chandelier", "Chandelier"),
        fi("q_light_cube_gjpaleplcy", "Cube pendant", MountType.CEILING),
        fi("q_light_cube", "Cube pendant 2", MountType.CEILING),
        fi("q_light_desk", "Desk lamp"),
        fi("q_light_floor_ebqtooeh43", "Floor lamp"),
        fi("q_light_floor", "Floor lamp 2"),
        fi("q_light_icosahedron", "Faceted pendant", MountType.CEILING),
        fi("q_light_stand", "Standing lamp"),
        fi("q_round_rug", "Round rug"),
        fi("q_rug", "Rug"),
    )),
    CatalogGroup("Architecture", listOf(
        fi("q_column_squarebig", "Square column"),
        fi("q_column_squaresmall", "Short square column"),
        fi("q_column_round", "Round column"),
        fi("q_door_double", "Double door"),
        fi("q_door_8it1hh1oru", "Door"),
        fi("q_door_kgt4ztckrm", "Door 2"),
        fi("q_door_li93wgnjys", "Door 3"),
        fi("q_door_atrxvw0q9n", "Door 4"),
        fi("q_door_b0aifzp6vl", "Door 5"),
        fi("q_door_ihxwsn12p1", "Door 6"),
        fi("q_door_xufnanlzfe", "Door 7"),
        fi("q_door", "Door 8"),
        fi("q_window_large", "Large window", MountType.WALL, 140f),
        fi("q_window_round", "Round window", MountType.WALL, 140f),
        fi("q_window_small", "Small window", MountType.WALL, 140f),
    )),
)

private val CATALOG_BY_KEY: Map<String, CatalogItem> =
    FURNITURE_CATALOG.flatMap { it.items }.associateBy { it.key }

fun catalogItem(key: String): CatalogItem? = CATALOG_BY_KEY[key]

/** Model attributions (CC-BY items require credit in-app). */
val MODEL_CREDITS: List<String> = listOf(
    "Quaternius (quaternius.com) — Ultimate House Interior Pack, Furniture Pack (CC0)",
    "ambientCG.com — wall and floor textures (CC0)",
)

// ─── Wall / floor surface presets (choosing one replaces ALL walls / the floor) ──
// model = mat_*.glb (textured quad, ambientCG CC0); colorHex tints it; tileM = metres per texture repeat.

data class SurfacePreset(val label: String, val model: String, val colorHex: String = "#FFFFFF", val tileM: Float = 1f) {
    val preview get() = "previews/$model.webp"
    val isPaint get() = colorHex != "#FFFFFF"
}

val WALL_PRESETS = listOf(
    SurfacePreset("Warm cream", "mat_paintedplaster017", "#F2E6D3", 2f),
    SurfacePreset("White", "mat_paintedplaster017", "#F6F4F0", 2f),
    SurfacePreset("Beige", "mat_paintedplaster017", "#E0D2BC", 2f),
    SurfacePreset("Grey", "mat_paintedplaster017", "#B9BDC2", 2f),
    SurfacePreset("Soft green", "mat_paintedplaster017", "#C5D6CF", 2f),
    SurfacePreset("Blush", "mat_paintedplaster017", "#EAD3CF", 2f),
    SurfacePreset("Raw plaster", "mat_plaster001", tileM = 2f),
    SurfacePreset("Red brick", "mat_bricks059", tileM = 1.5f),
    SurfacePreset("Concrete", "mat_concrete016", tileM = 2f),
    SurfacePreset("Ceramic tile", "mat_tiles101", tileM = 1.2f),
    SurfacePreset("Marble", "mat_marble012", tileM = 2f),
)

// Outside finishes. Roof and apron are separate objects from the walls, so they get their own
// lists; an exterior WALL finish would need a second skin of boxes and is not built.
// A preset's INDEX is what gets stored in the plan, so only ever append to these lists. Reordering
// or inserting silently repaints every saved room — the roof went from clay to corrugated steel that
// way while this was being written.
val ROOF_PRESETS = listOf(
    SurfacePreset("Dark slate", "mat_concrete016", "#7E7A76", 2f),
    SurfacePreset("Grey felt", "mat_concrete034", "#B4B0AA", 2f),
    SurfacePreset("Bare concrete", "mat_concrete016", tileM = 2f),
    SurfacePreset("Brick red", "mat_bricks059", "#C4643F", 1.2f),
    SurfacePreset("White deck", "mat_plaster001", "#F2F0EC", 2f),
    // colorHex multiplies the texture, so it can only darken: a red tint over a dark slate scan
    // stays dark. Terracotta has to come from a texture that is already terracotta.
    SurfacePreset("Terracotta", "mat_roofingtiles012", tileM = 1.2f),
    SurfacePreset("Aged clay", "mat_roofingtiles014", tileM = 1.2f),
    SurfacePreset("Charcoal tile", "mat_roofingtiles013", tileM = 1.2f),
    SurfacePreset("Slate tile", "mat_roofingtiles003", tileM = 1.6f),
    SurfacePreset("Ridged slate", "mat_roofingtiles001", tileM = 1.6f),
    SurfacePreset("Metal sheet", "mat_corrugatedsteel009", tileM = 1.6f),
)

val GROUND_PRESETS = listOf(
    SurfacePreset("Paving", "mat_pavingstones070", tileM = 1.5f),
    SurfacePreset("Gravel", "mat_gravel023", tileM = 1.5f),
    SurfacePreset("Lawn", "mat_grass005", tileM = 1.5f),
    SurfacePreset("Timber deck", "mat_woodfloor043", tileM = 1.2f),
    SurfacePreset("Sand", "mat_concrete034", "#D9C9A8", 2f),
    SurfacePreset("Dark stone", "mat_marble006", tileM = 2f),
    SurfacePreset("Bare earth", "mat_ground037", tileM = 2f),
)

val FLOOR_PRESETS = listOf(
    SurfacePreset("Light oak", "mat_woodfloor051", tileM = 1f),
    SurfacePreset("Golden oak", "mat_woodfloor040", tileM = 1f),
    SurfacePreset("Walnut", "mat_woodfloor007", tileM = 1f),
    SurfacePreset("Raw timber", "mat_woodfloor043", tileM = 1.2f),
    SurfacePreset("Parquet", "mat_woodfloor054", tileM = 1f),
    SurfacePreset("Marble", "mat_marble012", tileM = 2f),
    SurfacePreset("Black stone", "mat_marble006", tileM = 2f),
    SurfacePreset("Checker tile", "mat_tiles074", tileM = 1.5f),
    SurfacePreset("Patterned tile", "mat_tiles101", tileM = 1.2f),
    SurfacePreset("Encaustic tile", "mat_tiles131", tileM = 1f),
    SurfacePreset("Red carpet", "mat_carpet013", tileM = 1f),
    SurfacePreset("Beige carpet", "mat_carpet008", tileM = 1f),
    SurfacePreset("Concrete", "mat_concrete034", tileM = 2f),
    SurfacePreset("Paving stone", "mat_pavingstones070", tileM = 1.5f),
    SurfacePreset("Ash timber", "mat_woodfloor064", tileM = 1f),
    SurfacePreset("Stone tile", "mat_tiles141", tileM = 1.2f),
)
